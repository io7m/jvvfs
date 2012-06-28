package com.io7m.jvvfs;

import javax.annotation.Nonnull;

/**
 * Exception type raised during filesystem operations.
 */

public final class FilesystemError extends Exception
{
  public static enum Code
  {
    FS_ERROR_ARCHIVE_DAMAGED,
    FS_ERROR_CONSTRAINT_ERROR,
    FS_ERROR_DUPLICATE_MOUNT,
    FS_ERROR_IO_ERROR,
    FS_ERROR_IS_A_DIRECTORY,
    FS_ERROR_NONEXISTENT,
    FS_ERROR_NOT_A_DIRECTORY,
    FS_ERROR_UNHANDLED_TYPE,
    FS_ERROR_BUSY,
    FS_ERROR_NOT_MOUNTED
  }

  private static final long serialVersionUID = -2828062375812503115L;

  public static @Nonnull FilesystemError brokenArchive(
    final String archive,
    final String message)
  {
    return new FilesystemError(Code.FS_ERROR_ARCHIVE_DAMAGED, "archive '"
      + archive
      + "' appears to be corrupt - "
      + message);
  }

  public static @Nonnull FilesystemError busy(
    final String file)
  {
    return new FilesystemError(Code.FS_ERROR_BUSY, "could not unmount '"
      + file
      + "', filesystem is busy");
  }

  public static @Nonnull FilesystemError duplicateMount(
    final String archive,
    final String mount)
  {
    return new FilesystemError(Code.FS_ERROR_DUPLICATE_MOUNT, "archive '"
      + archive
      + "' is already mounted at '"
      + mount
      + "'");
  }

  public static @Nonnull FilesystemError fileNotFound(
    final String name)
  {
    return new FilesystemError(Code.FS_ERROR_NONEXISTENT, "file not found '"
      + name
      + "'");
  }

  public static @Nonnull FilesystemError ioError(
    final String archive,
    final String message)
  {
    return new FilesystemError(Code.FS_ERROR_IO_ERROR, "i/o error for '"
      + archive
      + "': "
      + message);
  }

  public static @Nonnull FilesystemError isDirectory(
    final String path)
  {
    return new FilesystemError(Code.FS_ERROR_IS_A_DIRECTORY, "file '"
      + path
      + "' is a directory");
  }

  public static @Nonnull FilesystemError notDirectory(
    final String path)
  {
    return new FilesystemError(Code.FS_ERROR_NOT_A_DIRECTORY, "file '"
      + path
      + "' is not a directory");
  }

  public static @Nonnull FilesystemError notMounted(
    final String file)
  {
    return new FilesystemError(
      Code.FS_ERROR_NOT_MOUNTED,
      "no filesystem mounted at '" + file + "'");
  }

  public static @Nonnull FilesystemError unhandledType(
    final String file)
  {
    return new FilesystemError(
      Code.FS_ERROR_UNHANDLED_TYPE,
      "no handler for file '" + file + "'");
  }

  public final @Nonnull Code code;

  public FilesystemError(
    final @Nonnull Code code,
    final @Nonnull String message)
  {
    super(message);
    this.code = code;
  }
}