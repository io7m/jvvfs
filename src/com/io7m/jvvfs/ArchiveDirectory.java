package com.io7m.jvvfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FileReference.Type;

public final class ArchiveDirectory implements Archive
{
  private final @Nonnull PathVirtual  path_mount;
  private final @Nonnull PathReal     path_real;
  private final @Nonnull Log          log;
  private final @CheckForNull Archive parent;

  public ArchiveDirectory(
    final @CheckForNull Archive parent,
    final @Nonnull PathReal path,
    final @Nonnull PathVirtual mount,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.parent = parent;
    this.path_real = Constraints.constrainNotNull(path, "path");
    this.path_mount = Constraints.constrainNotNull(mount, "mount point");
    this.log = new Log(Constraints.constrainNotNull(log, "log"), "archive");
  }

  public ArchiveDirectory(
    final @Nonnull PathReal path,
    final @Nonnull PathVirtual mount,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this(null, path, mount, log);
  }

  @Override public void close()
  {
    this.log.info("close " + this.path_real);
  }

  @Override public long fileSize(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    return this.fileSizeInternal(Constraints.constrainNotNull(path, "path"));
  }

  private long fileSizeInternal(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    final PathReal actual = this.path_real.concatenate(path);
    this.log.debug("size-real " + actual.value);

    final File file = new File(actual.value);
    if (file.isDirectory() == true) {
      throw FilesystemError.isDirectory(path.toString());
    }
    if (file.exists() == false) {
      throw FilesystemError.fileNotFound(path.toString());
    }
    return file.length();
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

    final File file =
      new File(this.path_real.value + "/" + ref.path.toString());

    for (final String item : file.list()) {
      items.add(item);
    }
  }

  @Override public @Nonnull FileReference lookup(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.debug("lookup " + path.toString());

    final PathReal actual = this.path_real.concatenate(path);

    this.log.debug("lookup-real " + actual.value);

    final File file = new File(actual.value);
    if (file.exists()) {
      return new FileReference(this, path, file.isDirectory()
        ? Type.TYPE_DIRECTORY
        : Type.TYPE_FILE);
    }

    this.log.debug("lookup-real " + actual.value + " - nonexistent");
    throw FilesystemError.fileNotFound(path.toString());
  }

  @Override public long modificationTime(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    return this.modificationTimeInternal(Constraints.constrainNotNull(
      path,
      "path"));
  }

  private long modificationTimeInternal(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    final PathReal actual = this.path_real.concatenate(path);
    this.log.debug("lookup-real " + actual.value);

    final File file = new File(actual.value);
    if (file.isDirectory() == true) {
      throw FilesystemError.isDirectory(path.toString());
    }
    if (file.exists() == false) {
      throw FilesystemError.fileNotFound(path.toString());
    }
    return file.lastModified();
  }

  @Override public InputStream openFile(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("open-file " + path.toString());
    return this.openFileInternal(Constraints.constrainNotNull(path, "path"));
  }

  private InputStream openFileInternal(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    final PathReal actual = this.path_real.concatenate(path);
    this.log.debug("lookup-real " + actual.value);

    try {
      final File file = new File(actual.value);
      if (file.isDirectory() == true) {
        throw FilesystemError.isDirectory(path.toString());
      }
      if (file.exists() == false) {
        throw FilesystemError.fileNotFound(path.toString());
      }
      return new FileInputStream(file);
    } catch (final FileNotFoundException e) {
      throw FilesystemError.fileNotFound(path.toString());
    }
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("dir:");
    builder.append(this.getRealPath());
    return builder.toString();
  }
}
