/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jvvfs;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Pair;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FileReference.Type;
import com.io7m.jvvfs.FilesystemError.Code;

/**
 * <p>
 * The basic filesystem implementation.
 * </p>
 * <p>
 * This implementation supports loading of archives from a single directory,
 * or from the classpath.
 * </p>
 * 
 * @see #makeWithArchiveDirectory(Log, PathReal)
 * @see #makeWithoutArchiveDirectory(Log)
 */

public final class Filesystem implements FSCapabilityAll
{
  private static abstract class FSReference
  {
    final @Nonnull FSReferenceType type;

    FSReference(
      final @Nonnull FSReferenceType in_type)
    {
      this.type = in_type;
    }
  }

  private static final class FSReferenceArchive extends FSReference
  {
    final @Nonnull FileReference<?> ref;

    FSReferenceArchive(
      final @Nonnull FileReference<?> in_ref)
    {
      super(FSReferenceType.FS_REF_ARCHIVE);
      this.ref = in_ref;
    }
  }

  private static enum FSReferenceType
  {
    FS_REF_ARCHIVE,
    FS_REF_VIRTUAL_DIRECTORY
  }

  private static final class FSReferenceVirtualDirectory extends FSReference
  {
    final @Nonnull PathVirtual path;
    final @Nonnull Calendar    mtime;

    FSReferenceVirtualDirectory(
      final @Nonnull PathVirtual in_path,
      final @Nonnull Calendar in_mtime)
    {
      super(FSReferenceType.FS_REF_VIRTUAL_DIRECTORY);
      this.path = in_path;
      this.mtime = in_mtime;
    }
  }

  private static class UpdateTimeEntry
  {
    final @Nonnull Calendar time_when_updated;
    final @Nonnull Calendar time_value;

    UpdateTimeEntry(
      final @Nonnull Calendar in_time_when_updated,
      final @Nonnull Calendar in_time_value)
    {
      this.time_when_updated = in_time_when_updated;
      this.time_value = in_time_value;
    }
  }

  private static @Nonnull Calendar getUTCTimeNow()
  {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  }

  @SuppressWarnings("unchecked") private static @Nonnull
    Option<FileReference<?>>
    lookupDirectInArchive(
      final @Nonnull Archive<?> a,
      final @Nonnull PathVirtual path)
      throws FilesystemError,
        ConstraintError
  {
    final Option<?> r = a.lookup(path);
    return (Option<FileReference<?>>) r;
  }

  /**
   * <p>
   * Construct a filesystem using <code>archives</code> as the location of the
   * archive directory.
   * </p>
   * 
   * @throws ConstraintError
   *           Iff <code>log == null || archives == null</code>.
   */

  public static @Nonnull Filesystem makeWithArchiveDirectory(
    final @Nonnull Log log,
    final @Nonnull PathReal archives)
    throws ConstraintError
  {
    Constraints.constrainNotNull(archives, "Archive directory");
    return new Filesystem(log, archives);
  }

  /**
   * <p>
   * Construct a filesystem without an archive directory. A filesystem
   * constructed in this manner may only access archives on the classpath.
   * </p>
   * 
   * @throws ConstraintError
   *           Iff <code>log == null</code>.
   */

  public static @Nonnull Filesystem makeWithoutArchiveDirectory(
    final @Nonnull Log log)
    throws ConstraintError
  {
    return new Filesystem(log, null);
  }

  private final @Nonnull Log                               log;
  private final @Nonnull Log                               log_directory;
  private final @Nonnull Log                               log_mount;
  private final @Nonnull Log                               log_lookup;
  private final @Nonnull Option<PathReal>                  archives;
  private final @Nonnull List<ArchiveHandler<?>>           handlers;
  private final @Nonnull LinkedList<Archive<?>>            archive_list;
  private final @Nonnull Map<PathVirtual, Calendar>        directories;
  private final @Nonnull Map<PathVirtual, UpdateTimeEntry> time_updates;

  private Filesystem(
    final @Nonnull Log in_log,
    final @CheckForNull PathReal in_archives)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log interface"),
        "filesystem");
    this.log_directory = new Log(this.log, "directory");
    this.log_mount = new Log(this.log, "mount");
    this.log_lookup = new Log(this.log, "lookup");

    this.archives =
      in_archives == null
        ? new Option.None<PathReal>()
        : new Option.Some<PathReal>(in_archives);

    this.handlers = new ArrayList<ArchiveHandler<?>>();
    this.handlers.add(new ArchiveDirectoryHandler());
    this.handlers.add(new ArchiveZipHandler());

    this.archive_list = new LinkedList<Archive<?>>();

    this.directories = new HashMap<PathVirtual, Calendar>();
    this.directories.put(PathVirtual.ROOT, Filesystem.getUTCTimeNow());
    this.time_updates = new HashMap<PathVirtual, UpdateTimeEntry>();
  }

  @Override public void close()
    throws ConstraintError,
      FilesystemError
  {
    for (final Archive<?> a : this.archive_list) {
      a.close();
    }

    this.archive_list.clear();
    this.time_updates.clear();
    this.directories.clear();
    this.directories.put(PathVirtual.ROOT, Filesystem.getUTCTimeNow());
  }

  @Override public void createDirectory(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    Constraints.constrainNotNull(path, "Path");

    this.log_directory.info("create-directory: " + path.toString());

    final PathVirtualEnum e = new PathVirtualEnum(path);
    while (e.hasMoreElements()) {
      final PathVirtual ancestor = e.nextElement();
      this.createDirectoryDirect(ancestor);
    }

    this.createDirectoryDirect(path);
  }

  /**
   * Lookup <code>path</code>. If <code>path</code> exists and is a file,
   * fail. Otherwise, mark it as explicitly created.
   */

  private void createDirectoryDirect(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    final Option<? extends FSReference> r = this.lookup(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        this.directories.put(path, Filesystem.getUTCTimeNow());
        break;
      }
      case OPTION_SOME:
      {
        final Some<? extends FSReference> s =
          (Option.Some<? extends FSReference>) r;

        switch (s.value.type) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) s.value;
            switch (ra.ref.type) {
              case TYPE_DIRECTORY:
              {
                this.directories.put(path, Filesystem.getUTCTimeNow());
                return;
              }
              case TYPE_FILE:
              {
                throw FilesystemError.notDirectory(path.toString());
              }
            }

            throw new UnreachableCodeException();
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            break;
          }
        }
      }
    }
  }

  @Override public boolean exists(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(path, "Path");
    return this.lookup(path).isSome();
  }

  @Override public long getFileSize(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    Constraints.constrainNotNull(path, "Path");

    final Option<? extends FSReference> r = this.lookup(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<? extends FSReference> s =
          (Option.Some<? extends FSReference>) r;

        switch (s.value.type) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) s.value;
            switch (ra.ref.type) {
              case TYPE_DIRECTORY:
              {
                throw FilesystemError.notFile(path.toString());
              }
              case TYPE_FILE:
              {
                final Archive<?> a = ra.ref.archive;
                return a.getFileSize(path.subtract(a.getMountPath()));
              }
            }

            throw new UnreachableCodeException();
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            throw FilesystemError.notFile(path.toString());
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public @Nonnull Calendar getModificationTime(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    Constraints.constrainNotNull(path, "Path");
    final Calendar t_in_archive = this.getModificationTimeActual(path);
    if (this.time_updates.containsKey(path)) {
      final UpdateTimeEntry u = this.time_updates.get(path);
      if (t_in_archive.after(u.time_when_updated)) {
        return t_in_archive;
      }
      return u.time_value;
    }

    return t_in_archive;
  }

  /**
   * Retrieve the modification time of the object at <code>path</code>,
   * ignoring any explicit updates that may have been made.
   * 
   * @see #updateModificationTime(PathVirtual, Calendar)
   * @see #time_updates
   */

  private @Nonnull Calendar getModificationTimeActual(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final Option<? extends FSReference> r = this.lookup(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<? extends FSReference> s =
          (Option.Some<? extends FSReference>) r;

        switch (s.value.type) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) s.value;
            final Archive<?> a = ra.ref.archive;
            return a.getModificationTime(path.subtract(a.getMountPath()));
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            final FSReferenceVirtualDirectory rvd =
              (FSReferenceVirtualDirectory) s.value;
            return rvd.mtime;
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public @Nonnull
    Deque<Pair<PathReal, PathVirtual>>
    getMountedArchives()
  {
    final Deque<Pair<PathReal, PathVirtual>> result =
      new ArrayDeque<Pair<PathReal, PathVirtual>>();

    final Iterator<Archive<?>> iter = this.archive_list.descendingIterator();
    while (iter.hasNext()) {
      final Archive<?> a = iter.next();
      final Pair<PathReal, PathVirtual> p =
        new Pair<PathReal, PathVirtual>(a.getRealPath(), a.getMountPath());
      result.push(p);
    }

    return result;
  }

  @Override public boolean isDirectory(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(path, "Path");

    final Option<? extends FSReference> r = this.lookup(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        return false;
      }
      case OPTION_SOME:
      {
        final Some<? extends FSReference> s =
          (Option.Some<? extends FSReference>) r;

        switch (s.value.type) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) s.value;
            return ra.ref.type == Type.TYPE_DIRECTORY;
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            return true;
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public boolean isFile(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(path, "Path");

    this.log_lookup.info("is-file: " + path);

    final Option<? extends FSReference> r = this.lookup(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        return false;
      }
      case OPTION_SOME:
      {
        final Some<? extends FSReference> s =
          (Option.Some<? extends FSReference>) r;

        switch (s.value.type) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) s.value;
            return ra.ref.type == Type.TYPE_FILE;
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            return false;
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public @Nonnull SortedSet<String> listDirectory(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    Constraints.constrainNotNull(path, "Path");

    this.log_lookup.info("is-directory: " + path);

    final Option<FSReference> ro = this.lookup(path);
    switch (ro.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final FSReference ref = ((Option.Some<FSReference>) ro).value;
        switch (ref.type) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) ref;
            if (ra.ref.type != Type.TYPE_DIRECTORY) {
              throw FilesystemError.notDirectory(path.toString());
            }
            return this.listDirectoryInternal(path);
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            return this.listDirectoryInternal(path);
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  private @Nonnull SortedSet<String> listDirectoryInternal(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    final TreeSet<String> items = new TreeSet<String>();

    /**
     * Take the union of the sets of unshadowed files in the archive stack.
     */

    boolean lookup_shadowed = false;
    boolean lookup_first = true;
    final Iterator<Archive<?>> iter = this.archive_list.iterator();
    while (iter.hasNext() && (!lookup_shadowed)) {
      final Archive<?> a = iter.next();
      final PathVirtual mount = a.getMountPath();

      if (mount.isAncestorOf(path) || mount.equals(path)) {
        final PathVirtual a_path = path.subtract(mount);

        try {
          final Set<String> a_items = a.listDirectory(a_path);
          items.addAll(a_items);
        } catch (final FilesystemError e) {

          /**
           * The path checked has an ancestor that happens to be a file in
           * this archive. If this is the first archive that has been queried,
           * then raise an error. Otherwise, don't check any further archives.
           */

          if (e.getCode() == Code.FS_ERROR_NOT_A_DIRECTORY) {
            if (lookup_first) {
              throw e;
            }
            lookup_shadowed = true;
          }
        }

        lookup_first = false;
      }
    }

    /**
     * Add any virtual directories with parents equal to <code>path</code>.
     */

    for (final PathVirtual d : this.directories.keySet()) {
      if (path.isParentOf(d)) {
        final Option<String> name_o = d.getBaseName();
        switch (name_o.type) {
          case OPTION_NONE:
          {
            /**
             * If path is a parent of d, then d cannot be root.
             */

            throw new UnreachableCodeException();
          }
          case OPTION_SOME:
          {
            items.add(((Option.Some<String>) name_o).value);
          }
        }
      }
    }

    return items;
  }

  /**
   * <p>
   * Lookup the filesystem object at <code>path</code>.
   * </p>
   * <p>
   * All ancestors of <code>path</code> are checked to ensure that they exist
   * and are directories.
   * </p>
   */

  private <T extends FSReference> Option<T> lookup(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log_lookup.debug(path.toString());

    /**
     * Check that all ancestors of <code>path</code> exist and are
     * directories.
     */

    final PathVirtualEnum e = new PathVirtualEnum(path);
    while (e.hasMoreElements()) {
      final PathVirtual ancestor = e.nextElement();
      this.lookupDirectAssertIsDirectory(ancestor);
    }

    /**
     * All ancestors of <code>path</code> existed and were directories.
     */

    return this.lookupDirect(path);
  }

  private <T extends FSReference> Option<T> lookupDirect(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    /**
     * Check the list of archives for <code>path</code>.
     */

    boolean lookup_shadowed = false;
    boolean lookup_first = true;
    final Iterator<Archive<?>> iter = this.archive_list.iterator();
    while (iter.hasNext() && (!lookup_shadowed)) {
      final Archive<?> a = iter.next();
      final PathVirtual mount = a.getMountPath();

      if (mount.isAncestorOf(path) || mount.equals(path)) {
        final PathVirtual a_path = path.subtract(mount);

        try {
          final Option<FileReference<?>> r =
            Filesystem.lookupDirectInArchive(a, a_path);

          switch (r.type) {
            case OPTION_NONE:
            {
              /**
               * This archive did not contain the requested path.
               */
              break;
            }
            case OPTION_SOME:
            {
              /**
               * This archive contained the requested path, return it.
               */

              final Some<FileReference<?>> s =
                (Option.Some<FileReference<?>>) r;
              @SuppressWarnings("unchecked") final T f =
                (T) new FSReferenceArchive(s.value);
              return new Option.Some<T>(f);
            }
          }
        } catch (final FilesystemError e) {

          /**
           * The path checked has an ancestor that happens to be a file in
           * this archive. If this is the first archive that has been queried,
           * then raise an error. Otherwise, don't check any further archives.
           */

          if (e.getCode() == Code.FS_ERROR_NOT_A_DIRECTORY) {
            if (lookup_first) {
              throw e;
            }
            lookup_shadowed = true;
          }
        }

        lookup_first = false;
      }
    }

    /**
     * No archive contained <code>path</code>. Check the list of virtual
     * directories.
     */

    if (this.directories.containsKey(path)) {
      final FSReferenceVirtualDirectory r =
        new FSReferenceVirtualDirectory(path, this.directories.get(path));
      @SuppressWarnings("unchecked") final Some<T> ro =
        new Option.Some<T>((T) r);
      return ro;
    }

    /**
     * No object exists at <code>path</code>.
     */

    return new Option.None<T>();
  }

  /**
   * Assert that <code>path</code> is a directory. The ancestors of
   * <code>path</code> are not checked.
   * 
   * @param path
   *          The path to check.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li><code>path</code> does not exist</li>
   *           <li><code>path</code> is not a directory</li>
   *           </ul>
   * @throws ConstraintError
   */

  private <T extends FSReference> void lookupDirectAssertIsDirectory(
    final PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final Option<T> r = this.lookupDirect(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<? extends FSReference> s =
          (Option.Some<? extends FSReference>) r;

        switch (s.value.type) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) s.value;
            if (ra.ref.type != Type.TYPE_DIRECTORY) {
              throw FilesystemError.notDirectory(path.toString());
            }
            break;
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            break;
          }
        }
      }
    }
  }

  @Override public void mountArchive(
    final @Nonnull String archive,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(archive, "Archive name");
    Constraints.constrainNotNull(mount, "Mount path");
    Constraints.constrainArbitrary(
      Name.isValid(archive),
      "Archive name is valid");

    this.log_mount.info("mount-archive: " + archive + " - " + mount);

    switch (this.archives.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.archiveNoDirectory(archive);
      }
      case OPTION_SOME:
      {
        final Some<PathReal> base = (Option.Some<PathReal>) this.archives;
        final File real = new File(new File(base.value.toString()), archive);

        if (real.exists() == false) {
          throw FilesystemError.archiveNonexistent(archive.toString());
        }

        this.mountInternal(new PathReal(real.toString()), mount);
      }
    }
  }

  @Override public void mountArchiveFromAnywhere(
    final @Nonnull File archive,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(archive, "Archive path");
    Constraints.constrainNotNull(mount, "Mount path");

    this.log_mount.info("mount-archive: " + archive + " - " + mount);

    if (archive.exists() == false) {
      throw FilesystemError.archiveNonexistent(archive.toString());
    }

    this.mountInternal(new PathReal(archive.toString()), mount);
  }

  private void mountCheckArchiveStack(
    final @Nonnull PathReal archive,
    final @Nonnull PathVirtual mount)
    throws FilesystemError
  {
    for (final Archive<?> a : this.archive_list) {
      final PathReal a_p = a.getRealPath();
      final PathVirtual a_m = a.getMountPath();

      if (a_p.equals(archive) && a_m.equals(mount)) {
        final String a_name = archive.toFile().getName();
        final String m_name = mount.toString();
        throw FilesystemError.archiveAlreadyMounted(a_name, m_name);
      }
    }
  }

  @Override public void mountClasspathArchive(
    final @Nonnull Class<?> c,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(c, "Class");
    Constraints.constrainNotNull(mount, "Mount path");

    this.log_mount.info("mount-classpath-archive: " + c + " - " + mount);

    Constraints.constrainNotNull(c, "Class");
    final String cname = c.getCanonicalName();
    Constraints.constrainNotNull(cname, "Class canonical name");

    final String cname_s = cname.replace('.', '/');
    final String cname_k = cname_s + ".class";

    final ClassLoader loader = c.getClassLoader();
    final URL url = loader.getResource(cname_k);

    this.log_mount.debug("mount-classpath-archive: url " + url);
    final String mount_path =
      ClassURIHandling.getClassContainerPath(url, cname_k);
    this.log_mount.debug("mount-classpath-archive: actual " + mount_path);

    this.mountInternal(new PathReal(mount_path), mount);
  }

  private void mountInternal(
    final @Nonnull PathReal archive,
    final @Nonnull PathVirtual mount)
    throws FilesystemError,
      ConstraintError
  {
    this.mountCheckArchiveStack(archive, mount);

    final ArchiveHandler<?> handler = this.mountInternalCheckHandler(archive);
    final Option<FSReference> r = this.lookup(mount);
    switch (r.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(mount.toString());
      }
      case OPTION_SOME:
      {
        final Some<FSReference> s = (Option.Some<FSReference>) r;
        switch (s.value.type) {
          case FS_REF_ARCHIVE:
          {
            this.createDirectory(mount);
            this.mountInternalActual(handler, archive, mount);
            return;
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            this.mountInternalActual(handler, archive, mount);
            return;
          }
        }
      }
    }
  }

  private void mountInternalActual(
    final @Nonnull ArchiveHandler<?> handler,
    final @Nonnull PathReal archive,
    final @Nonnull PathVirtual mount)
    throws FilesystemError,
      ConstraintError
  {
    final Archive<?> a = handler.load(this.log, archive, mount);
    this.archive_list.addFirst(a);
  }

  /**
   * Find an archive handler that can handle <code>archive</code>, or raise an
   * error.
   */

  private @Nonnull ArchiveHandler<?> mountInternalCheckHandler(
    final @Nonnull PathReal archive)
    throws FilesystemError
  {
    for (final ArchiveHandler<?> handler : this.handlers) {
      if (handler.canHandle(archive)) {
        return handler;
      }
    }

    throw FilesystemError.archiveTypeUnsupported(archive.toString());
  }

  @Override public @Nonnull InputStream openFile(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    Constraints.constrainNotNull(path, "Path");

    final Option<? extends FSReference> r = this.lookup(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<? extends FSReference> s =
          (Option.Some<? extends FSReference>) r;

        switch (s.value.type) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) s.value;
            switch (ra.ref.type) {
              case TYPE_DIRECTORY:
              {
                throw FilesystemError.notFile(path.toString());
              }
              case TYPE_FILE:
              {
                final Archive<?> a = ra.ref.archive;
                return a.openFile(path.subtract(a.getMountPath()));
              }
            }

            throw new UnreachableCodeException();
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            throw FilesystemError.notFile(path.toString());
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public void unmount(
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(mount, "Path");

    this.log_mount.info("unmount: " + mount);

    /**
     * Check to see if the mount point could possibly be mounted. It's
     * possible for files from archives mounted below the mount point M to
     * "shadow" M.
     */

    if (this.isDirectory(mount) == false) {
      throw FilesystemError.notDirectory(mount.toString());
    }

    final Iterator<Archive<?>> iter = this.archive_list.iterator();
    while (iter.hasNext()) {
      final Archive<?> a = iter.next();
      if (a.getMountPath().equals(mount)) {
        a.close();
        iter.remove();
        break;
      }
    }
  }

  @Override public void updateModificationTime(
    final @Nonnull PathVirtual path,
    final @Nonnull Calendar t)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(path, "Path");

    final Calendar ct = this.getModificationTimeActual(path);
    this.time_updates.put(path, new UpdateTimeEntry(ct, t));
  }
}
