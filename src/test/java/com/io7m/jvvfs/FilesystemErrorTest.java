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
