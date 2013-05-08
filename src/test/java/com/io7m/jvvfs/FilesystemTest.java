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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.FilesystemError.Code;

public class FilesystemTest
{
  private static final String archive_dir = "test-archives";

  private static Filesystem makeFS()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    return new Filesystem(TestData.getLog(), new PathReal(
      TestData.getTestDataDirectory()));
  }

  @SuppressWarnings("static-method") @Before public void setUp()
    throws Exception
  {
    {
      final File f =
        new File(FilesystemTest.archive_dir + "/archive1/subdir");
      if (f.exists() == false) {
        f.mkdirs();
      }
    }

    {
      final File f =
        new File(FilesystemTest.archive_dir + "/archive-mtime/subdir");
      if (f.exists() == false) {
        f.mkdirs();
      }
      f.setLastModified(0);
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListCorrectUnion()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("archive2", new PathVirtual("/"));
      f.mount("archive3", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    f.listDirectory(new PathVirtual("/"), items);
    Assert.assertTrue(items.contains("file21.txt"));
    Assert.assertTrue(items.contains("file22.txt"));
    Assert.assertTrue(items.contains("file23.txt"));
    Assert.assertTrue(items.contains("file31.txt"));
    Assert.assertTrue(items.contains("file32.txt"));
    Assert.assertTrue(items.contains("file33.txt"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListCorrectUnionNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("archive2", new PathVirtual("/xyz"));
      f.mount("archive3", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    f.listDirectory(new PathVirtual("/xyz"), items);
    Assert.assertTrue(items.contains("file21.txt"));
    Assert.assertTrue(items.contains("file22.txt"));
    Assert.assertTrue(items.contains("file23.txt"));
    Assert.assertTrue(items.contains("file31.txt"));
    Assert.assertTrue(items.contains("file32.txt"));
    Assert.assertTrue(items.contains("file33.txt"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryModificationTimeCorrect()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-mtime", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    final File file =
      new File(FilesystemTest.archive_dir + "/archive-mtime/file.txt");
    file.setLastModified(0);

    final long t = f.modificationTime(new PathVirtual("/file.txt"));
    Assert.assertEquals(0, t);
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryModificationTimeCorrectNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-mtime", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    final File file =
      new File(FilesystemTest.archive_dir + "/archive-mtime/file.txt");
    file.setLastModified(0);

    final long t = f.modificationTime(new PathVirtual("/xyz/file.txt"));
    Assert.assertEquals(0, t);
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryModificationTimeDirectory()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-mtime", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      f.modificationTime(new PathVirtual("/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryModificationTimeDirectoryNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-mtime", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      f.modificationTime(new PathVirtual("/xyz/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryModificationTimeNonexistent()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-mtime", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      f.modificationTime(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryModificationTimeNonexistentNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-mtime", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      f.modificationTime(new PathVirtual("/xyz/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings({ "static-method", "resource" }) @Test public
    void
    testFilesystemMountUnsafeClasspathFile()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Filesystem fs = FilesystemTest.makeFS();

    fs.createDirectory("/xyz");
    fs
      .mountUnsafeClasspathItem(FilesystemTest.class, new PathVirtual("/xyz"));

    final InputStream is = fs.openFile("/xyz/com/io7m/jvvfs/example.txt");
    final BufferedReader br = new BufferedReader(new InputStreamReader(is));
    final String text = br.readLine();
    Assert.assertEquals("Hello.", text);
    is.close();
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipIsDirectoryCorrect()
      throws ConstraintError,
        IOException,
        FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    fs.createDirectory(new PathVirtual("/usr/local/lib"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));

    fs.mount("local.zip", new PathVirtual("/usr"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListCorrectOneNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("three.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    f.listDirectory(new PathVirtual("/xyz"), items);
    Assert.assertTrue(items.contains("subdir"));
    Assert.assertTrue(items.contains("file.txt"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListCorrectUnion()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("zip2.zip", new PathVirtual("/"));
      f.mount("zip3.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    f.listDirectory(new PathVirtual("/"), items);
    Assert.assertTrue(items.contains("file21.txt"));
    Assert.assertTrue(items.contains("file22.txt"));
    Assert.assertTrue(items.contains("file23.txt"));
    Assert.assertTrue(items.contains("file31.txt"));
    Assert.assertTrue(items.contains("file32.txt"));
    Assert.assertTrue(items.contains("file33.txt"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListCorrectUnionNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("zip2.zip", new PathVirtual("/xyz"));
      f.mount("zip3.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    f.listDirectory(new PathVirtual("/xyz"), items);
    Assert.assertTrue(items.contains("file21.txt"));
    Assert.assertTrue(items.contains("file22.txt"));
    Assert.assertTrue(items.contains("file23.txt"));
    Assert.assertTrue(items.contains("file31.txt"));
    Assert.assertTrue(items.contains("file32.txt"));
    Assert.assertTrue(items.contains("file33.txt"));
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipListNonexistent()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("three.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.listDirectory(new PathVirtual("/nonexistent"), items);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipListNonexistentNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("three.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.listDirectory(new PathVirtual("/xyz/nonexistent"), items);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipListNotDirectory()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("one.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.listDirectory(new PathVirtual("/file.txt/"), items);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipListNotDirectoryNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("one.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.listDirectory(new PathVirtual("/xyz/file.txt/"), items);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipListShadowed()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("archive1", new PathVirtual("/"));
      f.mount("shadow.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.listDirectory(new PathVirtual("/subdir"), items);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipListShadowedNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("archive1", new PathVirtual("/xyz"));
      f.mount("shadow.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      f.listDirectory(new PathVirtual("/xyz/subdir"), items);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipModificationTimeCorrect()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("mtime.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    /**
     * Unfortunately, it seems there's very little that can be asserted about
     * the modification time portably, so this test solely exists for code
     * coverage.
     */

    f.modificationTime(new PathVirtual("/file.txt"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipModificationTimeCorrectNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("mtime.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    /**
     * Unfortunately, it seems there's very little that can be asserted about
     * the modification time portably, so this test solely exists for code
     * coverage.
     */

    f.modificationTime(new PathVirtual("/xyz/file.txt"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipModificationTimeCorrectString()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("mtime.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    /**
     * Unfortunately, it seems there's very little that can be asserted about
     * the modification time portably, so this test solely exists for code
     * coverage.
     */

    f.modificationTime("/file.txt");
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipModificationTimeDirectory()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("mtime.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      f.modificationTime(new PathVirtual("/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipModificationTimeDirectoryNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("mtime.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      f.modificationTime(new PathVirtual("/xyz/subdir"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_IS_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipModificationTimeNonexistent()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("mtime.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      f.modificationTime(new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipModificationTimeNonexistentNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("mtime.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail();
    }

    try {
      f.modificationTime(new PathVirtual("/xyz/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

}
