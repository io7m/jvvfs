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

/**
 * <p>
 * Exception type raised during filesystem operations.
 * </p>
 */

public final class FilesystemError extends Exception
{
  /**
   * The set of possible error codes reported by the filesystem.
   */

  public static enum Code
  {
    /**
     * The user tried to load an archive twice at the same location.
     */

    FS_ERROR_ARCHIVE_ALREADY_MOUNTED,

    /**
     * An archive appeared to be corrupt and could not be loaded.
     */

    FS_ERROR_ARCHIVE_DAMAGED,

    /**
     * The user tried to load an archive, but no archive directory was
     * specified.
     */

    FS_ERROR_ARCHIVE_NO_DIRECTORY,

    /**
     * The user tried to load a nonexistent archive.
     */

    FS_ERROR_ARCHIVE_NONEXISTENT,

    /**
     * The user tried to load an archive of a type not supported by the
     * implementation.
     */

    FS_ERROR_ARCHIVE_TYPE_UNSUPPORTED,

    /**
     * An internal constraint error occurred; this indicates a bug in
     * <code>jvvfs</code>.
     */

    FS_ERROR_CONSTRAINT_ERROR,

    /**
     * An operating system I/O error occurred.
     */

    FS_ERROR_IO_ERROR,

    /**
     * The object in question does not exist.
     */

    FS_ERROR_NONEXISTENT,

    /**
     * The object in question was expected to be a directory, but was a
     * something else instead.
     */

    FS_ERROR_NOT_A_DIRECTORY,

    /**
     * The object in question was expected to be a file, but was a something
     * else instead.
     */

    FS_ERROR_NOT_A_FILE
  }

  private static final long serialVersionUID = -2828062375812503115L;

  static FilesystemError archiveAlreadyMounted(
    final String archive,
    final String mount)
  {
    return new FilesystemError(
      Code.FS_ERROR_ARCHIVE_ALREADY_MOUNTED,
      "archive '" + archive + "' is already mounted at '" + mount + "'");
  }

  static FilesystemError archiveDamaged(
    final String archive,
    final String message)
  {
    return new FilesystemError(Code.FS_ERROR_ARCHIVE_DAMAGED, "archive '"
      + archive
      + "' appears to be corrupt - "
      + message);
  }

  static FilesystemError archiveNoDirectory(
    final String file)
  {
    return new FilesystemError(
      Code.FS_ERROR_ARCHIVE_NO_DIRECTORY,
      "requested to load archive '"
        + file
        + "' but no archive directory was specified");
  }

  static FilesystemError archiveNonexistent(
    final String file)
  {
    return new FilesystemError(Code.FS_ERROR_ARCHIVE_NONEXISTENT, "archive '"
      + file
      + "' does not exist");
  }

  static FilesystemError archiveTypeUnsupported(
    final String file)
  {
    return new FilesystemError(
      Code.FS_ERROR_ARCHIVE_TYPE_UNSUPPORTED,
      "no handler for file '" + file + "'");
  }

  static FilesystemError fileNotFound(
    final String name)
  {
    return new FilesystemError(Code.FS_ERROR_NONEXISTENT, "file not found '"
      + name
      + "'");
  }

  static FilesystemError ioError(
    final Exception e)
  {
    return new FilesystemError(e);
  }

  static FilesystemError notDirectory(
    final String path)
  {
    return new FilesystemError(Code.FS_ERROR_NOT_A_DIRECTORY, "file '"
      + path
      + "' is not a directory");
  }

  static FilesystemError notFile(
    final String path)
  {
    return new FilesystemError(Code.FS_ERROR_NOT_A_FILE, "directory '"
      + path
      + "' is not a file");
  }

  private final Code code;

  FilesystemError(
    final Code in_code,
    final String message)
  {
    super(message);
    this.code = in_code;
  }

  FilesystemError(
    final Exception cause)
  {
    super(cause);
    this.code = Code.FS_ERROR_IO_ERROR;
  }

  /**
   * @return The error code associated with the exception.
   */

  public Code getCode()
  {
    return this.code;
  }
}
