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

import javax.annotation.Nonnull;

/**
 * <p>
 * Exception type raised during filesystem operations.
 * </p>
 */

public final class FilesystemError extends Exception
{
  static enum Code
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
    FS_ERROR_NOT_MOUNTED,
    FS_ERROR_NOT_A_FILE
  }

  private static final long serialVersionUID = -2828062375812503115L;

  static @Nonnull FilesystemError brokenArchive(
    final String archive,
    final String message)
  {
    return new FilesystemError(Code.FS_ERROR_ARCHIVE_DAMAGED, "archive '"
      + archive
      + "' appears to be corrupt - "
      + message);
  }

  static @Nonnull FilesystemError busy(
    final String file)
  {
    return new FilesystemError(Code.FS_ERROR_BUSY, "could not unmount '"
      + file
      + "', filesystem is busy");
  }

  static @Nonnull FilesystemError duplicateMount(
    final String archive,
    final String mount)
  {
    return new FilesystemError(Code.FS_ERROR_DUPLICATE_MOUNT, "archive '"
      + archive
      + "' is already mounted at '"
      + mount
      + "'");
  }

  static @Nonnull FilesystemError fileNotFound(
    final String name)
  {
    return new FilesystemError(Code.FS_ERROR_NONEXISTENT, "file not found '"
      + name
      + "'");
  }

  static @Nonnull FilesystemError ioError(
    final @Nonnull Exception e)
  {
    return new FilesystemError(e);
  }

  static @Nonnull FilesystemError isDirectory(
    final String path)
  {
    return new FilesystemError(Code.FS_ERROR_IS_A_DIRECTORY, "file '"
      + path
      + "' is a directory");
  }

  static @Nonnull FilesystemError notDirectory(
    final String path)
  {
    return new FilesystemError(Code.FS_ERROR_NOT_A_DIRECTORY, "file '"
      + path
      + "' is not a directory");
  }

  static @Nonnull FilesystemError notFile(
    final String path)
  {
    return new FilesystemError(Code.FS_ERROR_NOT_A_FILE, "directory '"
      + path
      + "' is not a file");
  }

  static @Nonnull FilesystemError notMounted(
    final String file)
  {
    return new FilesystemError(
      Code.FS_ERROR_NOT_MOUNTED,
      "no filesystem mounted at '" + file + "'");
  }

  static @Nonnull FilesystemError unhandledType(
    final String file)
  {
    return new FilesystemError(
      Code.FS_ERROR_UNHANDLED_TYPE,
      "no handler for file '" + file + "'");
  }

  final @Nonnull Code code;

  FilesystemError(
    final @Nonnull Code code,
    final @Nonnull String message)
  {
    super(message);
    this.code = code;
  }

  FilesystemError(
    final @Nonnull Exception cause)
  {
    super(cause);
    this.code = Code.FS_ERROR_IO_ERROR;
  }
}
