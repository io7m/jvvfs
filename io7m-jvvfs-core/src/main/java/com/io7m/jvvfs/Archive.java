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

import java.io.InputStream;
import java.util.Calendar;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jlog.Log;

/**
 * <p>
 * An archive is an object in the operating system filesystem that can be
 * "mounted" in the virtual filesystem.
 * </p>
 * <p>
 * An archive mounted at mount point <code>m</code> makes its contents
 * available at paths prefixed with <code>m</code>. As an example, if an
 * archive contains the file <code>/x/y/z/file.txt</code> and the archive is
 * mounted at <code>/usr</code>, then the file is accessible via
 * <code>/usr/x/y/z/file.txt</code> in the virtual filesystem. The
 * implementation is responsible for converting virtual paths to
 * archive-relative paths. That is, if a user tries to open
 * <code>/usr/x/y/z/file.txt</code> in the example above, the filesystem is
 * responsible for translating that to <code>/x/y/z/file.txt</code> before
 * passing the path to the archive interface.
 * </p>
 */

abstract class Archive<T extends ArchiveKind>
{
  /**
   * <p>
   * Close the archive, freeing any resources used.
   * </p>
   */

  abstract void close()
    throws FilesystemError;

  /**
   * <p>
   * Retrieve the size of the file at <code>path</code>.
   * </p>
   * 
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a file</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  final long getFileSize(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final Option<FileReference<T>> ro = this.lookup(path);
    switch (ro.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<FileReference<T>> s = (Option.Some<FileReference<T>>) ro;
        final FileReference<T> ar = s.value;

        switch (s.value.type) {
          case TYPE_DIRECTORY:
          {
            throw FilesystemError.notFile(path.toString());
          }
          case TYPE_FILE:
          {
            return this.getFileSizeActual(ar);
          }
        }
        break;
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * <p>
   * Retrieve the size of the file at the given reference <code>r</code>.
   * </p>
   * 
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a file</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  abstract protected long getFileSizeActual(
    final @Nonnull FileReference<T> r)
    throws FilesystemError,
      ConstraintError;

  abstract protected @Nonnull Log getLogLookup();

  /**
   * <p>
   * Retrieve the modification time of the file or directory at
   * <code>path</code>.
   * </p>
   * 
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>r</code>.</li>
   *           <li>An I/O error occurs.</li>
   *           </ul>
   */

  final @Nonnull Calendar getModificationTime(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final Option<FileReference<T>> ro = this.lookup(path);
    switch (ro.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<FileReference<T>> s = (Option.Some<FileReference<T>>) ro;
        return this.getModificationTimeActual(s.value);
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * <p>
   * Retrieve the modification time of the object at the given reference
   * <code>r</code>.
   * </p>
   * 
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>r</code>.</li>
   *           <li>An I/O error occurs.</li>
   *           </ul>
   */

  abstract protected @Nonnull Calendar getModificationTimeActual(
    final @Nonnull FileReference<T> r);

  /**
   * <p>
   * The path at which the archive is mounted.
   * </p>
   */

  abstract @Nonnull PathVirtual getMountPath();

  /**
   * <p>
   * The real path of the archive.
   * </p>
   */

  abstract @Nonnull PathReal getRealPath();

  /**
   * <p>
   * List the contents of the directory at <code>path</code>.
   * </p>
   * 
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a directory</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  abstract @Nonnull Set<String> listDirectory(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError;

  /**
   * <p>
   * Retrieve a reference to the object at the given path. This is a
   * "primitive" operation; all other functions should use <code>lookup</code>
   * internally in order to provide consistent semantics.
   * </p>
   * 
   * @throws ConstraintError
   *           If <code>path == null</code>.
   */

  final @Nonnull Option<FileReference<T>> lookup(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final Log log = this.getLogLookup();
    if (log.enabledByConfiguration()) {
      log.debug(path.toString());
    }

    final PathVirtualEnum e = new PathVirtualEnum(path);
    while (e.hasMoreElements()) {
      final PathVirtual p = e.nextElement();
      final FileReference<T> r = this.lookupActual(p);
      if (r == null) {
        return new Option.None<FileReference<T>>();
      }
      switch (r.type) {
        case TYPE_DIRECTORY:
        {
          break;
        }
        case TYPE_FILE:
        {
          throw FilesystemError.notDirectory(p.toString());
        }
      }
    }

    final FileReference<T> r = this.lookupActual(path);
    if (r == null) {
      return new Option.None<FileReference<T>>();
    }

    return new Option.Some<FileReference<T>>(r);
  }

  /**
   * <p>
   * Retrieve a reference to the object at the given path directly, without
   * inspecting any ancestors of the given path.
   * </p>
   * 
   * @return A reference to the filesystem object at <code>path</code>,
   *         <code>null</code> if no object exists at <code>path</code>.
   * @throws ConstraintError
   *           If <code>path == null</code>.
   */

  abstract protected @CheckForNull FileReference<T> lookupActual(
    final @Nonnull PathVirtual path)
    throws ConstraintError;

  /**
   * <p>
   * Open the file at <code>path</code>.
   * </p>
   * 
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a file</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  final @Nonnull InputStream openFile(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final Option<FileReference<T>> ro = this.lookup(path);
    switch (ro.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<FileReference<T>> s = (Option.Some<FileReference<T>>) ro;
        final FileReference<T> ar = s.value;

        switch (s.value.type) {
          case TYPE_DIRECTORY:
          {
            throw FilesystemError.notFile(path.toString());
          }
          case TYPE_FILE:
          {
            return this.openFileActual(ar);
          }
        }
        break;
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * <p>
   * Open the file at the given reference <code>r</code>.
   * </p>
   * 
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a file</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  abstract protected @Nonnull InputStream openFileActual(
    final @Nonnull FileReference<T> r)
    throws FilesystemError,
      ConstraintError;
}
