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
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FileReference.Type;
import com.io7m.jvvfs.TreeView.Result;
import com.io7m.jvvfs.TreeView.TVDirectory;

/**
 * <p>
 * Archive based on 7z files.
 * </p>
 * 
 * <p>
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 * </p>
 */

@NotThreadSafe final class Archive7z extends Archive<Archive7zKind>
{
  static final class Archive7zReference extends FileReference<Archive7zKind>
  {
    /** <code>None</code> iff <code>path.isRoot()</code>. */
    final @Nonnull Option<SevenZArchiveEntry> sz_entry_opt;

    Archive7zReference(
      final @Nonnull Archive<Archive7zKind> archive,
      final @Nonnull PathVirtual path,
      final @Nonnull Type type,
      final @Nonnull SevenZArchiveEntry actual)
      throws ConstraintError
    {
      super(archive, path, type);

      if (actual == null) {
        Constraints.constrainArbitrary(
          type == Type.TYPE_DIRECTORY,
          "A null entry must imply a directory");
        this.sz_entry_opt = new Option.None<SevenZArchiveEntry>();
        return;
      }

      this.sz_entry_opt = new Option.Some<SevenZArchiveEntry>(actual);
    }
  }

  private final @Nonnull Log                          log;
  private final @Nonnull Log                          log_lookup;
  private final @Nonnull PathVirtual                  mount;
  private final @Nonnull PathReal                     real;
  private final @Nonnull SevenZFile                   f7z;
  private final @Nonnull TreeView<SevenZArchiveEntry> tree;

  Archive7z(
    final @Nonnull Log log,
    final @Nonnull PathReal base_path,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    try {
      this.log = new Log(log, "7z");
      this.log_lookup = new Log(this.log, "lookup");

      this.mount = Constraints.constrainNotNull(mount, "Mount path");
      this.f7z = new SevenZFile(base_path.toFile());
      this.real = new PathReal(base_path.toString());
      this.tree = TreeView.make(null);

      for (;;) {
        final SevenZArchiveEntry e = this.f7z.getNextEntry();
        if (e == null) {
          break;
        }

        final PathVirtual path = PathVirtual.ofString("/" + e.getName());
        log.debug("cache " + path);

        if (e.isDirectory()) {
          final Result<SevenZArchiveEntry> r =
            this.tree.createDirectories(path);
          final TVDirectory<SevenZArchiveEntry> dir =
            (TVDirectory<SevenZArchiveEntry>) r.getNode();
          dir.setData(e);

        } else {
          final Option<PathVirtual> dir_name_opt = path.getDirectoryName();
          assert dir_name_opt.isSome();
          final Option<String> base_name_opt = path.getBaseName();
          assert base_name_opt.isSome();

          final PathVirtual dir_name =
            ((Option.Some<PathVirtual>) dir_name_opt).value;
          final String base_name =
            ((Option.Some<String>) base_name_opt).value;

          final Result<SevenZArchiveEntry> r =
            this.tree.createDirectories(dir_name);
          final TVDirectory<SevenZArchiveEntry> d =
            (TVDirectory<SevenZArchiveEntry>) r.getNode();

          d.addFile(base_name, e);
        }
      }

    } catch (final IOException e) {
      throw FilesystemError.ioError(e);
    }
  }

  @Override void close()
    throws FilesystemError
  {
    try {
      this.f7z.close();
    } catch (final IOException e) {
      throw FilesystemError.ioError(e);
    }
  }

  @Override protected long getFileSizeActual(
    final @Nonnull FileReference<Archive7zKind> r)
    throws FilesystemError,
      ConstraintError
  {
    final Archive7zReference ra = (Archive7zReference) r;
    assert ra.type == Type.TYPE_FILE;

    switch (ra.sz_entry_opt.type) {
      case OPTION_NONE:
      {
        /**
         * The entry can only be <code>None</code> if the given path was root.
         * If the given path is root, it must be a directory, and
         * <code>getFileSizeActual</code> will never be called by
         * <code>Archive#getFileSize(PathVirtual)</code> with a reference to a
         * directory.
         */

        throw new UnreachableCodeException();
      }
      case OPTION_SOME:
      {
        final SevenZArchiveEntry ze =
          ((Option.Some<SevenZArchiveEntry>) ra.sz_entry_opt).value;
        return ze.getSize();
      }
    }

    throw new UnreachableCodeException();
  }

  @Override protected @Nonnull Log getLogLookup()
  {
    return this.log_lookup;
  }

  @Override protected @Nonnull Calendar getModificationTimeActual(
    final @Nonnull FileReference<Archive7zKind> r)
  {
    final Archive7zReference ra = (Archive7zReference) r;
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    switch (ra.sz_entry_opt.type) {
      case OPTION_NONE:
      {
        final File file = new File(this.real.toString());
        c.setTimeInMillis(file.lastModified());
        return c;
      }
      case OPTION_SOME:
      {
        final SevenZArchiveEntry ze =
          ((Option.Some<SevenZArchiveEntry>) ra.sz_entry_opt).value;

        try {
          c.setTimeInMillis(ze.getLastModifiedDate().getTime());
        } catch (final UnsupportedOperationException _) {
          c.setTimeInMillis(0L);
        }
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
    final Result<SevenZArchiveEntry> r = this.tree.get(path);
    switch (r.getCode()) {
      case R_NONEXISTENT:
        throw FilesystemError.fileNotFound(path.toString());
      case R_NOT_A_DIRECTORY:
        throw FilesystemError.notDirectory(path.toString());
      case R_OK:
      {
        if (r.getNode() instanceof TVDirectory) {
          final TVDirectory<SevenZArchiveEntry> d =
            (TVDirectory<SevenZArchiveEntry>) r.getNode();
          return d.getChildren().keySet();
        }
        throw FilesystemError.notDirectory(path.toString());
      }
    }

    throw new UnreachableCodeException();
  }

  @Override protected @CheckForNull
    FileReference<Archive7zKind>
    lookupActual(
      final @Nonnull PathVirtual path)
      throws ConstraintError
  {
    final Result<SevenZArchiveEntry> r = this.tree.get(path);
    switch (r.getCode()) {
      case R_NONEXISTENT:
        return null;
      case R_NOT_A_DIRECTORY:
        return null;
      case R_OK:
      {
        if (path.isRoot()) {
          return new Archive7zReference(this, path, Type.TYPE_DIRECTORY, null);
        }

        if (r.getNode() instanceof TVDirectory) {
          return new Archive7zReference(this, path, Type.TYPE_DIRECTORY, r
            .getNode()
            .getData());
        }

        return new Archive7zReference(this, path, Type.TYPE_FILE, r
          .getNode()
          .getData());
      }
    }

    return null;
  }

  @Override protected @Nonnull InputStream openFileActual(
    final @Nonnull FileReference<Archive7zKind> r)
    throws FilesystemError,
      ConstraintError
  {
    final Archive7zReference ra = (Archive7zReference) r;
    assert ra.type == Type.TYPE_FILE;

    switch (ra.sz_entry_opt.type) {
      case OPTION_NONE:
      {
        /**
         * The entry can only be <code>None</code> if the given path was root.
         * If the given path is root, it must be a directory, and
         * <code>openFileActual</code> will never be called by
         * <code>Archive#openFile(PathVirtual)</code> with a reference to a
         * directory.
         */

        throw new UnreachableCodeException();
      }
      case OPTION_SOME:
      {
        final SevenZArchiveEntry ze =
          ((Option.Some<SevenZArchiveEntry>) ra.sz_entry_opt).value;

        /*
         * XXX: Due to "solid" compression, it's not possible to seek to an
         * arbitrary file in a 7z archive, making the 7z format ultimately
         * unusable.
         */
      }
    }

    throw new UnreachableCodeException();
  }

}
