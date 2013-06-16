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
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nonnull;

import net.java.quickcheck.Characteristic;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.FilesystemError.Code;

public class FilesystemTest
{
  private static @Nonnull Filesystem makeFS()
    throws FileNotFoundException,
      IOException,
      ConstraintError
  {
    final File dir = TestData.getTestDataDirectory();
    final Filesystem fs =
      Filesystem.makeWithArchiveDirectory(
        TestData.getLog(),
        new PathReal(dir.toString()));
    return fs;
  }

  static void runWithNameGenerator(
    final Characteristic<String> c)
  {
    QuickCheck.forAll(new NameTest.ValidNameGenerator(), c);
  }

  @SuppressWarnings("static-method") @Test public
    void
    testCreateDirectoryNonexistent()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    FilesystemTest.runWithNameGenerator(new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final @Nonnull String name)
        throws Throwable
      {
        final PathVirtual path = PathVirtual.ofString("/" + name);
        fs.createDirectory(path);
        Assert.assertTrue(fs.isDirectory(path));
        Assert.assertTrue(fs.exists(path));
        Assert.assertFalse(fs.isFile(path));
      }
    });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testCreateDirectorySubdirectoriesNonexistent()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual path)
          throws Throwable
        {
          fs.createDirectory(path);
          Assert.assertTrue(fs.isDirectory(path));
          Assert.assertTrue(fs.exists(path));
          Assert.assertFalse(fs.isFile(path));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testExistsNonexistent()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    FilesystemTest.runWithNameGenerator(new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final @Nonnull String name)
        throws Throwable
      {
        Assert.assertFalse(fs.exists(PathVirtual.ofString("/" + name)));
      }
    });
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testExistsNonexistentParent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    try {
      fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.exists(PathVirtual.ofString("/subdir/nonexistent/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testExistsNull()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.exists(null);
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsDirectoryNonexistent()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    FilesystemTest.runWithNameGenerator(new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final @Nonnull String name)
        throws Throwable
      {
        Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/" + name)));
      }
    });
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testIsDirectoryNonexistentParent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    try {
      fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.isDirectory(PathVirtual.ofString("/subdir/nonexistent/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsDirectoryNotDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.mountArchive("single-file.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/file.txt")));
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testIsDirectoryNull()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.isDirectory(null);
  }

  @SuppressWarnings("static-method") @Test public void testIsFileFile()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.mountArchive("single-file.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file.txt")));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsFileNonexistent()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    FilesystemTest.runWithNameGenerator(new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final @Nonnull String name)
        throws Throwable
      {
        Assert.assertFalse(fs.isFile(PathVirtual.ofString("/" + name)));
      }
    });
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testIsFileNonexistentParent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    try {
      fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.isFile(PathVirtual.ofString("/subdir/nonexistent/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test public void testIsFileNotFile()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/subdir")));
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testIsFileNull()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.isFile(null);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMakeWithArchivesNullDirectory()
      throws ConstraintError,
        IOException
  {
    Filesystem.makeWithArchiveDirectory(TestData.getLog(), null);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMakeWithArchivesNullLog()
      throws ConstraintError
  {
    Filesystem.makeWithArchiveDirectory(null, new PathReal("nonexistent"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testMountArchiveAtArchiveDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);

    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file.txt")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file3.txt")));

    fs.mountArchive("files1-3.zip", PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/subdir/file3.txt")));
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testMountArchiveAtFile()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("single-file.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.mountArchive("files1-3.zip", PathVirtual.ofString("/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testMountArchiveAtFileAncestor()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("single-file.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.mountArchive(
        "files1-3.zip",
        PathVirtual.ofString("/file.txt/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMountArchiveClasspathNullArchive()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountClasspathArchive(null, PathVirtual.ROOT);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMountArchiveClasspathNullMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountClasspathArchive(FilesystemTest.class, null);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMountArchiveInvalidArchiveName()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("..", PathVirtual.ROOT);
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testMountArchiveInvalidArchiveType()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("unknown.unknown", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_ARCHIVE_TYPE_UNSUPPORTED, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testMountArchiveNonexistentArchive()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    FilesystemTest.runWithNameGenerator(new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final @Nonnull String a)
        throws Throwable
      {
        try {
          fs.mountArchive(a, PathVirtual.ROOT);
        } catch (final FilesystemError e) {
          Assert.assertEquals(Code.FS_ERROR_ARCHIVE_NONEXISTENT, e.code);
        }
      }
    });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testMountArchiveNonexistentDirectory()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    FilesystemTest.runWithNameGenerator(new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final @Nonnull String name)
        throws Throwable
      {
        try {
          fs.mountArchive("single-file.zip", PathVirtual.ofString("/" + name));
        } catch (final FilesystemError e) {
          Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
        }
      }
    });
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMountArchiveNullArchive()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive(null, PathVirtual.ROOT);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMountArchiveNullMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("xyz", null);
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testMountArchiveTwiceDuplicate()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("single-file.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.mountArchive("single-file.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_ARCHIVE_ALREADY_MOUNTED, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testMountArchiveTwiceNotDuplicate()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("files1-3.zip", PathVirtual.ROOT);

    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file3.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file4.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file5.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file6.txt")));

    fs.mountArchive("files4-6.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file3.txt")));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/file4.txt")));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/file5.txt")));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/file6.txt")));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testMountArchiveWithoutArchives()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs =
      Filesystem.makeWithoutArchiveDirectory(TestData.getLog());

    FilesystemTest.runWithNameGenerator(new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final @Nonnull String a)
        throws Throwable
      {
        try {
          fs.mountArchive(a, PathVirtual.ROOT);
        } catch (final FilesystemError e) {
          Assert.assertEquals(Code.FS_ERROR_ARCHIVE_NO_DIRECTORY, e.code);
        }
      }
    });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testMountClasspathArchive()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountClasspathArchive(FilesystemTest.class, PathVirtual.ROOT);
    Assert.assertTrue(fs.isFile(PathVirtual
      .ofString("/com/io7m/jvvfs/single-file.zip")));
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMountClasspathArchiveNullClass()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountClasspathArchive(null, PathVirtual.ROOT);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMountClasspathArchiveNullMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountClasspathArchive(FilesystemTest.class, null);
  }

  @SuppressWarnings("static-method") @Test public void testRootExists()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    Assert.assertTrue(fs.exists(PathVirtual.ROOT));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertFalse(fs.isFile(PathVirtual.ROOT));
  }
}
