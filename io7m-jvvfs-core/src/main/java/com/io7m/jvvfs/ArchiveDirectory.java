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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
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

final class ArchiveDirectory extends Archive<ArchiveDirectoryKind>
{
  static final class ArchiveDirectoryReference extends
    FileReference<ArchiveDirectoryKind>
  {
    private final File actual;

    ArchiveDirectoryReference(
      final Archive<ArchiveDirectoryKind> in_archive,
      final PathVirtual in_path,
      final Type in_type,
      final File in_actual)
    {
      super(in_archive, in_path, in_type);
      this.actual = in_actual;
    }

    File getActual()
    {
      return this.actual;
    }
  }

  private final File          base;
  private final LogUsableType log;
  private final PathVirtual   mount;
  private final PathReal      real;

  ArchiveDirectory(
    final LogUsableType in_log,
    final PathReal base_path,
    final PathVirtual in_mount)
  {
    this.log = NullCheck.notNull(in_log, "Log").with("directory");
    this.mount = NullCheck.notNull(in_mount, "Mount path");
    this.base = new File(base_path.toString());
    final String r = this.base.toString();
    assert r != null;
    this.real = new PathReal(r);
  }

  @Override void close()
    throws FilesystemError
  {
    // Nothing required
  }

  @Override protected long getFileSizeActual(
    final FileReference<ArchiveDirectoryKind> r)
    throws FilesystemError
  {
    final ArchiveDirectoryReference ra = (ArchiveDirectoryReference) r;
    return ra.getActual().length();
  }

  @Override protected LogUsableType getLogLookup()
  {
    return this.log;
  }

  @Override protected Calendar getModificationTimeActual(
    final FileReference<ArchiveDirectoryKind> r)
  {
    final ArchiveDirectoryReference ra = (ArchiveDirectoryReference) r;
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    c.setTimeInMillis(ra.getActual().lastModified());
    return c;
  }

  @Override PathVirtual getMountPath()
  {
    return this.mount;
  }

  @Override PathReal getRealPath()
  {
    return this.real;
  }

  @Override SortedSet<String> listDirectory(
    final PathVirtual path)
    throws FilesystemError
  {
    final OptionType<FileReference<ArchiveDirectoryKind>> r =
      this.lookup(path);
    return r
      .acceptPartial(new OptionPartialVisitorType<FileReference<ArchiveDirectoryKind>, SortedSet<String>, FilesystemError>() {
        @Override public SortedSet<String> none(
          final None<FileReference<ArchiveDirectoryKind>> n)
          throws FilesystemError
        {
          throw FilesystemError.fileNotFound(path.toString());
        }

        @Override public SortedSet<String> some(
          final Some<FileReference<ArchiveDirectoryKind>> s)
          throws FilesystemError
        {
          final FileReference<ArchiveDirectoryKind> ar = s.get();
          final ArchiveDirectoryReference ra = (ArchiveDirectoryReference) ar;

          switch (ra.getType()) {
            case TYPE_DIRECTORY:
            {
              final String[] fs = ra.getActual().list();
              final SortedSet<String> ts = new TreeSet<String>();
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

          throw new UnreachableCodeException();
        }
      });
  }

  @Override protected @Nullable
    FileReference<ArchiveDirectoryKind>
    lookupActual(
      final PathVirtual path)
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

  @Override protected InputStream openFileActual(
    final FileReference<ArchiveDirectoryKind> r)
    throws FilesystemError
  {
    final ArchiveDirectoryReference ra = (ArchiveDirectoryReference) r;
    try {
      return new FileInputStream(ra.getActual());
    } catch (final FileNotFoundException e) {
      throw FilesystemError.fileNotFound(ra.getPath().toString());
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
