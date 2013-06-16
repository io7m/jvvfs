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
    FS_ERROR_ARCHIVE_NO_DIRECTORY,
    FS_ERROR_ARCHIVE_NONEXISTENT,
    FS_ERROR_ARCHIVE_TYPE_UNSUPPORTED,
    FS_ERROR_ARCHIVE_ALREADY_MOUNTED,
    FS_ERROR_CONSTRAINT_ERROR,
    FS_ERROR_IO_ERROR,
    FS_ERROR_IS_A_DIRECTORY,
    FS_ERROR_IS_A_FILE,
    FS_ERROR_NOT_A_FILE,
    FS_ERROR_NOT_A_DIRECTORY,
    FS_ERROR_NONEXISTENT
  }

  private static final long serialVersionUID = -2828062375812503115L;

  static @Nonnull FilesystemError archiveAlreadyMounted(
    final String archive,
    final String mount)
  {
    return new FilesystemError(
      Code.FS_ERROR_ARCHIVE_ALREADY_MOUNTED,
      "archive '" + archive + "' is already mounted at '" + mount + "'");
  }

  static @Nonnull FilesystemError archiveDamaged(
    final String archive,
    final String message)
  {
    return new FilesystemError(Code.FS_ERROR_ARCHIVE_DAMAGED, "archive '"
      + archive
      + "' appears to be corrupt - "
      + message);
  }

  static @Nonnull FilesystemError archiveNoDirectory(
    final String file)
  {
    return new FilesystemError(
      Code.FS_ERROR_ARCHIVE_NO_DIRECTORY,
      "requested to load archive '"
        + file
        + "' but no archive directory was specified");
  }

  static @Nonnull FilesystemError archiveNonexistent(
    final String file)
  {
    return new FilesystemError(Code.FS_ERROR_ARCHIVE_NONEXISTENT, "archive '"
      + file
      + "' does not exist");
  }

  static @Nonnull FilesystemError archiveTypeUnsupported(
    final String file)
  {
    return new FilesystemError(
      Code.FS_ERROR_ARCHIVE_TYPE_UNSUPPORTED,
      "no handler for file '" + file + "'");
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

  static @Nonnull FilesystemError isFile(
    final String path)
  {
    return new FilesystemError(Code.FS_ERROR_IS_A_FILE, "file '"
      + path
      + "' is a file");
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
