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

import org.junit.Assert;
import org.junit.Test;

public class FilesystemErrorTest
{
  @SuppressWarnings("static-method") @Test public void testBrokenArchive()
  {
    try {
      throw FilesystemError.brokenArchive("file.zip", "Broken");
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_ARCHIVE_DAMAGED,
        e.code);
    }
  }

  @SuppressWarnings("static-method") @Test public void testBusy()
  {
    try {
      throw FilesystemError.busy("Busy");
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_BUSY, e.code);
    }
  }

  @SuppressWarnings("static-method") @Test public void testDuplicateMount()
  {
    try {
      throw FilesystemError.duplicateMount("file.zip", "/");
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_DUPLICATE_MOUNT,
        e.code);
    }
  }

  @SuppressWarnings("static-method") @Test public void testFileNotFound()
  {
    try {
      throw FilesystemError.fileNotFound("file.zip");
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
    }
  }

  @SuppressWarnings("static-method") @Test public void testIOError()
  {
    try {
      throw FilesystemError.ioError("file.zip", "I/O error");
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_IO_ERROR, e.code);
    }
  }

  @SuppressWarnings("static-method") @Test public void testIsDirectory()
  {
    try {
      throw FilesystemError.isDirectory("directory");
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_IS_A_DIRECTORY,
        e.code);
    }
  }

  @SuppressWarnings("static-method") @Test public void testNotDirectory()
  {
    try {
      throw FilesystemError.notDirectory("file.zip");
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_NOT_A_DIRECTORY,
        e.code);
    }
  }

  @SuppressWarnings("static-method") @Test public void testNotMounted()
  {
    try {
      throw FilesystemError.notMounted("file.zip");
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NOT_MOUNTED, e.code);
    }
  }

  @SuppressWarnings("static-method") @Test public void testUnhandledType()
  {
    try {
      throw FilesystemError.unhandledType("unhandled");
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_UNHANDLED_TYPE,
        e.code);
    }
  }
}
