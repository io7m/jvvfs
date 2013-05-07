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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.FilesystemError.Code;

abstract public class FilesystemAPIContract
{
  abstract @Nonnull FilesystemAPI makeFilesystem(
    final @Nonnull PathReal path)
    throws FilesystemError,
      IOException,
      ConstraintError;

  abstract @Nonnull FilesystemAPI makeFilesystem()
    throws FilesystemError,
      IOException,
      ConstraintError;

  /**
   * Initializing a filesystem with a nonexistent archive directory fails.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemBadArchiveDir()
      throws ConstraintError,
        FilesystemError,
        IOException
  {
    try {
      this.makeFilesystem(new PathReal("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  /**
   * Creating virtual directories works.
   */

  @Test public void testFilesystemCreateDirectory()
    throws ConstraintError,
      FilesystemError,
      IOException
  {
    final FilesystemAPI fs = this.makeFilesystem();
    fs.createDirectory(new PathVirtual("/dev"));
    fs.createDirectory(new PathVirtual("/usr/local/lib"));
  }

  /**
   * Virtual directories are directories.
   */

  @Test public void testFilesystemCreatedIsDirectory()
    throws ConstraintError,
      FilesystemError,
      IOException
  {
    final FilesystemAPI fs = this.makeFilesystem();

    fs.createDirectory(new PathVirtual("/usr/local/lib"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));

    fs.createDirectory("/opt/local/lib");
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/opt")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/opt/local")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/opt/local/lib")));
  }

  /**
   * Virtual directories inside mounts are directories.
   */

  @Test public void testFilesystemDirectoryIsDirectoryCorrect()
    throws ConstraintError,
      IOException,
      FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    fs.createDirectory(new PathVirtual("/usr/local/lib"));
    fs.mount("single-file", new PathVirtual("/usr"));

    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));
  }

  /**
   * Files inside mounts are files.
   */

  @Test public void testFilesystemDirectoryIsFileCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.mount("single-file", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertFalse(fs.isFile(new PathVirtual("/")));
    Assert.assertTrue(fs.isFile(new PathVirtual("/file.txt")));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/file.txt")));
  }

  /**
   * Files inside non-root mounts are files.
   */

  @Test public void testFilesystemDirectoryIsFileCorrectNonRootMount()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.createDirectory("/xyz");
      fs.mount("single-file", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertFalse(fs.isFile(new PathVirtual("/xyz")));
    Assert.assertTrue(fs.isFile(new PathVirtual("/xyz/file.txt")));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/xyz/file.txt")));
  }

  /**
   * Files inside mounts are files (String version).
   */

  @Test public void testFilesystemDirectoryIsFileCorrectString()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.mount("single-file", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertFalse(fs.isFile("/"));
    Assert.assertTrue(fs.isFile("/file.txt"));
    Assert.assertFalse(fs.isDirectory("/file.txt"));
  }

  /**
   * Opened files have the correct contents.
   */

  @SuppressWarnings({ "resource" }) @Test public
    void
    testFilesystemDirectoryOpenFileCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.mount("single-file", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello zip.", line);
  }

  /**
   * Opened files have the correct contents in non-root mounts.
   */

  @SuppressWarnings({ "resource" }) @Test public
    void
    testFilesystemDirectoryOpenFileCorrectNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.createDirectory("/xyz");
      fs.mount("single-file", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/xyz/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello zip.", line);
  }

  /**
   * Opening directories as files is an error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryOpenFileDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.mount("single-file-and-subdir", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      assert fs != null;
      fs.openFile(new PathVirtual("/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  /**
   * Opening directories as files in non-root mounts is an error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryOpenFileDirectoryNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.createDirectory("/xyz");
      fs.mount("single-file-and-subdir", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      assert fs != null;
      fs.openFile(new PathVirtual("/xyz/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  /**
   * Opening nonexistent files inside directories in mounted archives is an
   * error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryOpenFileNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.mount("single-file-and-subdir", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      assert fs != null;
      fs.openFile(new PathVirtual("/subdir/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Opening nonexistent files inside directories in non-root mounted archives
   * is an error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryOpenFileNonexistentNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.createDirectory("/xyz");
      fs.mount("single-file-and-subdir", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      assert fs != null;
      fs.openFile(new PathVirtual("/xyz/subdir/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Opening files in non-root directory mounts works.
   */

  @SuppressWarnings({ "resource" }) @Test public
    void
    testFilesystemDirectoryOpenFileNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.createDirectory("/xyz");
      fs.mount("single-file", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/xyz/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello zip.", line);
  }

  /**
   * Mounting directories over other directories works (and provides the
   * correct files).
   */

  @SuppressWarnings({ "resource" }) @Test public
    void
    testFilesystemDirectoryOpenFileOverlayCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.mount("single-file", new PathVirtual("/"));
      fs.mount("single-file-and-subdir", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello two.zip.", line);
  }

  /**
   * Mounting directories over other directories works for non-root mounts
   * (and provides the correct files).
   */

  @SuppressWarnings({ "resource" }) @Test public
    void
    testFilesystemDirectoryOpenFileOverlayCorrectNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.createDirectory("/xyz");
      fs.mount("single-file", new PathVirtual("/xyz"));
      fs.mount("single-file-and-subdir", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/xyz/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello two.zip.", line);
  }

  /**
   * Opening the root directory as a file fails.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryOpenFileRoot()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      f.mount("single-file", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.openFile(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  /**
   * File sizes are correct.
   */

  @Test public void testFilesystemDirectorySizeCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      f.mount("single-file", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    final long size = f.fileSize(new PathVirtual("/file.txt"));
    Assert.assertEquals(11, size);
  }

  /**
   * File sizes are correct in non-root mounts.
   */

  @Test public void testFilesystemDirectorySizeCorrectNonRootMount()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      f.createDirectory("/xyz");
      f.mount("single-file", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    final long size = f.fileSize(new PathVirtual("/xyz/file.txt"));
    Assert.assertEquals(11, size);
  }

  /**
   * Taking the size of a directory is an error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectorySizeDirectory()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      f.mount("single-file-and-subdir", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.fileSize(new PathVirtual("/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  /**
   * Taking the size of a directory in a non-root mount is an error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectorySizeDirectoryNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      f.createDirectory("/xyz");
      f.mount("single-file-and-subdir", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.fileSize(new PathVirtual("/xyz/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  /**
   * Taking the size of nonexistent file is an error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectorySizeNonexistent()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      f.mount("single-file-and-subdir", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.fileSize(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Taking the size of nonexistent file in a non-root mount is an error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectorySizeNonexistentNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      f.createDirectory("/xyz");
      f.mount("single-file-and-subdir", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.fileSize(new PathVirtual("/xyz/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Touching a directory is an error.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemTouchIsDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    try {
      final FilesystemAPI fs =
        this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.touch("/", 0x10203040);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  /**
   * Touching a file changes the modification time.
   */

  @Test public void testFilesystemTouchModificationTime()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
    fs.mount("single-file", new PathVirtual("/"));

    fs.touch("/file.txt", 0x10203040);
    Assert.assertEquals(0x10203040, fs.modificationTime("/file.txt"));
    fs.touch("/file.txt", 0x10203041);
    Assert.assertEquals(0x10203041, fs.modificationTime("/file.txt"));
    fs.touch("/file.txt", 0x10203042);
    Assert.assertEquals(0x10203042, fs.modificationTime("/file.txt"));
  }

  /**
   * Touching a file, and then mounting a file over the top of that file shows
   * the new modification time.
   */

  @Test public void testFilesystemTouchModificationTimeChanged()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
    fs.mount("single-file", new PathVirtual("/"));

    final long time = fs.modificationTime("/file.txt");
    fs.touch("/file.txt", 0x10203040);
    Assert.assertEquals(0x10203040, fs.modificationTime("/file.txt"));

    fs.mount("single-file.zip", new PathVirtual("/"));
    Assert.assertFalse(time == fs.modificationTime("/file.txt"));
    Assert.assertFalse(0x10203040 == fs.modificationTime("/file.txt"));
  }

  /**
   * Touching a nonexistent file fails.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemTouchNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    try {
      final FilesystemAPI fs =
        this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
      fs.touch("/nonexistent", 0x10203040);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Created directories appear in directory listings.
   */

  @Test public void testFilesystemListMkdir()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/a");
      f.createDirectory("/b");
      f.createDirectory("/c");
      f.createDirectory("/a/x");
      f.createDirectory("/b/y");
      f.createDirectory("/c/z");
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    } catch (final ConstraintError e) {
      Assert.fail(e.getMessage());
    }

    items.clear();
    f.listDirectory(new PathVirtual("/"), items);
    Assert.assertEquals(3, items.size());
    Assert.assertTrue(items.contains("a"));
    Assert.assertTrue(items.contains("b"));
    Assert.assertTrue(items.contains("c"));

    items.clear();
    f.listDirectory(new PathVirtual("/a"), items);
    Assert.assertEquals(1, items.size());
    Assert.assertTrue(items.contains("x"));

    items.clear();
    f.listDirectory(new PathVirtual("/b"), items);
    Assert.assertEquals(1, items.size());
    Assert.assertTrue(items.contains("y"));

    items.clear();
    f.listDirectory(new PathVirtual("/c"), items);
    Assert.assertEquals(1, items.size());
    Assert.assertTrue(items.contains("z"));
  }

  /**
   * Mounting a nonexistent archive fails.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountBadArchive()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.mount("/nonexistent", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  /**
   * Mounting an archive in a subdirectory of another archives, without
   * explicitly creating the directory, works.
   */

  @Test public void testFilesystemMountInMount()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    fs.mount("single-file-and-subdir", new PathVirtual("/"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir")));
    fs.mount("single-file-and-subdir", new PathVirtual("/subdir"));

    System.out.println(fs);
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir/subdir")));
  }

  /**
   * Mounting an archive in a nonexistent directory fails.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountNoMountpoint()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.mount("single-file.zip", new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  /**
   * Mounting archives in a manually created directory works.
   */

  @Test public void testFilesystemMountOK()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    fs.createDirectory(new PathVirtual("/empty"));
    fs.mount("single-file", new PathVirtual("/empty"));
    fs.mount("single-file.zip", new PathVirtual("/empty"));
  }

  /**
   * Mounting archives in a manually created directory works (String version).
   */

  @Test public void testFilesystemMountOKString()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    fs.createDirectory("/empty");
    fs.mount("single-file", "/empty");
    fs.mount("single-file.zip", "/empty");
  }

  /**
   * Creating a directory in the real filesystem exists when queried in the
   * virtual filesystem.
   */

  @Test public void testFilesystemMountedIsDirectoryExternalMakeDir()
    throws ConstraintError,
      IOException,
      FilesystemError
  {
    final File archive_dir = TestData.getTestDataDirectory();
    final File new_dir = new File(archive_dir, "local");
    new_dir.delete();

    try {
      final FilesystemAPI fs =
        this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

      fs.createDirectory(new PathVirtual("/usr/local/lib"));
      fs.mount("single-file", new PathVirtual("/usr"));

      Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));

      final boolean ok = new_dir.mkdir();
      Assert.assertTrue(ok);
      Assert.assertTrue(new_dir.isDirectory());

      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));
    } finally {
      new_dir.delete();
    }
  }

  /**
   * A manually created directory that is overriden by a mounted archive is be
   * treated as a directory.
   */

  @Test public void testFilesystemMountedIsDirectoryOverriddenFile()
    throws ConstraintError,
      IOException,
      FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    fs.createDirectory(new PathVirtual("/file.txt"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/file.txt")));

    fs.mount("single-file", new PathVirtual("/"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/file.txt")));

    try {
      fs.isDirectory(new PathVirtual("/file.txt/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
    }
  }

  /**
   * Unmounting works.
   */

  @Test public void testFilesystemUnmountCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.mount("single-file-and-subdir", new PathVirtual("/"));
      fs.mount("single-file", new PathVirtual("/"));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir")));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      fs.unmount(new PathVirtual("/"));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir")));
      fs.unmount(new PathVirtual("/"));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
      Assert.assertFalse(fs.isDirectory(new PathVirtual("/subdir")));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_MOUNTED, e.code);
      throw e;
    }
  }

  /**
   * Unmounting works (String version).
   */

  @Test public void testFilesystemUnmountCorrectString()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.mount("single-file-and-subdir", "/");
      fs.mount("single-file", "/");
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir")));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      fs.unmount("/");
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir")));
      fs.unmount("/");
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
      Assert.assertFalse(fs.isDirectory(new PathVirtual("/subdir")));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_MOUNTED, e.code);
      throw e;
    }
  }

  /**
   * Unmounting something that isn't mounted fails.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemUnmountNotMountedRoot()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.unmount(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_MOUNTED, e.code);
      throw e;
    }
  }

  /**
   * Unmounting doesn't fail.
   */

  @Test public void testFilesystemDirectoryUnmountCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    f.mount("single-file", new PathVirtual("/"));
    f.unmount(new PathVirtual("/"));
  }

  /**
   * Unmounting a mount that is "busy" (as in providing a directory for
   * another mount), fails.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountBusy()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      f.createDirectory(new PathVirtual("/usr"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertFalse(f.isDirectory(new PathVirtual("/usr/subdir")));
      f.mount("single-file-and-subdir", new PathVirtual("/usr"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr/subdir")));
      f.mount("single-file-and-subdir", new PathVirtual("/usr/subdir"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr/subdir")));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr/subdir/subdir")));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.unmount(new PathVirtual("/usr/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_BUSY, e.code);
      throw e;
    }
  }

  /**
   * Colliding mounts are an error.
   */

  @Test public void testFilesystemMountCollision()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    fs.createDirectory(new PathVirtual("/file.txt/lib"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/file.txt")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/file.txt/lib")));

    fs.mount("single-file", new PathVirtual("/"));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/file.txt")));

    try {
      fs.isDirectory(new PathVirtual("/file.txt/lib"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
    }

    try {
      fs.mount("single-file", new PathVirtual("/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
    }
  }

  /**
   * Mounting an archive multiple times in the same place fails.
   */

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountDuplicate()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final FilesystemAPI fs =
      this.makeFilesystem(new PathReal(TestData.getTestDataDirectory()));

    try {
      fs.mount("single-file", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      fs.mount("single-file", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_DUPLICATE_MOUNT, e.code);
      throw e;
    }
  }
}
