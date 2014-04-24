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

import java.io.InputStream;
import java.util.Calendar;
import java.util.SortedSet;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;

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
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<FileReference<T>> ro = this.lookup(path);
    return ro
      .acceptPartial(
        new OptionPartialVisitorType<FileReference<T>, Long, FilesystemError>() {
          @Override public Long none(
            final None<FileReference<T>> n)
            throws FilesystemError
          {
            throw FilesystemError.fileNotFound(path.toString());
          }

          @Override public Long some(
            final Some<FileReference<T>> s)
            throws FilesystemError
          {
            final FileReference<T> r = s.get();
            switch (r.getType()) {
              case TYPE_DIRECTORY:
              {
                throw FilesystemError.notFile(path.toString());
              }
              case TYPE_FILE:
              {
                final Long k =
                  Long.valueOf(Archive.this.getFileSizeActual(r));
                assert k != null;
                return k;
              }
            }

            throw new UnreachableCodeException();
          }
        })
      .longValue();
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
    final FileReference<T> r)
    throws FilesystemError;

  abstract protected LogUsableType getLogLookup();

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

  final Calendar getModificationTime(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<FileReference<T>> ro = this.lookup(path);
    return ro
      .acceptPartial(new OptionPartialVisitorType<FileReference<T>, Calendar, FilesystemError>() {
        @Override public Calendar none(
          final None<FileReference<T>> n)
          throws FilesystemError
        {
          throw FilesystemError.fileNotFound(path.toString());
        }

        @Override public Calendar some(
          final Some<FileReference<T>> s)
          throws FilesystemError
        {
          return Archive.this.getModificationTimeActual(s.get());
        }
      });
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

  abstract protected Calendar getModificationTimeActual(
    final FileReference<T> r);

  /**
   * <p>
   * The path at which the archive is mounted.
   * </p>
   */

  abstract PathVirtual getMountPath();

  /**
   * <p>
   * The real path of the archive.
   * </p>
   */

  abstract PathReal getRealPath();

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

  abstract SortedSet<String> listDirectory(
    final PathVirtual path)
    throws FilesystemError;

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

  final OptionType<FileReference<T>> lookup(
    final PathVirtual path)
    throws FilesystemError
  {
    final LogUsableType log = this.getLogLookup();
    log.debug(path.toString());

    final PathVirtualEnum e = PathVirtualEnum.enumerate(path);
    while (e.hasMoreElements()) {
      final PathVirtual p = e.nextElement();
      final FileReference<T> r = this.lookupActual(p);
      if (r == null) {
        return Option.none();
      }
      switch (r.getType()) {
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
      return Option.none();
    }

    return Option.some(r);
  }

  /**
   * <p>
   * Retrieve a reference to the object at the given path directly, without
   * inspecting any ancestors of the given path.
   * </p>
   * 
   * @return A reference to the filesystem object at <code>path</code>,
   *         <code>null</code> if no object exists at <code>path</code>.
   * @throws FilesystemError
   *           If a filesystem error occurs.
   */

  abstract protected @Nullable FileReference<T> lookupActual(
    final PathVirtual path)
    throws FilesystemError;

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

  final InputStream openFile(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<FileReference<T>> ro = this.lookup(path);

    return ro
      .acceptPartial(new OptionPartialVisitorType<FileReference<T>, InputStream, FilesystemError>() {
        @Override public InputStream none(
          final None<FileReference<T>> n)
          throws FilesystemError
        {
          throw FilesystemError.fileNotFound(path.toString());
        }

        @Override public InputStream some(
          final Some<FileReference<T>> s)
          throws FilesystemError
        {
          final FileReference<T> r = s.get();
          switch (r.getType()) {
            case TYPE_DIRECTORY:
            {
              throw FilesystemError.notFile(path.toString());
            }
            case TYPE_FILE:
            {
              return Archive.this.openFileActual(r);
            }
          }

          throw new UnreachableCodeException();
        }
      });
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

  abstract protected InputStream openFileActual(
    final FileReference<T> r)
    throws FilesystemError;
}
