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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Pair;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FileReference.Type;

/**
 * <p>
 * The basic filesystem implementation.
 * </p>
 */

public final class Filesystem implements
  FSCapabilityCreateDirectory,
  FSCapabilityRead,
  FSCapabilityMountDirectory,
  FSCapabilityMountClasspath
{
  private static abstract class FSReference
  {
    final @Nonnull FSReferenceType type;

    FSReference(
      final @Nonnull FSReferenceType type)
    {
      this.type = type;
    }
  }

  private static final class FSReferenceArchive extends FSReference
  {
    final @Nonnull FileReference<?> ref;

    FSReferenceArchive(
      final @Nonnull FileReference<?> ref)
    {
      super(FSReferenceType.FS_REF_ARCHIVE);
      this.ref = ref;
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

    FSReferenceVirtualDirectory(
      final @Nonnull PathVirtual path)
    {
      super(FSReferenceType.FS_REF_VIRTUAL_DIRECTORY);
      this.path = path;
    }
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
   * Look up the object at <code>path</code> in the mounted archives
   * <code>mount_map</code>. Only mount points that are ancestors of or equal
   * to <code>path</code> are checked.
   * </p>
   * <p>
   * The ancestors of <code>path</code> are not checked.
   * </p>
   * 
   * @throws ConstraintError
   * @throws FilesystemError
   */

  private static @CheckForNull
    <T extends FSReference>
    Option<T>
    lookupDirectInMounts(
      final @Nonnull Map<PathVirtual, Stack<Archive<?>>> mount_map,
      final @Nonnull PathVirtual path)
      throws FilesystemError,
        ConstraintError
  {
    final Set<Entry<PathVirtual, Stack<Archive<?>>>> mount_entries =
      mount_map.entrySet();

    for (final Entry<PathVirtual, Stack<Archive<?>>> entry : mount_entries) {
      final PathVirtual mount = entry.getKey();

      if (mount.isAncestorOf(path) || mount.equals(path)) {
        final Stack<Archive<?>> stack = entry.getValue();
        final Option<T> r =
          Filesystem.lookupDirectInStack(path, stack, mount);
        if (r.isSome()) {
          return r;
        }
      }
    }

    return new Option.None<T>();
  }

  /**
   * <p>
   * Look up the object at <code>path</code> in the stack of archives
   * <code>stack</code>.
   * </p>
   * <p>
   * The ancestors of <code>path</code> are not checked.
   * </p>
   * 
   * @throws ConstraintError
   * @throws FilesystemError
   */

  private static @Nonnull
    <T extends FSReference>
    Option<T>
    lookupDirectInStack(
      final @Nonnull PathVirtual path,
      final @Nonnull Stack<Archive<?>> stack,
      final @Nonnull PathVirtual mount)
      throws FilesystemError,
        ConstraintError
  {
    assert stack != null;
    assert stack.size() > 0;

    /**
     * Check each archive in the stack, from the top of the stack to the
     * bottom.
     */

    final int size = stack.size();
    for (int index = size - 1; index >= 0; --index) {
      final Archive<?> a = stack.get(index);

      final Option<FileReference<?>> r =
        Filesystem.lookupDirectInArchive(a, path.subtract(mount));
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

          final Some<FileReference<?>> s = (Option.Some<FileReference<?>>) r;
          @SuppressWarnings("unchecked") final T f =
            (T) new FSReferenceArchive(s.value);
          return new Option.Some<T>(f);
        }
      }
    }

    return new Option.None<T>();
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

  private final @Nonnull Log                                 log;
  private final @Nonnull Log                                 log_directory;
  private final @Nonnull Log                                 log_mount;

  private final @Nonnull Option<PathReal>                    archives;
  private final @Nonnull List<ArchiveHandler<?>>             handlers;
  private final @Nonnull Map<PathVirtual, Stack<Archive<?>>> mounts;
  private final @Nonnull Set<PathVirtual>                    directories;

  private Filesystem(
    final @Nonnull Log log,
    final @CheckForNull PathReal archives)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(log, "Log interface"),
        "filesystem");
    this.log_directory = new Log(this.log, "directory");
    this.log_mount = new Log(this.log, "mount");

    this.archives =
      archives == null
        ? new Option.None<PathReal>()
        : new Option.Some<PathReal>(archives);

    this.handlers = new ArrayList<ArchiveHandler<?>>();
    this.handlers.add(new ArchiveDirectoryHandler());
    this.handlers.add(new ArchiveZipHandler());

    this.mounts = new HashMap<PathVirtual, Stack<Archive<?>>>();

    this.directories = new HashSet<PathVirtual>();
    this.directories.add(PathVirtual.ROOT);
  }

  /**
   * <p>
   * Return <code>(Some(stack), TRUE)</code> if the archive named
   * <code>archive_name</code> is mounted at <code>mount</code>.
   * </p>
   * <p>
   * Return <code>(Some(stack), FALSE)</code> if there is an archive stack at
   * <code>mount</code> but no archive named <code>archive_name</code> is in
   * it.
   * </p>
   * <p>
   * Return <code>(None, _)</code> if nothing is mounted at <code>mount</code>
   * .
   * </p>
   */

  private @Nonnull Pair<Option<Stack<Archive<?>>>, Boolean> archiveMountedAt(
    final @Nonnull PathReal archive_name,
    final @Nonnull PathVirtual mount)
  {
    if (this.mounts.containsKey(mount)) {
      final Stack<Archive<?>> stack = this.mounts.get(mount);
      final Option<Stack<Archive<?>>> ss =
        new Option.Some<Stack<Archive<?>>>(stack);

      for (final Archive<?> archive : stack) {
        if (archive.getRealPath().equals(archive_name)) {
          return new Pair<Option<Stack<Archive<?>>>, Boolean>(
            ss,
            Boolean.TRUE);
        }
      }
      return new Pair<Option<Stack<Archive<?>>>, Boolean>(ss, Boolean.FALSE);
    }

    return new Pair<Option<Stack<Archive<?>>>, Boolean>(
      new Option.None<Stack<Archive<?>>>(),
      Boolean.FALSE);
  }

  @Override public void createDirectory(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    Constraints.constrainNotNull(path, "Path");

    this.log_directory.debug("create-directory: " + path.toString());

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
        this.directories.add(path);
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
                this.directories.add(path);
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
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull Calendar getModificationTime(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    throw new UnimplementedCodeException();
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
    /**
     * Check that all ancestors of <code>path</code> exist and are
     * directories.
     */

    final PathVirtualEnum e = new PathVirtualEnum(path);
    while (e.hasMoreElements()) {
      final PathVirtual ancestor = e.nextElement();
      final Option<T> r = this.lookupDirect(ancestor);
      switch (r.type) {
        case OPTION_NONE:
        {
          throw FilesystemError.fileNotFound(ancestor.toString());
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
                throw FilesystemError.notDirectory(ancestor.toString());
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
     * Check mounts for path.
     */

    final Option<T> r = Filesystem.lookupDirectInMounts(this.mounts, path);

    switch (r.type) {
      case OPTION_NONE:
      {
        /**
         * If no mount contains <code>path</code>, check the set of explicitly
         * created directories.
         */

        if (this.directories.contains(path)) {
          @SuppressWarnings("unchecked") final T f =
            (T) new FSReferenceVirtualDirectory(path);
          return new Option.Some<T>(f);
        }
        return new Option.None<T>();
      }
      case OPTION_SOME:
      {
        return r;
      }
    }

    throw new UnreachableCodeException();
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

    this.log_mount.debug("mount-archive: " + archive + " - " + mount);

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

  @Override public void mountClasspathArchive(
    final @Nonnull Class<?> c,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(c, "Class");
    Constraints.constrainNotNull(mount, "Mount path");

    this.log_mount.debug("mount-classpath-archive: " + c + " - " + mount);

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
    this.log_mount.debug("mount-internal: " + archive + " - " + mount);

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

  /**
   * Load the archive named <code>archive_name</code> with
   * <code>handler</code>, inserting it into the (possibly newly-created)
   * stack of archives at <code>mount</code>.
   */

  private void mountInternalActual(
    final @Nonnull ArchiveHandler<?> handler,
    final @Nonnull PathReal archive_name,
    final @Nonnull PathVirtual mount)
    throws FilesystemError,
      ConstraintError
  {
    final Stack<Archive<?>> stack =
      this.mountInternalGetStack(archive_name, mount);

    final Archive<?> archive = handler.load(archive_name, mount);
    stack.push(archive);
    this.mounts.put(mount, stack);
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

  /**
   * <p>
   * Return the stack of archives at <code>mount</code>. If an archive named
   * <code>archive_name</code> is already in that stack, raise an error.
   * </p>
   * 
   * <p>
   * If no stack of archives exists at <code>mount</code>, create a new stack
   * and return it.
   * </p>
   */

  private @Nonnull Stack<Archive<?>> mountInternalGetStack(
    final PathReal archive_name,
    final PathVirtual mount)
    throws FilesystemError
  {
    final Pair<Option<Stack<Archive<?>>>, Boolean> r =
      this.archiveMountedAt(archive_name, mount);

    if (r.first.isSome()) {
      if (r.second.booleanValue()) {
        throw FilesystemError.archiveAlreadyMounted(
          archive_name.toString(),
          mount.toString());
      }

      final Stack<Archive<?>> stack =
        ((Option.Some<Stack<Archive<?>>>) r.first).value;
      return stack;
    }

    return new Stack<Archive<?>>();
  }

  @Override public @Nonnull InputStream openFile(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    Constraints.constrainNotNull(path, "Path");

    throw new UnimplementedCodeException();
  }
}
