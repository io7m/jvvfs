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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jvvfs.FileReference.Type;

/**
 * <p>
 * Archive based on standard filesystem/directory operations.
 * </p>
 * 
 * <p>
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 * </p>
 */

@NotThreadSafe final class ArchiveDirectory extends
  Archive<ArchiveDirectoryKind>
{
  static final class ArchiveDirectoryReference extends
    FileReference<ArchiveDirectoryKind>
  {
    final @Nonnull File actual;

    ArchiveDirectoryReference(
      final @Nonnull Archive<ArchiveDirectoryKind> archive,
      final @Nonnull PathVirtual path,
      final @Nonnull Type type,
      final @Nonnull File actual)
      throws ConstraintError
    {
      super(archive, path, type);
      this.actual = actual;
    }
  }

  private final @Nonnull File        base;
  private final @Nonnull PathVirtual mount;
  private final @Nonnull PathReal    real;

  ArchiveDirectory(
    final @Nonnull PathReal base_path,
    final @Nonnull PathVirtual mount)
    throws ConstraintError
  {
    this.mount = Constraints.constrainNotNull(mount, "Mount path");
    this.base = new File(base_path.toString());
    this.real = new PathReal(this.base.toString());
  }

  @Override void close()
    throws FilesystemError
  {
    // Nothing required
  }

  @Override long getFileSizeActual(
    final @Nonnull FileReference<ArchiveDirectoryKind> r)
    throws FilesystemError,
      ConstraintError
  {
    final ArchiveDirectoryReference ra = (ArchiveDirectoryReference) r;
    return ra.actual.length();
  }

  @Override @Nonnull Calendar getModificationTimeActual(
    final @Nonnull FileReference<ArchiveDirectoryKind> r)
  {
    final ArchiveDirectoryReference ra = (ArchiveDirectoryReference) r;
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    c.setTimeInMillis(ra.actual.lastModified());
    return c;
  }

  @Override @Nonnull PathVirtual getMountPath()
  {
    return this.mount;
  }

  @Override @Nonnull PathReal getRealPath()
  {
    return this.real;
  }

  @Override @Nonnull Set<String> listDirectory(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final Option<FileReference<ArchiveDirectoryKind>> r = this.lookup(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<FileReference<ArchiveDirectoryKind>> s =
          (Option.Some<FileReference<ArchiveDirectoryKind>>) r;
        final FileReference<ArchiveDirectoryKind> ar = s.value;
        final ArchiveDirectoryReference ra = (ArchiveDirectoryReference) ar;

        switch (s.value.type) {
          case TYPE_DIRECTORY:
          {
            final String[] fs = ra.actual.list();
            final TreeSet<String> ts = new TreeSet<String>();
            for (final String f : fs) {
              ts.add(f);
            }
            return ts;
          }
          case TYPE_FILE:
          {
            throw FilesystemError.notDirectory(path.toString());
          }
        }
        break;
      }
    }

    throw new UnreachableCodeException();
  }

  @Override @CheckForNull FileReference<ArchiveDirectoryKind> lookupActual(
    final @Nonnull PathVirtual path)
    throws ConstraintError
  {
    final File f = new File(this.base, path.toString());
    if (f.exists()) {
      final FileReference<ArchiveDirectoryKind> r =
        new ArchiveDirectoryReference(this, path, f.isDirectory()
          ? Type.TYPE_DIRECTORY
          : Type.TYPE_FILE, f);
      return r;
    }
    return null;
  }

  @Override @Nonnull InputStream openFileActual(
    final @Nonnull FileReference<ArchiveDirectoryKind> r)
    throws FilesystemError,
      ConstraintError
  {
    final ArchiveDirectoryReference ra = (ArchiveDirectoryReference) r;
    try {
      return new FileInputStream(ra.actual);
    } catch (final FileNotFoundException e) {
      throw FilesystemError.fileNotFound(ra.path.toString());
    }
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[ArchiveDirectory ");
    builder.append(this.base);
    builder.append(" ");
    builder.append(this.mount);
    builder.append("]");
    return builder.toString();
  }
}
