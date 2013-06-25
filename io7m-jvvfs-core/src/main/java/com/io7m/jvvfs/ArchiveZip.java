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
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
 * Archive based on zip files.
 * </p>
 * 
 * <p>
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 * </p>
 */

@NotThreadSafe final class ArchiveZip extends Archive<ArchiveZipKind>
{
  static final class ArchiveZipReference extends
    FileReference<ArchiveZipKind>
  {
    /** <code>None</code> iff <code>path.isRoot()</code>. */
    final @Nonnull Option<ZipEntry> zip_entry_opt;

    ArchiveZipReference(
      final @Nonnull Archive<ArchiveZipKind> archive,
      final @Nonnull PathVirtual path,
      final @Nonnull Type type,
      final @Nonnull ZipEntry actual)
      throws ConstraintError
    {
      super(archive, path, type);

      if (actual == null) {
        Constraints.constrainArbitrary(
          path.isRoot(),
          "Path must be root for null zip entry");
        this.zip_entry_opt = new Option.None<ZipEntry>();
      } else {
        this.zip_entry_opt = new Option.Some<ZipEntry>(actual);
      }
    }
  }

  private final @Nonnull ZipFile     zip;
  private final @Nonnull PathVirtual mount;
  private final @Nonnull PathReal    real;

  ArchiveZip(
    final @Nonnull PathReal base_path,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      IOException,
      FilesystemError
  {
    try {
      this.mount = Constraints.constrainNotNull(mount, "Mount path");
      this.zip = new ZipFile(base_path.toString());
      this.real = new PathReal(base_path.toString());
    } catch (final ZipException e) {
      throw FilesystemError.archiveDamaged(
        base_path.toFile().getName(),
        e.getMessage());
    }
  }

  @Override void close()
    throws FilesystemError
  {
    try {
      this.zip.close();
    } catch (final IOException e) {
      throw FilesystemError.ioError(e);
    }
  }

  private @CheckForNull ZipEntry expensiveDirectoryLookup(
    final @Nonnull String name)
  {
    final Enumeration<? extends ZipEntry> entries = this.zip.entries();

    while (entries.hasMoreElements()) {
      final ZipEntry e = entries.nextElement();
      final String entry_name = e.getName();
      if (entry_name.startsWith(name)) {
        return e;
      }
    }

    return null;
  }

  @Override protected long getFileSizeActual(
    final @Nonnull FileReference<ArchiveZipKind> r)
    throws FilesystemError,
      ConstraintError
  {
    final ArchiveZipReference ra = (ArchiveZipReference) r;
    assert ra.type == Type.TYPE_FILE;

    switch (ra.zip_entry_opt.type) {
      case OPTION_NONE:
      {
        /**
         * The zip entry can only be <code>None</code> if the given path was
         * root. If the given path is root, it must be a directory, and
         * <code>getFileSizeActual</code> will never be called by
         * <code>Archive#getFileSize(PathVirtual)</code> with a reference to a
         * directory.
         */

        throw new UnreachableCodeException();
      }
      case OPTION_SOME:
      {
        final ZipEntry ze = ((Option.Some<ZipEntry>) ra.zip_entry_opt).value;
        return ze.getSize();
      }
    }

    throw new UnreachableCodeException();
  }

  @Override protected @Nonnull Calendar getModificationTimeActual(
    final @Nonnull FileReference<ArchiveZipKind> r)
  {
    final ArchiveZipReference ra = (ArchiveZipReference) r;
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    switch (ra.zip_entry_opt.type) {
      case OPTION_NONE:
      {
        final File file = new File(this.real.toString());
        c.setTimeInMillis(file.lastModified());
        return c;
      }
      case OPTION_SOME:
      {
        final ZipEntry ze = ((Option.Some<ZipEntry>) ra.zip_entry_opt).value;
        c.setTimeInMillis(ze.getTime());
        return c;
      }
    }

    throw new UnreachableCodeException();
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
    final Option<FileReference<ArchiveZipKind>> r = this.lookup(path);
    switch (r.type) {
      case OPTION_NONE:
      {
        throw FilesystemError.fileNotFound(path.toString());
      }
      case OPTION_SOME:
      {
        final Some<FileReference<ArchiveZipKind>> s =
          (Option.Some<FileReference<ArchiveZipKind>>) r;
        final FileReference<ArchiveZipKind> ar = s.value;
        final ArchiveZipReference ra = (ArchiveZipReference) ar;

        switch (s.value.type) {
          case TYPE_DIRECTORY:
          {
            return this.listDirectoryInternal(ra);
          }
          case TYPE_FILE:
          {
            throw FilesystemError.notDirectory(path.toString());
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  private Set<String> listDirectoryInternal(
    final @Nonnull ArchiveZipReference ra)
  {
    final TreeSet<String> items = new TreeSet<String>();
    final Enumeration<? extends ZipEntry> entries = this.zip.entries();
    final String ps = ra.path.toString() + (ra.path.isRoot() ? "" : "/");

    while (entries.hasMoreElements()) {
      final ZipEntry e = entries.nextElement();
      final String en0 = "/" + e.getName();

      if (en0.startsWith(ps)) {
        final String en1 = en0.substring(ps.length());
        final String en2 = en1.replaceFirst("^/", "");

        if (en2.length() == 0) {
          continue;
        }

        if (en2.contains("/")) {
          final String en3 = en2.substring(0, en2.indexOf('/'));
          items.add(en3);
        } else {
          items.add(en2);
        }
      }
    }

    return items;
  }

  @Override protected @CheckForNull
    FileReference<ArchiveZipKind>
    lookupActual(
      final @Nonnull PathVirtual path)
      throws ConstraintError
  {
    if (path.isRoot()) {
      return new ArchiveZipReference(this, path, Type.TYPE_DIRECTORY, null);
    }

    final String name_minus_slash = path.toString().replaceFirst("^/", "");
    assert name_minus_slash.length() > 0;

    final String name_slash = name_minus_slash + "/";

    {
      final ZipEntry entry_name = this.zip.getEntry(name_minus_slash);
      final ZipEntry entry_slash = this.zip.getEntry(name_slash);

      if ((entry_slash != null) && (entry_name != null)) {
        return new ArchiveZipReference(
          this,
          path,
          Type.TYPE_DIRECTORY,
          entry_slash);
      }
      if ((entry_slash == null) && (entry_name != null)) {
        return new ArchiveZipReference(this, path, Type.TYPE_FILE, entry_name);
      }
    }

    {
      final ZipEntry entry = this.expensiveDirectoryLookup(name_slash);
      if (entry != null) {
        return new ArchiveZipReference(this, path, Type.TYPE_DIRECTORY, entry);
      }
    }

    return null;
  }

  @Override protected @Nonnull InputStream openFileActual(
    final @Nonnull FileReference<ArchiveZipKind> r)
    throws FilesystemError,
      ConstraintError
  {
    final ArchiveZipReference ra = (ArchiveZipReference) r;
    assert ra.type == Type.TYPE_FILE;

    try {
      switch (ra.zip_entry_opt.type) {
        case OPTION_NONE:
        {
          /**
           * The zip entry can only be <code>None</code> if the given path was
           * root. If the given path is root, it must be a directory, and
           * <code>openFileActual</code> will never be called by
           * <code>Archive#openFile(PathVirtual)</code> with a reference to a
           * directory.
           */

          throw new UnreachableCodeException();
        }
        case OPTION_SOME:
        {
          final ZipEntry ze =
            ((Option.Some<ZipEntry>) ra.zip_entry_opt).value;
          return this.zip.getInputStream(ze);
        }
      }
    } catch (final IOException e) {
      throw FilesystemError.ioError(e);
    }

    throw new UnreachableCodeException();
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[ArchiveZip ");
    builder.append(this.real);
    builder.append(" ");
    builder.append(this.mount);
    builder.append("]");
    return builder.toString();
  }
}
