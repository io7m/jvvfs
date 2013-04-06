package com.io7m.jvvfs;

import java.io.IOException;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jlog.Log;

public class ArchiveDirectoryTest
{
  private static Log getLog()
    throws IOException,
      ConstraintError
  {
    final Log log =
      new Log(
        PropertyUtils.loadFromFile("io7m-jvvfs.properties"),
        "com.io7m.jvvfs",
        "main");
    return log;
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileSizeDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    try {
      z0.fileSize(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_IS_A_DIRECTORY,
        e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileSizeNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    try {
      z0.fileSize(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testListDirectoryNotFile()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    try {
      final TreeSet<String> items = new TreeSet<String>();
      z0.listDirectory(new PathVirtual("/file.txt"), items);
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_NOT_A_DIRECTORY,
        e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testModTimeNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    try {
      z0.modificationTime(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testModTimeNotFile()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    try {
      z0.modificationTime(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_IS_A_DIRECTORY,
        e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testOpenFileDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    try {
      z0.openFile(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_IS_A_DIRECTORY,
        e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testOpenFileNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    try {
      z0.openFile(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testToStringDifferent()
      throws IOException,
        ConstraintError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final PathReal r1 = new PathReal("test-archives/archive1");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());
    final ArchiveDirectory z1 =
      new ArchiveDirectory(
        r1,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    Assert.assertFalse(z0.toString().equals(z1.toString()));
  }

  @SuppressWarnings("static-method") @Test public void testToStringSame()
    throws IOException,
      ConstraintError
  {
    final PathReal r = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());
    final ArchiveDirectory z1 =
      new ArchiveDirectory(
        r,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    Assert.assertTrue(z0.toString().equals(z1.toString()));
  }
}
