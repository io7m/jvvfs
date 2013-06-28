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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Deque;
import java.util.SortedSet;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import net.java.quickcheck.Characteristic;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Pair;
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

  /**
   * Closing a filesystem with directories removes the directories.
   */

  @SuppressWarnings("static-method") @Test public void testCloseDirectories()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.createDirectory(PathVirtual.ofString("/a"));
    fs.createDirectory(PathVirtual.ofString("/b"));
    fs.createDirectory(PathVirtual.ofString("/c"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/b")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/c")));

    fs.close();
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/a")));
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/b")));
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/c")));
  }

  /**
   * Closing an empty filesystem has no observable effect.
   */

  @SuppressWarnings("static-method") @Test public void testCloseEmpty()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.close();
  }

  /**
   * Closing a filesystem with mounted archives removes the mounts.
   */

  @SuppressWarnings("static-method") @Test public void testCloseMounted()
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

    fs.close();
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/file1.txt")));
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/file2.txt")));
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/file3.txt")));
  }

  /**
   * Closing a filesystem with mounted archives removes the mounts.
   * 
   * (This test checks for concurrent modifications to the internal mount
   * map).
   */

  @SuppressWarnings("static-method") @Test public void testCloseMountedMap()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.createDirectory(PathVirtual.ofString("/a"));
    fs.createDirectory(PathVirtual.ofString("/b"));
    fs.createDirectory(PathVirtual.ofString("/c"));

    fs.mountArchive("single-file.zip", PathVirtual.ofString("/a"));
    fs.mountArchive("single-file.zip", PathVirtual.ofString("/b"));
    fs.mountArchive("single-file.zip", PathVirtual.ofString("/c"));
    fs.close();

    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/a")));
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/b")));
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/c")));
  }

  /**
   * Creating otherwise nonexistent directories works.
   */

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

  /**
   * Creating directories ensures that all ancestors exist and are
   * directories.
   */

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

          final PathVirtualEnum e = new PathVirtualEnum(path);
          while (e.hasMoreElements()) {
            final PathVirtual ancestor = e.nextElement();
            Assert.assertTrue(fs.isDirectory(ancestor));
            Assert.assertTrue(fs.exists(ancestor));
            Assert.assertFalse(fs.isFile(ancestor));
          }

          Assert.assertTrue(fs.isDirectory(path));
          Assert.assertTrue(fs.exists(path));
          Assert.assertFalse(fs.isFile(path));
        }
      });
  }

  /**
   * Nonexistent objects do not exist.
   */

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

  /**
   * Nonexistent parents are signalled when checking existence.
   */

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
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    }
  }

  /**
   * Passing <code>null</code> to {@link Filesystem#exists(PathVirtual)}
   * fails.
   */

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

  /**
   * Opening a file works.
   */

  @SuppressWarnings("static-method") @Test public void testFileOpenCorrect()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file.zip", PathVirtual.ROOT);

    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file.txt")));

    final InputStream s = fs.openFile(PathVirtual.ofString("/file.txt"));
    final BufferedReader b = new BufferedReader(new InputStreamReader(s));

    final String l = b.readLine();
    Assert.assertEquals("Hello zip.", l);
    s.close();
  }

  /**
   * Opening a directory fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileOpenDirectory()
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
      fs.openFile(PathVirtual.ofString("/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_FILE, e.getCode());
      throw e;
    }
  }

  /**
   * Opening a virtual directory fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileOpenDirectoryVirtual()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.createDirectory(PathVirtual.ofString("/bin"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.openFile(PathVirtual.ofString("/bin"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_FILE, e.getCode());
      throw e;
    }
  }

  /**
   * Opening a file with a file ancestor fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileOpenFileAncestor()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.openFile(PathVirtual.ofString("/subdir/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    }
  }

  /**
   * Opening a nonexistent file fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileOpenNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.openFile(PathVirtual.ofString("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    }
  }

  /**
   * Opening root within an archive, fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileOpenRootZip()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("complex.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.openFile(PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_FILE, e.getCode());
      throw e;
    }
  }

  /**
   * Retrieving the size of a file works.
   */

  @SuppressWarnings("static-method") @Test public void testFileSizeCorrect()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file.zip", PathVirtual.ROOT);

    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file.txt")));
    Assert
      .assertEquals(11, fs.getFileSize(PathVirtual.ofString("/file.txt")));
  }

  /**
   * Retrieving the size of a directory fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileSizeDirectory()
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
      fs.getFileSize(PathVirtual.ofString("/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_FILE, e.getCode());
      throw e;
    }
  }

  /**
   * Retrieving the size of a virtual directory fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileSizeDirectoryVirtual()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.createDirectory(PathVirtual.ofString("/bin"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.getFileSize(PathVirtual.ofString("/bin"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_FILE, e.getCode());
      throw e;
    }
  }

  /**
   * Retrieving the size of a file with a file ancestor fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileSizeFileAncestor()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.getFileSize(PathVirtual.ofString("/subdir/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    }
  }

  /**
   * Retrieving the size of a nonexistent file fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFileSizeNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.getFileSize(PathVirtual.ofString("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    }
  }

  /**
   * Checking if an object is a directory with a file ancestor fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testIsDirectoryFileAncestor()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.isDirectory(PathVirtual.ofString("/subdir/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    }
  }

  /**
   * Nonexistent objects are not directories.
   */

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

  /**
   * Nonexistent ancestors are signalled when checking directories.
   */

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
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    }
  }

  /**
   * Files are not directories.
   */

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

  /**
   * Passing <code>null</code> to {@link Filesystem#isDirectory(PathVirtual)}
   * fails.
   */

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

  /**
   * Files in archives are files.
   */

  @SuppressWarnings("static-method") @Test public void testIsFileFile()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.mountArchive("single-file.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file.txt")));
  }

  /**
   * Nonexistent objects are not files.
   */

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

  /**
   * Nonexistent ancestors are signalled when checking files.
   */

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
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    }
  }

  /**
   * Directories are not files.
   */

  @SuppressWarnings("static-method") @Test public void testIsFileNotFile()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/subdir")));
  }

  /**
   * Passing <code>null</code> to {@link Filesystem#isFile(PathVirtual)}
   * fails.
   */

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

  /**
   * Listing a created directory returns nothing.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testListCreatedDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.createDirectory(PathVirtual.ofString("/a"));

    {
      final SortedSet<String> items =
        fs.listDirectory(PathVirtual.ofString("/a"));
      Assert.assertEquals(0, items.size());
    }

    fs.mountArchive("files1-3.zip", PathVirtual.ofString("/a"));

    {
      final SortedSet<String> items =
        fs.listDirectory(PathVirtual.ofString("/a"));
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("file1.txt"));
      Assert.assertTrue(items.contains("file2.txt"));
      Assert.assertTrue(items.contains("file3.txt"));
    }

    fs.unmount(PathVirtual.ofString("/a"));

    {
      final SortedSet<String> items =
        fs.listDirectory(PathVirtual.ofString("/a"));
      Assert.assertEquals(0, items.size());
    }
  }

  /**
   * Listing a file fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testListFile()
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
      fs.listDirectory(PathVirtual.ofString("/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    }
  }

  /**
   * Listing a nonexistent object fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testListNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.listDirectory(PathVirtual.ofString("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    }
  }

  /**
   * Listing null fails.
   */

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testListNull()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    fs.listDirectory(null);
  }

  /**
   * Listing the root directory with a mounted archive works.
   */

  @SuppressWarnings("static-method") @Test public void testListRootBase()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("complex.zip", PathVirtual.ROOT);

    final SortedSet<String> items = fs.listDirectory(PathVirtual.ROOT);
    Assert.assertTrue(items.contains("a"));
    Assert.assertTrue(items.contains("b"));
    Assert.assertEquals(2, items.size());
  }

  /**
   * Listing the root directory of an empty filesystem returns nothing.
   */

  @SuppressWarnings("static-method") @Test public void testListRootEmpty()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    final SortedSet<String> items = fs.listDirectory(PathVirtual.ROOT);
    Assert.assertTrue(items.isEmpty());
  }

  /**
   * Complicated shadowing is respected.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testListShadowComplex0()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    /**
     * <ol>
     * <li>Create /subdir</li>
     * <li>Mount an archive into /subdir</li>
     * <li>Shadow /subdir with a file</li>
     * <li>Shadow the /subdir file with a directory</li>
     * <li>Check that /subdir is now empty</li>
     * <li>Mount an archive into /subdir</li>
     * <li>Check that /subdir contains only the new files</li>
     * </ol>
     */

    fs.createDirectory(PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));

    fs.mountArchive("files1-3.zip", PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file3.txt")));

    {
      final SortedSet<String> items =
        fs.listDirectory(PathVirtual.ofString("/subdir"));
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("file1.txt"));
      Assert.assertTrue(items.contains("file2.txt"));
      Assert.assertTrue(items.contains("file3.txt"));
    }

    fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir")));

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file3.txt")));

    {
      final SortedSet<String> items =
        fs.listDirectory(PathVirtual.ofString("/subdir"));
      Assert.assertEquals(1, items.size());
      Assert.assertTrue(items.contains("file.txt"));
    }

    fs.mountArchive("files4-6.zip", PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file3.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file4.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file5.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file6.txt")));

    {
      final SortedSet<String> items =
        fs.listDirectory(PathVirtual.ofString("/subdir"));
      Assert.assertEquals(4, items.size());
      Assert.assertTrue(items.contains("file.txt"));
      Assert.assertTrue(items.contains("file4.txt"));
      Assert.assertTrue(items.contains("file5.txt"));
      Assert.assertTrue(items.contains("file6.txt"));
    }
  }

  /**
   * Listing a directory takes the union of the directory contents with
   * respect to mounted archives.
   */

  @SuppressWarnings("static-method") @Test public void testListUnion()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("files1-3.zip", PathVirtual.ROOT);
    fs.mountArchive("files4-6.zip", PathVirtual.ROOT);

    final SortedSet<String> items = fs.listDirectory(PathVirtual.ROOT);
    Assert.assertEquals(6, items.size());
    Assert.assertTrue(items.contains("file1.txt"));
    Assert.assertTrue(items.contains("file2.txt"));
    Assert.assertTrue(items.contains("file3.txt"));
    Assert.assertTrue(items.contains("file4.txt"));
    Assert.assertTrue(items.contains("file5.txt"));
    Assert.assertTrue(items.contains("file6.txt"));
  }

  /**
   * Passing <code>null</code> as an archive directory fails.
   */

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMakeWithArchivesNullDirectory()
      throws ConstraintError,
        IOException
  {
    Filesystem.makeWithArchiveDirectory(TestData.getLog(), null);
  }

  /**
   * Passing <code>null</code> as a log interface fails.
   */

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testMakeWithArchivesNullLog()
      throws ConstraintError
  {
    Filesystem.makeWithArchiveDirectory(null, new PathReal("nonexistent"));
  }

  /**
   * Retrieving the modification time of a directory works.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testModificationTimeDirectoryCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);

    final Calendar ct =
      fs.getModificationTime(PathVirtual.ofString("/subdir"));
    Assert.assertEquals(ct.get(Calendar.YEAR), 2012);
    Assert.assertEquals(ct.get(Calendar.MONTH), 0);
    Assert.assertEquals(ct.get(Calendar.DAY_OF_MONTH), 23);
    Assert.assertEquals(ct.get(Calendar.HOUR_OF_DAY), 21);
    Assert.assertEquals(ct.get(Calendar.MINUTE), 50);
  }

  /**
   * Retrieving the modification time of a created directory works.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testModificationTimeDirectoryVirtualCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    final Calendar cnow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final long cnow_t = cnow.getTime().getTime();
    fs.createDirectory(PathVirtual.ofString("/bin"));

    final Calendar cdir =
      fs.getModificationTime(PathVirtual.ofString("/bin"));
    final long cdir_t = cdir.getTime().getTime();

    final long diff = Math.abs(cdir_t - cnow_t);
    Assert.assertTrue(diff < 2);
  }

  /**
   * Retrieving the size of a file with a file ancestor fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testModificationTimeFileAncestor()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    try {
      fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      fs.getModificationTime(PathVirtual.ofString("/subdir/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    }
  }

  /**
   * Retrieving the modification time of a file works.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testModificationTimeFileCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file.zip", PathVirtual.ROOT);

    final Calendar ct =
      fs.getModificationTime(PathVirtual.ofString("/file.txt"));
    Assert.assertEquals(ct.get(Calendar.YEAR), 2012);
    Assert.assertEquals(ct.get(Calendar.MONTH), 0);
    Assert.assertEquals(ct.get(Calendar.DAY_OF_MONTH), 20);
    Assert.assertEquals(ct.get(Calendar.HOUR_OF_DAY), 21);
    Assert.assertEquals(ct.get(Calendar.MINUTE), 47);
  }

  /**
   * Retrieving the modification time of a nonexistent file fails.
   */

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testModificationTimeNonexistent()
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
      fs.getModificationTime(PathVirtual.ofString("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    }
  }

  /**
   * Retrieving the modification time of root within an archive, works.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testModificationTimeRootZip()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("complex.zip", PathVirtual.ROOT);

    final Calendar cnow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final long cnow_t = cnow.getTime().getTime();
    final Calendar cdir = fs.getModificationTime(PathVirtual.ROOT);
    final long cdir_t = cdir.getTime().getTime();

    final long diff = Math.abs(cdir_t - cnow_t);
    System.out.println("diff: " + diff);

    Assert.assertTrue(diff < 2000);
  }

  /**
   * Mounted archives can be mounted at directories provided by other
   * archives, and display their contents correctly.
   */

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

  /**
   * Attempting to mount an archive at a path that denotes a file, fails.
   */

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
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    }
  }

  /**
   * Attempting to mount an archive at a path that has an ancestor that is a
   * file, fails.
   */

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
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    }
  }

  /**
   * Passing <code>null</code> to
   * {@link Filesystem#mountClasspathArchive(Class, PathVirtual)} fails.
   */

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

  /**
   * Passing <code>null</code> to
   * {@link Filesystem#mountClasspathArchive(Class, PathVirtual)} fails.
   */

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

  /**
   * Mounting an archive that hides an existing directory with a file, makes
   * the hidden directory inaccessible.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testMountArchiveFileShadows()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);

    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));

    fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);

    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir")));

    try {
      fs.exists(PathVirtual.ofString("/subdir/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
    }
  }

  /**
   * Mounting an archive that hides an existing directory with a file, makes
   * mounted archives inaccessible.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testMountArchiveFileShadowsMounts()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));

    fs.mountArchive(
      "single-file-and-subdir.zip",
      PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual
      .ofString("/subdir/subdir/file.txt")));

    fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir")));

    try {
      fs.exists(PathVirtual.ofString("/subdir/file.txt"));
      throw new UnreachableCodeException();
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
    }

    try {
      fs.unmount(PathVirtual.ofString("/subdir"));
      throw new UnreachableCodeException();
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
    }
  }

  /**
   * Mounting an archive that hides an existing file with a directory, makes
   * the hidden file inaccessible.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testMountArchiveFileShadowsReverse()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);

    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir")));

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);

    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));
  }

  /**
   * Passing an invalid archive name fails.
   */

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

  /**
   * Trying to mount an archive of an unsupported type fails.
   */

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
      Assert
        .assertEquals(Code.FS_ERROR_ARCHIVE_TYPE_UNSUPPORTED, e.getCode());
      throw e;
    }
  }

  /**
   * Trying to mount a nonexistent archive fails.
   */

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
          Assert.assertEquals(Code.FS_ERROR_ARCHIVE_NONEXISTENT, e.getCode());
        }
      }
    });
  }

  /**
   * Trying to mount an archive at a nonexistent directory fails.
   */

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
          Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
        }
      }
    });
  }

  /**
   * Passing <code>null</code> to
   * {@link Filesystem#mountArchive(String, PathVirtual)} fails.
   */

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

  /**
   * Passing <code>null</code> to
   * {@link Filesystem#mountArchive(String, PathVirtual)} fails.
   */

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

  /**
   * Trying to mount an archive twice at the same location fails.
   */

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
      Assert.assertEquals(Code.FS_ERROR_ARCHIVE_ALREADY_MOUNTED, e.getCode());
      throw e;
    }
  }

  /**
   * Trying to mount different archives at the same location succeeds.
   */

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

  /**
   * Trying to any archive if a directory has not been specified fails.
   */

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
          Assert.assertEquals(Code.FS_ERROR_ARCHIVE_NO_DIRECTORY, e.getCode());
        }
      }
    });
  }

  /**
   * Mounting an item on the classpath works.
   */

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

  /**
   * Passing <code>null</code> to
   * {@link Filesystem#mountClasspathArchive(Class, PathVirtual)} fails.
   */

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

  /**
   * Passing <code>null</code> to
   * {@link Filesystem#mountClasspathArchive(Class, PathVirtual)} fails.
   */

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

  /**
   * Complicated shadowing works.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testMountShadowComplex0()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    /**
     * <ol>
     * <li>Create /subdir</li>
     * <li>Mount an archive into /subdir</li>
     * <li>Shadow /subdir with a file</li>
     * <li>Shadow the /subdir file with a directory</li>
     * <li>Check that /subdir is now empty</li>
     * <li>Mount an archive into /subdir</li>
     * <li>Check that /subdir contains only the new files</li>
     * </ol>
     */

    fs.createDirectory(PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));

    fs.mountArchive("files1-3.zip", PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file3.txt")));

    fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir")));

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file3.txt")));

    fs.mountArchive("files4-6.zip", PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file3.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file4.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file5.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file6.txt")));
  }

  /**
   * Asking for a snapshot of the mounts of an empty filesystem results in an
   * empty list.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testMountSnapshotEmpty()
      throws IOException,
        ConstraintError
  {
    final Filesystem fs = FilesystemTest.makeFS();
    Assert.assertTrue(fs.getMountedArchives().isEmpty());
  }

  /**
   * Asking for a snapshot of the mounts of filesystem works.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testMountSnapshotMounts()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.createDirectory(PathVirtual.ofString("/a"));
    fs.createDirectory(PathVirtual.ofString("/b"));
    fs.createDirectory(PathVirtual.ofString("/c"));

    fs.mountArchive("single-file.zip", PathVirtual.ofString("/a"));
    fs.mountArchive("complex.zip", PathVirtual.ofString("/a"));
    fs.mountArchive("single-file.zip", PathVirtual.ofString("/b"));
    fs.mountArchive("single-file.zip", PathVirtual.ofString("/c"));
    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ofString("/c"));

    final Deque<Pair<PathReal, PathVirtual>> mounts = fs.getMountedArchives();
    Assert.assertEquals(5, mounts.size());

    {
      final Pair<PathReal, PathVirtual> p = mounts.pop();
      Assert.assertEquals("single-file-and-subdir.zip", p.first
        .toFile()
        .getName());
      Assert.assertEquals(PathVirtual.ofString("/c"), p.second);
    }

    {
      final Pair<PathReal, PathVirtual> p = mounts.pop();
      Assert.assertEquals("single-file.zip", p.first.toFile().getName());
      Assert.assertEquals(PathVirtual.ofString("/c"), p.second);
    }

    {
      final Pair<PathReal, PathVirtual> p = mounts.pop();
      Assert.assertEquals("single-file.zip", p.first.toFile().getName());
      Assert.assertEquals(PathVirtual.ofString("/b"), p.second);
    }

    {
      final Pair<PathReal, PathVirtual> p = mounts.pop();
      Assert.assertEquals("complex.zip", p.first.toFile().getName());
      Assert.assertEquals(PathVirtual.ofString("/a"), p.second);
    }

    {
      final Pair<PathReal, PathVirtual> p = mounts.pop();
      Assert.assertEquals("single-file.zip", p.first.toFile().getName());
      Assert.assertEquals(PathVirtual.ofString("/a"), p.second);
    }
  }

  /**
   * The root directory always exists and is a directory.
   */

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

  /**
   * @see #testShadowEdgeCaseShadowSameMount()
   */

  @SuppressWarnings("static-method") @Test public
    void
    testShadowEdgeCaseReverseShadowSameMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir")));
  }

  /**
   * If a directory in an archive shadows a file, then searching for any
   * object in that archive does not cause an error to be raised due to the
   * original file appearing to be an ancestor.
   * 
   * This tests checks the case where the shadowing file is in an archive
   * mounted at an ancestor of the shadowed directory.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testShadowEdgeCaseShadowIsAncestor()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.createDirectory(PathVirtual.ofString("/subdir"));
    fs.mountArchive(
      "single-file-and-subdir.zip",
      PathVirtual.ofString("/subdir"));

    fs.mountArchive("subdir-subdir-shadow.zip", PathVirtual.ROOT);
    Assert
      .assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir/subdir")));
  }

  /**
   * If a directory in an archive shadows a file, then searching for any
   * object in that archive does not cause an error to be raised due to the
   * original file appearing to be an ancestor.
   * 
   * This tests checks the case where the shadowed directory in archive
   * <code>A</code> mounted at <code>MA</code> is shadowed by a file in an
   * archive mounted at a child of <code>MA</code>.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testShadowEdgeCaseShadowIsChild()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("single-file-in-subdir-subdir.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual
      .ofString("/subdir/subdir/file.txt")));

    fs.mountArchive("subdir-shadow.zip", PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert
      .assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir/subdir")));
  }

  /**
   * If a directory in an archive shadows a file, then searching for any
   * object in that archive does not cause an error to be raised due to the
   * original file appearing to be an ancestor.
   * 
   * This tests checks the case where the shadowing file is in an archive
   * mounted at the same mount point as the archive containing the shadowed
   * directory.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testShadowEdgeCaseShadowSameMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir")));
    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert
      .assertFalse(fs.exists(PathVirtual.ofString("/subdir/nonexistent")));
  }

  @SuppressWarnings("static-method") @Test public void testShadowExample()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.createDirectory(PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));

    fs.mountArchive("files1-3.zip", PathVirtual.ofString("/subdir"));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file3.txt")));

    fs.mountArchive("subdir-shadow.zip", PathVirtual.ROOT);
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/subdir")));

    fs.mountArchive("single-file-and-subdir.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/subdir")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file.txt")));

    fs.mountArchive("files4-6.zip", PathVirtual.ofString("/subdir"));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file1.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file2.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/subdir/file3.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file4.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file5.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/subdir/file6.txt")));
  }

  /**
   * Mounting an archive B at a directory provided by another archive A, and
   * then unmounting A, means B is still accessible.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testUnmountMountArchiveDirectoryAccessible()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.mountArchive("complex.zip", PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/b")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a/c")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/ac1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/ac2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/ac3.txt")));

    fs.mountArchive("complex.zip", PathVirtual.ofString("/a/c"));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/b")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a/c")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/ac1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/ac2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/ac3.txt")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a/c/a")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a/c/a/c")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/a/c/ac1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/a/c/ac2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/a/c/ac3.txt")));

    fs.unmount(PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a")));
    Assert.assertFalse(fs.isDirectory(PathVirtual.ofString("/b")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a/c")));
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/a/c/ac1.txt")));
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/a/c/ac2.txt")));
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/a/c/ac3.txt")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a/c/a")));
    Assert.assertTrue(fs.isDirectory(PathVirtual.ofString("/a/c/a/c")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/a/c/ac1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/a/c/ac2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/a/c/a/c/ac3.txt")));
  }

  /**
   * Mounting reveals filesystem objects, unmounting hides them again, and has
   * stacking semantics.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testUnmountMountMultiple()
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
    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file3.txt")));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/file4.txt")));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/file5.txt")));
    Assert.assertTrue(fs.exists(PathVirtual.ofString("/file6.txt")));

    fs.unmount(PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file1.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file2.txt")));
    Assert.assertTrue(fs.isFile(PathVirtual.ofString("/file3.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file4.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file5.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file6.txt")));

    fs.unmount(PathVirtual.ROOT);
    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file1.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file2.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file3.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file4.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file5.txt")));
    Assert.assertFalse(fs.exists(PathVirtual.ofString("/file6.txt")));
  }

  /**
   * Mounting reveals filesystem objects, unmounting hides them again.
   */

  @SuppressWarnings("static-method") @Test public void testUnmountMountOne()
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

    fs.unmount(PathVirtual.ROOT);

    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/file1.txt")));
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/file2.txt")));
    Assert.assertFalse(fs.isFile(PathVirtual.ofString("/file3.txt")));
  }

  /**
   * Unmounting the root directory does nothing.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testUnmountNotMounted()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.unmount(PathVirtual.ROOT);
    fs.unmount(PathVirtual.ROOT);
    fs.unmount(PathVirtual.ROOT);

    Assert.assertTrue(fs.isDirectory(PathVirtual.ROOT));
  }

}
