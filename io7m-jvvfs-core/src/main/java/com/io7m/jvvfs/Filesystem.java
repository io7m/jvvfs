/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
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
 * @see #makeWithArchiveDirectory(LogUsableType, PathReal)
 * @see #makeWithoutArchiveDirectory(LogUsableType)
 */

@SuppressWarnings("synthetic-access") public final class Filesystem implements
  FilesystemType
{
  private static abstract class FSReference
  {
    private final FSReferenceType type;

    FSReference(
      final FSReferenceType in_type)
    {
      this.type = in_type;
    }

    final FSReferenceType getType()
    {
      return this.type;
    }
  }

  private static final class FSReferenceArchive extends FSReference
  {
    private final FileReference<?> ref;

    FSReferenceArchive(
      final FileReference<?> in_ref)
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
    private final Calendar    mtime;
    private final PathVirtual path;

    FSReferenceVirtualDirectory(
      final PathVirtual in_path,
      final Calendar in_mtime)
    {
      super(FSReferenceType.FS_REF_VIRTUAL_DIRECTORY);
      this.path = in_path;
      this.mtime = in_mtime;
    }
  }

  private static class UpdateTimeEntry
  {
    private final Calendar time_value;
    private final Calendar time_when_updated;

    UpdateTimeEntry(
      final Calendar in_time_when_updated,
      final Calendar in_time_value)
    {
      this.time_when_updated = in_time_when_updated;
      this.time_value = in_time_value;
    }
  }

  private static final Boolean FALSE_NOT_NULL = NullCheck
                                                .notNull(Boolean.FALSE);

  private static final Boolean TRUE_NOT_NULL  = NullCheck
                                                .notNull(Boolean.TRUE);

  private static Calendar getUTCTimeNow()
  {
    final TimeZone utc = TimeZone.getTimeZone("UTC");
    final Calendar ci =
      Calendar.getInstance(NullCheck.notNull(utc, "UTC time zone"));
    assert ci != null;
    return ci;
  }

  @SuppressWarnings("unchecked") private static
    OptionType<FileReference<?>>
    lookupDirectInArchive(
      final Archive<?> a,
      final PathVirtual path)
      throws FilesystemError
  {
    final OptionType<?> r = a.lookup(path);
    return (OptionType<FileReference<?>>) r;
  }

  /**
   * <p>
   * Construct a filesystem using <code>archives</code> as the location of the
   * archive directory.
   * </p>
   * 
   * @param log
   *          The log interface
   * @param archives
   *          The archive directory
   * @return A new filesystem
   */

  public static FilesystemType makeWithArchiveDirectory(
    final LogUsableType log,
    final PathReal archives)
  {
    NullCheck.notNull(archives, "Archive directory");
    return new Filesystem(log, archives);
  }

  /**
   * <p>
   * Construct a filesystem without an archive directory. A filesystem
   * constructed in this manner may only access archives on the classpath.
   * </p>
   * 
   * @param log
   *          The log interface
   * @return A new filesystem
   */

  public static FilesystemType makeWithoutArchiveDirectory(
    final LogUsableType log)
  {
    return new Filesystem(log, null);
  }

  private final Deque<Archive<?>>                 archive_list;
  private final OptionType<PathReal>              archives;
  private final Map<PathVirtual, Calendar>        directories;
  private final List<ArchiveHandler<?>>           handlers;
  private final LogUsableType                     log;
  private final LogUsableType                     log_directory;
  private final LogUsableType                     log_lookup;
  private final LogUsableType                     log_mount;
  private final Map<PathVirtual, UpdateTimeEntry> time_updates;

  private Filesystem(
    final LogUsableType in_log,
    final @Nullable PathReal in_archives)
  {
    this.log = NullCheck.notNull(in_log, "Log").with("filesystem");
    this.log_directory = this.log.with("directory");
    this.log_mount = this.log.with("mount");
    this.log_lookup = this.log.with("lookup");
    this.archives = Option.of(in_archives);

    this.handlers = new ArrayList<ArchiveHandler<?>>();
    this.handlers.add(new ArchiveDirectoryHandler());
    this.handlers.add(new ArchiveZipHandler());

    this.archive_list = new LinkedList<Archive<?>>();

    this.directories = new HashMap<PathVirtual, Calendar>();
    this.directories.put(PathVirtual.ROOT, Filesystem.getUTCTimeNow());
    this.time_updates = new HashMap<PathVirtual, UpdateTimeEntry>();
  }

  @Override public void close()
    throws FilesystemError
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
    final PathVirtual path)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");

    this.log_directory.info("create-directory: " + path.toString());

    final PathVirtualEnum e = PathVirtualEnum.enumerate(path);
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

  private <T extends FSReference> void createDirectoryDirect(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<T> r = this.lookup(path);

    r.acceptPartial(new OptionPartialVisitorType<T, Unit, FilesystemError>() {
      @Override public Unit none(
        final None<T> n)
        throws FilesystemError
      {
        Filesystem.this.directories.put(path, Filesystem.getUTCTimeNow());
        return Unit.unit();
      }

      @Override public Unit some(
        final Some<T> s)
        throws FilesystemError
      {
        final T rf = s.get();
        switch (rf.getType()) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) rf;
            switch (ra.ref.getType()) {
              case TYPE_DIRECTORY:
              {
                Filesystem.this.directories.put(
                  path,
                  Filesystem.getUTCTimeNow());
                break;
              }
              case TYPE_FILE:
              {
                throw FilesystemError.notDirectory(path.toString());
              }
            }
            break;
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            break;
          }
        }

        return Unit.unit();
      }
    });
  }

  @Override public boolean exists(
    final PathVirtual path)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");
    final Boolean r =
      this.lookup(path).accept(
        new OptionVisitorType<Filesystem.FSReference, Boolean>() {
          @SuppressWarnings("null") @Override public Boolean none(
            final None<FSReference> n)
          {
            return Boolean.FALSE;
          }

          @SuppressWarnings("null") @Override public Boolean some(
            final Some<FSReference> s)
          {
            return Boolean.TRUE;
          }
        });
    return r.booleanValue();
  }

  @Override public long getFileSize(
    final PathVirtual path)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");
    return this.getFileSizeActual(path).longValue();
  }

  private <T extends FSReference> Long getFileSizeActual(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<T> r = this.lookup(path);
    return r
      .acceptPartial(new OptionPartialVisitorType<T, Long, FilesystemError>() {
        @Override public Long none(
          final None<T> n)
          throws FilesystemError
        {
          throw FilesystemError.fileNotFound(path.toString());
        }

        @Override public Long some(
          final Some<T> s)
          throws FilesystemError
        {
          final T rr = s.get();
          switch (rr.getType()) {
            case FS_REF_ARCHIVE:
            {
              final FSReferenceArchive ra = (FSReferenceArchive) rr;
              switch (ra.ref.getType()) {
                case TYPE_DIRECTORY:
                {
                  throw FilesystemError.notFile(path.toString());
                }
                case TYPE_FILE:
                {
                  final Archive<?> a = ra.ref.getArchive();

                  /*
                   * XXX: Under what conditions will an archive be null?
                   */

                  assert a != null;

                  final Long q =
                    Long.valueOf(a.getFileSize(path.subtract(a.getMountPath())));
                  assert q != null;
                  return q;
                }
              }

              throw new UnreachableCodeException();
            }
            case FS_REF_VIRTUAL_DIRECTORY:
            {
              throw FilesystemError.notFile(path.toString());
            }
          }

          throw new UnreachableCodeException();
        }
      });
  }

  @Override public Calendar getModificationTime(
    final PathVirtual path)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");
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

  private <T extends FSReference> Calendar getModificationTimeActual(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<T> r = this.lookup(path);
    return r
      .acceptPartial(new OptionPartialVisitorType<T, Calendar, FilesystemError>() {
        @Override public Calendar none(
          final None<T> n)
          throws FilesystemError
        {
          throw FilesystemError.fileNotFound(path.toString());
        }

        @Override public Calendar some(
          final Some<T> s)
          throws FilesystemError
        {
          final T rr = s.get();
          switch (rr.getType()) {
            case FS_REF_ARCHIVE:
            {
              final FSReferenceArchive ra = (FSReferenceArchive) rr;
              final Archive<?> a = ra.ref.getArchive();
              /*
               * XXX: Under what conditions can getArchive() return null?
               */
              assert a != null;
              return a.getModificationTime(path.subtract(a.getMountPath()));
            }
            case FS_REF_VIRTUAL_DIRECTORY:
            {
              final FSReferenceVirtualDirectory rvd =
                (FSReferenceVirtualDirectory) rr;
              return rvd.mtime;
            }
          }

          throw new UnreachableCodeException();
        }
      });
  }

  @Override public Deque<Pair<PathReal, PathVirtual>> getMountedArchives()
  {
    final Deque<Pair<PathReal, PathVirtual>> result =
      new ArrayDeque<Pair<PathReal, PathVirtual>>();

    final Iterator<Archive<?>> iter = this.archive_list.descendingIterator();
    while (iter.hasNext()) {
      final Archive<?> a = iter.next();
      final Pair<PathReal, PathVirtual> p =
        Pair.pair(a.getRealPath(), a.getMountPath());
      result.push(p);
    }

    return result;
  }

  @Override public boolean isDirectory(
    final PathVirtual path)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");
    return this.isDirectoryActual(path).booleanValue();
  }

  private <T extends FSReference> Boolean isDirectoryActual(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<T> r = this.lookup(path);
    return r
      .acceptPartial(new OptionPartialVisitorType<T, Boolean, FilesystemError>() {
        @Override public Boolean none(
          final None<T> n)
          throws FilesystemError
        {
          return Filesystem.FALSE_NOT_NULL;
        }

        @Override public Boolean some(
          final Some<T> s)
          throws FilesystemError
        {
          final T q = s.get();
          switch (q.getType()) {
            case FS_REF_ARCHIVE:
            {
              final FSReferenceArchive ra = (FSReferenceArchive) q;
              final Boolean rq =
                Boolean.valueOf(ra.ref.getType() == Type.TYPE_DIRECTORY);
              assert rq != null;
              return rq;
            }
            case FS_REF_VIRTUAL_DIRECTORY:
            {
              return Filesystem.TRUE_NOT_NULL;
            }
          }

          throw new UnreachableCodeException();
        }
      });
  }

  @Override public boolean isFile(
    final PathVirtual path)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");
    this.log_lookup.info("is-file: " + path);
    return this.isFileActual(path).booleanValue();
  }

  private <T extends FSReference> Boolean isFileActual(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<T> r = this.lookup(path);
    return r
      .acceptPartial(new OptionPartialVisitorType<T, Boolean, FilesystemError>() {
        @Override public Boolean none(
          final None<T> n)
          throws FilesystemError
        {
          return Filesystem.FALSE_NOT_NULL;
        }

        @Override public Boolean some(
          final Some<T> s)
          throws FilesystemError
        {
          final T rr = s.get();
          switch (rr.getType()) {
            case FS_REF_ARCHIVE:
            {
              final FSReferenceArchive ra = (FSReferenceArchive) rr;
              final Boolean b =
                Boolean.valueOf(ra.ref.getType() == Type.TYPE_FILE);
              assert b != null;
              return b;
            }
            case FS_REF_VIRTUAL_DIRECTORY:
            {
              return Filesystem.FALSE_NOT_NULL;
            }
          }

          throw new UnreachableCodeException();
        }
      });
  }

  @Override public SortedSet<String> listDirectory(
    final PathVirtual path)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");

    this.log_lookup.info("is-directory: " + path);

    final OptionType<FSReference> ro = this.lookup(path);
    return ro
      .acceptPartial(new OptionPartialVisitorType<Filesystem.FSReference, SortedSet<String>, FilesystemError>() {
        @Override public SortedSet<String> none(
          final None<FSReference> n)
          throws FilesystemError
        {
          throw FilesystemError.fileNotFound(path.toString());
        }

        @Override public SortedSet<String> some(
          final Some<FSReference> s)
          throws FilesystemError
        {
          final FSReference ref = s.get();
          switch (ref.getType()) {
            case FS_REF_ARCHIVE:
            {
              final FSReferenceArchive ra = (FSReferenceArchive) ref;
              if (ra.ref.getType() != Type.TYPE_DIRECTORY) {
                throw FilesystemError.notDirectory(path.toString());
              }
              return Filesystem.this.listDirectoryInternal(path);
            }
            case FS_REF_VIRTUAL_DIRECTORY:
            {
              return Filesystem.this.listDirectoryInternal(path);
            }
          }

          throw new UnreachableCodeException();
        }
      });
  }

  private SortedSet<String> listDirectoryInternal(
    final PathVirtual path)
    throws FilesystemError
  {
    final SortedSet<String> items = new TreeSet<String>();

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
      assert d != null;

      if (path.isParentOf(d)) {
        final OptionType<String> name_o = d.getBaseName();
        name_o.accept(new OptionVisitorType<String, Unit>() {
          @Override public Unit none(
            final None<String> n)
          {
            /**
             * If path is a parent of d, then d cannot be root.
             */

            throw new UnreachableCodeException();
          }

          @Override public Unit some(
            final Some<String> s)
          {
            items.add(s.get());
            return Unit.unit();
          }
        });
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

  private <T extends FSReference> OptionType<T> lookup(
    final PathVirtual path)
    throws FilesystemError
  {
    this.log_lookup.debug(path.toString());

    /**
     * Check that all ancestors of <code>path</code> exist and are
     * directories.
     */

    final PathVirtualEnum e = PathVirtualEnum.enumerate(path);
    while (e.hasMoreElements()) {
      final PathVirtual ancestor = e.nextElement();
      this.lookupDirectAssertIsDirectory(ancestor);
    }

    /**
     * All ancestors of <code>path</code> existed and were directories.
     */

    return this.lookupDirect(path);
  }

  private <T extends FSReference> OptionType<T> lookupDirect(
    final PathVirtual path)
    throws FilesystemError
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
          final OptionType<FileReference<?>> r =
            Filesystem.lookupDirectInArchive(a, a_path);

          if (r.isSome()) {

            /**
             * This archive contained the requested path, return it.
             */

            return r
              .mapPartial(new PartialFunctionType<FileReference<?>, T, FilesystemError>() {
                @Override public T call(
                  final FileReference<?> x)
                  throws FilesystemError
                {
                  @SuppressWarnings("unchecked") final T y =
                    (T) new FSReferenceArchive(x);
                  return y;
                }
              });
          }

          /**
           * This archive did not contain the requested path.
           */

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
      final Calendar d = this.directories.get(path);
      assert d != null;
      final FSReferenceVirtualDirectory r =
        new FSReferenceVirtualDirectory(path, d);
      @SuppressWarnings("unchecked") final OptionType<T> rt =
        (OptionType<T>) Option.some(r);
      return rt;
    }

    /**
     * No object exists at <code>path</code>.
     */

    return Option.none();
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
    throws FilesystemError
  {
    final OptionType<T> r = this.lookupDirect(path);
    r.acceptPartial(new OptionPartialVisitorType<T, Unit, FilesystemError>() {
      @Override public Unit none(
        final None<T> n)
        throws FilesystemError
      {
        throw FilesystemError.fileNotFound(path.toString());
      }

      @Override public Unit some(
        final Some<T> s)
        throws FilesystemError
      {
        final T x = s.get();
        switch (x.getType()) {
          case FS_REF_ARCHIVE:
          {
            final FSReferenceArchive ra = (FSReferenceArchive) x;
            if (ra.ref.getType() != Type.TYPE_DIRECTORY) {
              throw FilesystemError.notDirectory(path.toString());
            }
            break;
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            break;
          }
        }

        return Unit.unit();
      }
    });
  }

  @Override public void mountArchive(
    final String archive,
    final PathVirtual mount)
    throws FilesystemError
  {
    NullCheck.notNull(archive, "Archive name");
    NullCheck.notNull(mount, "Mount path");

    if (Name.isValid(archive) == false) {
      throw new FilesystemError(
        Code.FS_ERROR_CONSTRAINT_ERROR,
        "Archive name is not valid");
    }

    this.log_mount.info("mount-archive: " + archive + " - " + mount);

    this.archives
      .acceptPartial(new OptionPartialVisitorType<PathReal, Unit, FilesystemError>() {
        @Override public Unit none(
          final None<PathReal> n)
          throws FilesystemError
        {
          throw FilesystemError.archiveNoDirectory(archive);
        }

        @Override public Unit some(
          final Some<PathReal> s)
          throws FilesystemError
        {
          final File real = new File(new File(s.get().toString()), archive);

          if (real.exists() == false) {
            throw FilesystemError.archiveNonexistent(archive);
          }

          Filesystem.this.mountInternal(new PathReal(real), mount);
          return Unit.unit();
        }
      });
  }

  @Override public void mountArchiveFromAnywhere(
    final File archive,
    final PathVirtual mount)
    throws FilesystemError
  {
    NullCheck.notNull(archive, "Archive path");
    NullCheck.notNull(mount, "Mount path");

    this.log_mount.info("mount-archive: " + archive + " - " + mount);

    if (archive.exists() == false) {
      final String as = archive.toString();
      assert as != null;
      throw FilesystemError.archiveNonexistent(as);
    }

    this.mountInternal(new PathReal(archive), mount);
  }

  private void mountCheckArchiveStack(
    final PathReal archive,
    final PathVirtual mount)
    throws FilesystemError
  {
    for (final Archive<?> a : this.archive_list) {
      final PathReal a_p = a.getRealPath();
      final PathVirtual a_m = a.getMountPath();

      if (a_p.equals(archive) && a_m.equals(mount)) {
        final String a_name = archive.toFile().getName();
        final String m_name = mount.toString();
        assert a_name != null;
        throw FilesystemError.archiveAlreadyMounted(a_name, m_name);
      }
    }
  }

  @Override public void mountClasspathArchive(
    final Class<?> c,
    final PathVirtual mount)
    throws FilesystemError
  {
    NullCheck.notNull(c, "Class");
    NullCheck.notNull(mount, "Mount path");

    this.log_mount.info("mount-classpath-archive: " + c + " - " + mount);

    NullCheck.notNull(c, "Class");
    final String cname = c.getCanonicalName();
    NullCheck.notNull(cname, "Class canonical name");

    final String cname_s = cname.replace('.', '/');
    final String cname_k = cname_s + ".class";

    final ClassLoader loader = c.getClassLoader();
    final URL url = loader.getResource(cname_k);
    this.log_mount.debug("mount-classpath-archive: url " + url);

    final String mount_path =
      ClassURIHandling.getClassContainerPath(
        NullCheck.notNull(url, "URL from ClassLoader"),
        cname_k);
    this.log_mount.debug("mount-classpath-archive: actual " + mount_path);

    this.mountInternal(new PathReal(mount_path), mount);
  }

  private <T extends FSReference> void mountInternal(
    final PathReal archive,
    final PathVirtual mount)
    throws FilesystemError
  {
    this.mountCheckArchiveStack(archive, mount);

    final ArchiveHandler<?> handler = this.mountInternalCheckHandler(archive);
    final OptionType<T> r = this.lookup(mount);

    r.acceptPartial(new OptionPartialVisitorType<T, Unit, FilesystemError>() {
      @Override public Unit none(
        final None<T> n)
        throws FilesystemError
      {
        throw FilesystemError.fileNotFound(mount.toString());
      }

      @Override public Unit some(
        final Some<T> s)
        throws FilesystemError
      {
        switch (s.get().getType()) {
          case FS_REF_ARCHIVE:
          {
            Filesystem.this.createDirectory(mount);
            Filesystem.this.mountInternalActual(handler, archive, mount);
            return Unit.unit();
          }
          case FS_REF_VIRTUAL_DIRECTORY:
          {
            Filesystem.this.mountInternalActual(handler, archive, mount);
            return Unit.unit();
          }
        }

        throw new UnreachableCodeException();
      }
    });
  }

  private void mountInternalActual(
    final ArchiveHandler<?> handler,
    final PathReal archive,
    final PathVirtual mount)
    throws FilesystemError
  {
    final Archive<?> a = handler.load(this.log, archive, mount);
    this.archive_list.addFirst(a);
  }

  /**
   * Find an archive handler that can handle <code>archive</code>, or raise an
   * error.
   */

  private ArchiveHandler<?> mountInternalCheckHandler(
    final PathReal archive)
    throws FilesystemError
  {
    for (final ArchiveHandler<?> handler : this.handlers) {
      if (handler.canHandle(archive)) {
        return handler;
      }
    }

    throw FilesystemError.archiveTypeUnsupported(archive.toString());
  }

  @Override public InputStream openFile(
    final PathVirtual path)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");
    return this.openFileActual(path);
  }

  private <T extends FSReference> InputStream openFileActual(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<T> r = this.lookup(path);
    return r
      .acceptPartial(new OptionPartialVisitorType<T, InputStream, FilesystemError>() {
        @Override public InputStream none(
          final None<T> n)
          throws FilesystemError
        {
          throw FilesystemError.fileNotFound(path.toString());
        }

        @Override public InputStream some(
          final Some<T> s)
          throws FilesystemError
        {
          switch (s.get().getType()) {
            case FS_REF_ARCHIVE:
            {
              final FSReferenceArchive ra = (FSReferenceArchive) s.get();
              switch (ra.ref.getType()) {
                case TYPE_DIRECTORY:
                {
                  throw FilesystemError.notFile(path.toString());
                }
                case TYPE_FILE:
                {
                  final Archive<?> a = ra.ref.getArchive();

                  /*
                   * XXX: Under what conditions can getArchive() return null?
                   */

                  assert a != null;
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

          throw new UnreachableCodeException();
        }
      });
  }

  @Override public void unmount(
    final PathVirtual mount)
    throws FilesystemError
  {
    NullCheck.notNull(mount, "Path");

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
    final PathVirtual path,
    final Calendar t)
    throws FilesystemError
  {
    NullCheck.notNull(path, "Path");

    final Calendar ct = this.getModificationTimeActual(path);
    this.time_updates.put(path, new UpdateTimeEntry(ct, t));
  }
}
