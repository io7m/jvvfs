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
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.Some;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FileReference.Type;
import com.io7m.jvvfs.FilesystemError.Code;

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

final class ArchiveZip extends Archive<ArchiveZipKind>
{
  static final class ArchiveZipReference extends
    FileReference<ArchiveZipKind>
  {
    /**
     * <code>None</code> iff <code>path.isRoot()</code>.
     */

    private final OptionType<ZipEntry> zip_entry_opt;

    ArchiveZipReference(
      final Archive<ArchiveZipKind> in_archive,
      final PathVirtual in_path,
      final Type in_type,
      final @Nullable ZipEntry actual)
      throws FilesystemError
    {
      super(in_archive, in_path, in_type);

      if (actual == null) {
        if (in_path.isRoot() == false) {
          throw new FilesystemError(
            Code.FS_ERROR_CONSTRAINT_ERROR,
            "Path must be root for null zip entry");
        }
        this.zip_entry_opt = Option.none();
      } else {
        this.zip_entry_opt = Option.some(actual);
      }
    }

    /**
     * @return <code>None</code> iff <code>path.isRoot()</code>.
     */

    OptionType<ZipEntry> getZipEntryOption()
    {
      return this.zip_entry_opt;
    }
  }

  private final LogType     log;
  private final LogType     log_lookup;
  private final PathVirtual mount;
  private final PathReal    real;
  private final ZipFile     zip;

  ArchiveZip(
    final LogUsableType in_log,
    final PathReal base_path,
    final PathVirtual in_mount)
    throws IOException,
      FilesystemError
  {
    try {
      this.log = NullCheck.notNull(in_log, "Log").with("zip");
      this.log_lookup = this.log.with("lookup");
      this.mount = NullCheck.notNull(in_mount, "Mount path");
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

  private @Nullable ZipEntry expensiveDirectoryLookup(
    final String name)
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
    final FileReference<ArchiveZipKind> r)
    throws FilesystemError
  {
    final ArchiveZipReference ra = (ArchiveZipReference) r;
    assert ra.getType() == Type.TYPE_FILE;

    return ra
      .getZipEntryOption()
      .accept(new OptionVisitorType<ZipEntry, Long>() {
        @Override public Long none(
          final None<ZipEntry> n)
        {
          /**
           * The zip entry can only be <code>None</code> if the given path was
           * root. If the given path is root, it must be a directory, and
           * <code>getFileSizeActual</code> will never be called by
           * <code>Archive#getFileSize(PathVirtual)</code> with a reference to
           * a directory.
           */

          throw new UnreachableCodeException();
        }

        @Override public Long some(
          final Some<ZipEntry> s)
        {
          final Long rs = Long.valueOf(s.get().getSize());
          assert rs != null;
          return rs;
        }
      })
      .longValue();
  }

  @Override protected LogType getLogLookup()
  {
    return this.log_lookup;
  }

  @Override protected Calendar getModificationTimeActual(
    final FileReference<ArchiveZipKind> r)
  {
    final ArchiveZipReference ra = (ArchiveZipReference) r;
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    return ra.getZipEntryOption().accept(
      new OptionVisitorType<ZipEntry, Calendar>() {
        @SuppressWarnings("synthetic-access") @Override public Calendar none(
          final None<ZipEntry> n)
        {
          final File file = new File(ArchiveZip.this.real.toString());
          c.setTimeInMillis(file.lastModified());
          return c;
        }

        @Override public Calendar some(
          final Some<ZipEntry> s)
        {
          c.setTimeInMillis(s.get().getTime());
          return c;
        }
      });
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
    final OptionType<FileReference<ArchiveZipKind>> r = this.lookup(path);
    return r
      .acceptPartial(new OptionPartialVisitorType<FileReference<ArchiveZipKind>, SortedSet<String>, FilesystemError>() {
        @Override public SortedSet<String> none(
          final None<FileReference<ArchiveZipKind>> n)
          throws FilesystemError
        {
          throw FilesystemError.fileNotFound(path.toString());
        }

        @SuppressWarnings("synthetic-access") @Override public
          SortedSet<String>
          some(
            final Some<FileReference<ArchiveZipKind>> s)
            throws FilesystemError
        {
          final FileReference<ArchiveZipKind> ar = s.get();
          final ArchiveZipReference ra = (ArchiveZipReference) ar;

          switch (ra.getType()) {
            case TYPE_DIRECTORY:
            {
              return ArchiveZip.this.listDirectoryInternal(ra);
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

  private SortedSet<String> listDirectoryInternal(
    final ArchiveZipReference ra)
  {
    final SortedSet<String> items = new TreeSet<String>();
    final Enumeration<? extends ZipEntry> entries = this.zip.entries();
    final String ps =
      ra.getPath().toString() + (ra.getPath().isRoot() ? "" : "/");

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

  @Override protected @Nullable FileReference<ArchiveZipKind> lookupActual(
    final PathVirtual path)
    throws FilesystemError
  {
    if (this.log_lookup.wouldLog(LogLevel.LOG_DEBUG)) {
      final StringBuilder m = new StringBuilder();
      m.append(this.real.toFile().getName());
      m.append(": ");
      m.append(path.toString());
      final String r = m.toString();
      assert r != null;
      this.log_lookup.debug(r);
    }

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

    if (this.log_lookup.wouldLog(LogLevel.LOG_DEBUG)) {
      final StringBuilder m = new StringBuilder();
      m.append(this.real.toFile().getName());
      m.append(": ");
      m.append(path.toString());
      m.append(" is nonexistent");
      final String r = m.toString();
      assert r != null;
      this.log_lookup.debug(r);
    }
    return null;
  }

  @Override protected InputStream openFileActual(
    final FileReference<ArchiveZipKind> r)
    throws FilesystemError
  {
    final ArchiveZipReference ra = (ArchiveZipReference) r;
    assert ra.getType() == Type.TYPE_FILE;

    try {
      return ra.getZipEntryOption().acceptPartial(
        new OptionPartialVisitorType<ZipEntry, InputStream, IOException>() {
          @Override public InputStream none(
            final None<ZipEntry> n)
          {
            /**
             * The zip entry can only be <code>None</code> if the given path
             * was root. If the given path is root, it must be a directory,
             * and <code>openFileActual</code> will never be called by
             * <code>Archive#openFile(PathVirtual)</code> with a reference to
             * a directory.
             */

            throw new UnreachableCodeException();
          }

          @SuppressWarnings("synthetic-access") @Override public
            InputStream
            some(
              final Some<ZipEntry> s)
              throws IOException
          {
            final InputStream ri =
              ArchiveZip.this.zip.getInputStream(s.get());
            assert ri != null;
            return ri;
          }
        });
    } catch (final IOException e) {
      throw FilesystemError.ioError(e);
    }
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
