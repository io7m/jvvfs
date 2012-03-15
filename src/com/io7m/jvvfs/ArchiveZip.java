package com.io7m.jvvfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FileReference.Type;

public final class ArchiveZip implements Archive
{
  private static long convertZipTime(final long t)
  {
    final String os = System.getProperty("os.name");
    if (os.indexOf("win") >= 0 || os.indexOf("Win") >= 0) {
      return convertZipTimeWin32(t);
    }
    return convertZipTimeUnix(t);
  }
  private static long convertZipTimeUnix(final long t)
  {
    return t - 315532800000L;
  }
  private static long convertZipTimeWin32(final long t)
  {
    return t - 315561600000L;
  }
  private final @Nonnull PathVirtual  path_mount;
  private final @Nonnull PathReal     path_real;

  private final @Nonnull ZipFile      zip;

  private final @Nonnull Log          log;

  private final @CheckForNull Archive parent;

  public ArchiveZip(
    final @CheckForNull Archive parent,
    final @Nonnull PathReal path,
    final @Nonnull PathVirtual mount,
    final @Nonnull Log log)
    throws ConstraintError,
      FilesystemError
  {
    try {
      this.log = new Log(Constraints.constrainNotNull(log, "log"), "archive");
      this.path_real = Constraints.constrainNotNull(path, "path");
      this.path_mount = Constraints.constrainNotNull(mount, "mount point");
      this.parent = parent;
      this.zip = new ZipFile(new File(this.path_real.value));
      assert this.zip != null;
    } catch (final IOException e) {
      throw FilesystemError.brokenArchive(path.value, e.getMessage());
    }
  }

  public ArchiveZip(
    final @Nonnull PathReal path,
    final @Nonnull PathVirtual mount,
    final @Nonnull Log log)
    throws ConstraintError,
      FilesystemError
  {
    this(null, path, mount, log);
  }

  @Override public void close()
    throws FilesystemError
  {
    this.log.info("close " + this.path_real);

    try {
      this.zip.close();
    } catch (final IOException e) {
      throw FilesystemError.ioError(this.path_real.value, e.getMessage());
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

  @Override public long fileSize(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("file-size " + path);
    return this.fileSizeInternal(Constraints.constrainNotNull(path, "path"));
  }

  private long fileSizeInternal(
    final @Nonnull PathVirtual path)
    throws FilesystemError
  {
    if (path.isRoot()) {
      throw FilesystemError.isDirectory(path.toString());
    }

    final String name_minus_slash = path.toString().replaceFirst("^/", "");
    assert name_minus_slash.length() > 0;

    final String name_slash = name_minus_slash + "/";

    {
      final ZipEntry entry_name = this.zip.getEntry(name_minus_slash);
      final ZipEntry entry_slash = this.zip.getEntry(name_slash);

      if ((entry_slash != null) && (entry_name != null)) {
        throw FilesystemError.isDirectory(path.toString());
      }
      if ((entry_slash == null) && (entry_name != null)) {
        return entry_name.getSize();
      }
    }

    /* UNREACHABLE */
    throw new AssertionError("bug: unreachable code");
  }

  @Override public @Nonnull PathVirtual getMountPath()
  {
    return this.path_mount;
  }

  @Override public Archive getParent()
  {
    return this.parent;
  }

  @Override public @Nonnull PathReal getRealPath()
  {
    return this.path_real;
  }

  @Override public void listDirectory(
    final @Nonnull PathVirtual path,
    final @Nonnull TreeSet<String> items)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("list-directory " + path);
    this.listDirectoryInternal(
      Constraints.constrainNotNull(path, "path"),
      Constraints.constrainNotNull(items, "items"));
  }

  private void listDirectoryInternal(
    final @Nonnull PathVirtual path,
    final @Nonnull TreeSet<String> items)
    throws ConstraintError,
      FilesystemError
  {
    final FileReference ref = this.lookup(path);
    if (ref.type != Type.TYPE_DIRECTORY) {
      throw FilesystemError.notDirectory(path.toString());
    }

    final Enumeration<? extends ZipEntry> entries = this.zip.entries();
    final String ps = path.toString() + (path.isRoot() ? "" : "/");

    while (entries.hasMoreElements()) {
      final ZipEntry e = entries.nextElement();
      final String en0 = "/" + e.getName();

      if (en0.startsWith(ps)) {
        final String en1 = en0.substring(ps.length());
        final String en2 = en1.replaceFirst("^/", "");
        if (en2.contains("/")) {
          final String en3 = en2.substring(0, en2.indexOf('/'));
          items.add(en3);
        } else {
          items.add(en2);
        }
      }
    }
  }

  @Override public FileReference lookup(
    final PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("lookup " + path);
    return this.lookupInternal(path);
  }
  
  private @Nonnull FileReference lookupInternal(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    if (path.isRoot()) {
      return new FileReference(this, path, Type.TYPE_DIRECTORY);
    }

    final String name_minus_slash = path.toString().replaceFirst("^/", "");
    assert name_minus_slash.length() > 0;

    final String name_slash = name_minus_slash + "/";

    {
      final ZipEntry entry_name = this.zip.getEntry(name_minus_slash);
      final ZipEntry entry_slash = this.zip.getEntry(name_slash);

      if ((entry_slash != null) && (entry_name != null)) {
        return new FileReference(this, path, Type.TYPE_DIRECTORY);
      }
      if ((entry_slash == null) && (entry_name != null)) {
        return new FileReference(this, path, Type.TYPE_FILE);
      }
    }

    {
      final ZipEntry entry = this.expensiveDirectoryLookup(name_slash);
      if (entry != null) {
        return new FileReference(this, path, Type.TYPE_DIRECTORY);
      }
    }

    throw FilesystemError.fileNotFound(path.toString());
  }

  /*
   * XXX: Portability: There are probably systems that aren't UNIX
   * and aren't Windows.
   */
  
  @Override public long modificationTime(
    final PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    return this.modificationTimeInternal(Constraints.constrainNotNull(
      path,
      "path"));
  }
  
  private long modificationTimeInternal(
    final @Nonnull PathVirtual path)
    throws FilesystemError
  {
    if (path.isRoot()) {
      throw FilesystemError.isDirectory(path.toString());
    }

    final String name_minus_slash = path.toString().replaceFirst("^/", "");
    assert name_minus_slash.length() > 0;

    final String name_slash = name_minus_slash + "/";

    {
      final ZipEntry entry_name = this.zip.getEntry(name_minus_slash);
      final ZipEntry entry_slash = this.zip.getEntry(name_slash);

      if ((entry_slash != null) && (entry_name != null)) {
        throw FilesystemError.isDirectory(path.toString());
      }

      /*
       * Zip files appear to store the date in seconds since the Windows NT
       * epoch (Jan 01 1980). Add a ten year offset to get the date into Unix
       * time.
       */
      if ((entry_slash == null) && (entry_name != null)) {
        return convertZipTime(entry_name.getTime());
      }
    }

    /* UNREACHABLE */
    throw new AssertionError("bug: unreachable code");
  }

  @Override public @Nonnull InputStream openFile(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("open-file " + path);
    return this.openFileInternal(Constraints.constrainNotNull(path, "path"));
  }

  private @Nonnull InputStream openFileInternal(
    final @Nonnull PathVirtual path)
    throws FilesystemError
  {
    try {
      if (path.isRoot()) {
        throw FilesystemError.isDirectory(path.toString());
      }

      final String name_minus_slash = path.toString().replaceFirst("^/", "");
      assert name_minus_slash.length() > 0;

      final String name_slash = name_minus_slash + "/";

      {
        final ZipEntry entry_name = this.zip.getEntry(name_minus_slash);
        final ZipEntry entry_slash = this.zip.getEntry(name_slash);

        if ((entry_slash != null) && (entry_name != null)) {
          throw FilesystemError.isDirectory(path.toString());
        }
        if ((entry_slash == null) && (entry_name != null)) {
          return this.zip.getInputStream(entry_name);
        }
      }

      /* UNREACHABLE */
      throw new AssertionError("bug: unreachable code");
    } catch (final IOException e) {
      throw FilesystemError.ioError(path.toString(), e.getMessage());
    }
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("zip:");
    builder.append(this.getRealPath());
    return builder.toString();
  }
}
