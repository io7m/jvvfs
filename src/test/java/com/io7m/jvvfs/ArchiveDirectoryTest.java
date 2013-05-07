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
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;

public class ArchiveDirectoryTest
{
  /**
   * Trying to retrieve the size of a directory is an error.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileSizeDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r0 = new PathReal(new File(tempdir, "single-file"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r0, new PathVirtual("/"), TestData.getLog());

    try {
      z0.fileSize(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_IS_A_DIRECTORY,
        e.code);
      throw e;
    }
  }

  /**
   * Retrieving the size of a nonexistent file is an error.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileSizeNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r0 = new PathReal(new File(tempdir, "single-file"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r0, new PathVirtual("/"), TestData.getLog());

    try {
      z0.fileSize(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Trying to list a directory that is not a directory is an error.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testListDirectoryNotFile()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r0 = new PathReal(new File(tempdir, "single-file"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r0, new PathVirtual("/"), TestData.getLog());

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

  /**
   * Trying to take the modification time of a nonexistent directory is an
   * error.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testModTimeNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r0 = new PathReal(new File(tempdir, "single-file"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r0, new PathVirtual("/"), TestData.getLog());

    try {
      z0.modificationTime(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Trying to take the modification time of a directory is an error.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testModTimeNotFile()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r0 = new PathReal(new File(tempdir, "single-file"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r0, new PathVirtual("/"), TestData.getLog());

    try {
      z0.modificationTime(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_IS_A_DIRECTORY,
        e.code);
      throw e;
    }
  }

  /**
   * Trying to open a file that is actually a directory is an error.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testOpenFileDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r0 = new PathReal(new File(tempdir, "single-file"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r0, new PathVirtual("/"), TestData.getLog());

    try {
      z0.openFile(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_IS_A_DIRECTORY,
        e.code);
      throw e;
    }
  }

  /**
   * Trying to open a nonexistent file is an error.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testOpenFileNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r0 = new PathReal(new File(tempdir, "single-file"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r0, new PathVirtual("/"), TestData.getLog());

    try {
      z0.openFile(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Archive directory string varies with different mounts.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testToStringDifferent()
      throws IOException,
        ConstraintError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r0 = new PathReal(new File(tempdir, "single-file"));
    final PathReal r1 =
      new PathReal(new File(tempdir, "single-file-with-subdir"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r0, new PathVirtual("/"), TestData.getLog());
    final ArchiveDirectory z1 =
      new ArchiveDirectory(r1, new PathVirtual("/"), TestData.getLog());

    Assert.assertFalse(z0.toString().equals(z1.toString()));
  }

  /**
   * Archive directory string does not vary with identical mounts.
   */

  @SuppressWarnings("static-method") @Test public void testToStringSame()
    throws IOException,
      ConstraintError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r = new PathReal(new File(tempdir, "single-file"));

    final ArchiveDirectory z0 =
      new ArchiveDirectory(r, new PathVirtual("/"), TestData.getLog());
    final ArchiveDirectory z1 =
      new ArchiveDirectory(r, new PathVirtual("/"), TestData.getLog());

    Assert.assertTrue(z0.toString().equals(z1.toString()));
  }
}
