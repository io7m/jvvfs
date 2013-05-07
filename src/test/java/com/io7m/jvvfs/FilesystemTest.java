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
    testFilesystemDirectoryListComplex0()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/"), items);
      Assert.assertEquals(2, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("b"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex0NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz"), items);
      Assert.assertEquals(2, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("b"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex1()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/a"), items);
      Assert.assertEquals(6, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("a1.txt"));
      Assert.assertTrue(items.contains("a2.txt"));
      Assert.assertTrue(items.contains("a3.txt"));
      Assert.assertTrue(items.contains("b"));
      Assert.assertTrue(items.contains("c"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex1NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/a"), items);
      Assert.assertEquals(6, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("a1.txt"));
      Assert.assertTrue(items.contains("a2.txt"));
      Assert.assertTrue(items.contains("a3.txt"));
      Assert.assertTrue(items.contains("b"));
      Assert.assertTrue(items.contains("c"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex2()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/b"), items);
      Assert.assertEquals(6, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("b1.txt"));
      Assert.assertTrue(items.contains("b2.txt"));
      Assert.assertTrue(items.contains("b3.txt"));
      Assert.assertTrue(items.contains("b"));
      Assert.assertTrue(items.contains("c"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex2NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/b"), items);
      Assert.assertEquals(6, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("b1.txt"));
      Assert.assertTrue(items.contains("b2.txt"));
      Assert.assertTrue(items.contains("b3.txt"));
      Assert.assertTrue(items.contains("b"));
      Assert.assertTrue(items.contains("c"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex3()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/a/a"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("aa1.txt"));
      Assert.assertTrue(items.contains("aa2.txt"));
      Assert.assertTrue(items.contains("aa3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex3NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/a/a"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("aa1.txt"));
      Assert.assertTrue(items.contains("aa2.txt"));
      Assert.assertTrue(items.contains("aa3.txt"));
    }
  }

  /*
   * Attempting to look up a path /a/b where 'a' is not a directory raises
   * 'FS_ERROR_NOT_A_DIRECTORY'.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex4()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/a/b"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ab1.txt"));
      Assert.assertTrue(items.contains("ab2.txt"));
      Assert.assertTrue(items.contains("ab3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex4NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/a/b"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ab1.txt"));
      Assert.assertTrue(items.contains("ab2.txt"));
      Assert.assertTrue(items.contains("ab3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex5()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/a/c"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ac1.txt"));
      Assert.assertTrue(items.contains("ac2.txt"));
      Assert.assertTrue(items.contains("ac3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex5NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/a/c"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ac1.txt"));
      Assert.assertTrue(items.contains("ac2.txt"));
      Assert.assertTrue(items.contains("ac3.txt"));
    }
  }

  /*
   * If a file appears in a mounted archive that shadows a previously visible
   * directory, the directory is invisible.
   */

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex6()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/b/a"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ba1.txt"));
      Assert.assertTrue(items.contains("ba2.txt"));
      Assert.assertTrue(items.contains("ba3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex6NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/b/a"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ba1.txt"));
      Assert.assertTrue(items.contains("ba2.txt"));
      Assert.assertTrue(items.contains("ba3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex7()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/b/b"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("bb1.txt"));
      Assert.assertTrue(items.contains("bb2.txt"));
      Assert.assertTrue(items.contains("bb3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex7NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/b/b"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("bb1.txt"));
      Assert.assertTrue(items.contains("bb2.txt"));
      Assert.assertTrue(items.contains("bb3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex8()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive-nest", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/b/c"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("bc1.txt"));
      Assert.assertTrue(items.contains("bc2.txt"));
      Assert.assertTrue(items.contains("bc3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListComplex8NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("archive-nest", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/b/c"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("bc1.txt"));
      Assert.assertTrue(items.contains("bc2.txt"));
      Assert.assertTrue(items.contains("bc3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListCorrectOne()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("archive1", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    f.listDirectory(new PathVirtual("/"), items);
    Assert.assertTrue(items.contains("subdir"));
    Assert.assertTrue(items.contains("file.txt"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListCorrectOneNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("archive1", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    f.listDirectory(new PathVirtual("/xyz"), items);
    Assert.assertTrue(items.contains("subdir"));
    Assert.assertTrue(items.contains("file.txt"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListCorrectOneString()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("archive1", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    f.listDirectory("/", items);
    Assert.assertTrue(items.contains("subdir"));
    Assert.assertTrue(items.contains("file.txt"));
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
    testFilesystemDirectoryListMountDirect()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/");
      f.mount("archive1", new PathVirtual("/"));
      f.listDirectory(new PathVirtual("/"), items);
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(2, items.size());
    Assert.assertTrue(items.contains("file.txt"));
    Assert.assertTrue(items.contains("subdir"));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemDirectoryListMountDirectNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("archive1", new PathVirtual("/xyz"));
      f.listDirectory(new PathVirtual("/xyz"), items);
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(2, items.size());
    Assert.assertTrue(items.contains("file.txt"));
    Assert.assertTrue(items.contains("subdir"));
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryListNonexistent()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("archive1", new PathVirtual("/"));
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
    testFilesystemDirectoryListNonexistentNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("archive1", new PathVirtual("/xyz"));
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
    testFilesystemDirectoryListNotDirectory()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("archive1", new PathVirtual("/"));
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
    testFilesystemDirectoryListNotDirectoryNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("archive1", new PathVirtual("/xyz"));
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
    testFilesystemDirectoryListShadowed()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.mount("archive1", new PathVirtual("/"));
      f.mount("archive-shadow-file", new PathVirtual("/"));
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
    testFilesystemDirectoryListShadowedNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();
    final TreeSet<String> items = new TreeSet<String>();

    try {
      f.createDirectory("/xyz");
      f.mount("archive1", new PathVirtual("/xyz"));
      f.mount("archive-shadow-file", new PathVirtual("/xyz"));
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
    testFilesystemZipIsFileCorrect()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    try {
      fs.mount("one.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertFalse(fs.isFile(new PathVirtual("/")));
    Assert.assertTrue(fs.isFile(new PathVirtual("/file.txt")));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/file.txt")));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipIsFileCorrectNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    try {
      fs.createDirectory("/xyz");
      fs.mount("one.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertFalse(fs.isFile(new PathVirtual("/xyz")));
    Assert.assertTrue(fs.isFile(new PathVirtual("/xyz/file.txt")));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/xyz/file.txt")));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex0()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/"), items);
      Assert.assertEquals(2, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("b"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex0NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz"), items);
      Assert.assertEquals(2, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("b"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex1()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/a"), items);
      Assert.assertEquals(6, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("a1.txt"));
      Assert.assertTrue(items.contains("a2.txt"));
      Assert.assertTrue(items.contains("a3.txt"));
      Assert.assertTrue(items.contains("b"));
      Assert.assertTrue(items.contains("c"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex1NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/a"), items);
      Assert.assertEquals(6, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("a1.txt"));
      Assert.assertTrue(items.contains("a2.txt"));
      Assert.assertTrue(items.contains("a3.txt"));
      Assert.assertTrue(items.contains("b"));
      Assert.assertTrue(items.contains("c"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex2()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/b"), items);
      Assert.assertEquals(6, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("b1.txt"));
      Assert.assertTrue(items.contains("b2.txt"));
      Assert.assertTrue(items.contains("b3.txt"));
      Assert.assertTrue(items.contains("b"));
      Assert.assertTrue(items.contains("c"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex2NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/b"), items);
      Assert.assertEquals(6, items.size());
      Assert.assertTrue(items.contains("a"));
      Assert.assertTrue(items.contains("b1.txt"));
      Assert.assertTrue(items.contains("b2.txt"));
      Assert.assertTrue(items.contains("b3.txt"));
      Assert.assertTrue(items.contains("b"));
      Assert.assertTrue(items.contains("c"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex3()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/a/a"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("aa1.txt"));
      Assert.assertTrue(items.contains("aa2.txt"));
      Assert.assertTrue(items.contains("aa3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex3NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/a/a"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("aa1.txt"));
      Assert.assertTrue(items.contains("aa2.txt"));
      Assert.assertTrue(items.contains("aa3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex4()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/a/b"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ab1.txt"));
      Assert.assertTrue(items.contains("ab2.txt"));
      Assert.assertTrue(items.contains("ab3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex4NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/a/b"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ab1.txt"));
      Assert.assertTrue(items.contains("ab2.txt"));
      Assert.assertTrue(items.contains("ab3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex5()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/a/c"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ac1.txt"));
      Assert.assertTrue(items.contains("ac2.txt"));
      Assert.assertTrue(items.contains("ac3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex5NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/a/c"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ac1.txt"));
      Assert.assertTrue(items.contains("ac2.txt"));
      Assert.assertTrue(items.contains("ac3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex6()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/b/a"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ba1.txt"));
      Assert.assertTrue(items.contains("ba2.txt"));
      Assert.assertTrue(items.contains("ba3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex6NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/b/a"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("ba1.txt"));
      Assert.assertTrue(items.contains("ba2.txt"));
      Assert.assertTrue(items.contains("ba3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex7()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/b/b"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("bb1.txt"));
      Assert.assertTrue(items.contains("bb2.txt"));
      Assert.assertTrue(items.contains("bb3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex7NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/b/b"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("bb1.txt"));
      Assert.assertTrue(items.contains("bb2.txt"));
      Assert.assertTrue(items.contains("bb3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex8()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("zip-nest.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/b/c"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("bc1.txt"));
      Assert.assertTrue(items.contains("bc2.txt"));
      Assert.assertTrue(items.contains("bc3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListComplex8NonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("zip-nest.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    {
      final TreeSet<String> items = new TreeSet<String>();
      f.listDirectory(new PathVirtual("/xyz/b/c"), items);
      Assert.assertEquals(3, items.size());
      Assert.assertTrue(items.contains("bc1.txt"));
      Assert.assertTrue(items.contains("bc2.txt"));
      Assert.assertTrue(items.contains("bc3.txt"));
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipListCorrectOne()
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

    f.listDirectory(new PathVirtual("/"), items);
    Assert.assertTrue(items.contains("subdir"));
    Assert.assertTrue(items.contains("file.txt"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipMountBusy()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory(new PathVirtual("/usr"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertFalse(f.isDirectory(new PathVirtual("/usr/subdir")));
      f.mount("two.zip", new PathVirtual("/usr"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr/subdir")));
      f.mount("two.zip", new PathVirtual("/usr/subdir"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipMountBusyNoExplicitSubdir()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory(new PathVirtual("/usr"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertFalse(f.isDirectory(new PathVirtual("/usr/subdir")));
      f.mount("no-explicit-subdir.zip", new PathVirtual("/usr"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr/subdir")));
      f.mount("no-explicit-subdir.zip", new PathVirtual("/usr/subdir"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipMountEncrypted()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("encrypted.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_ARCHIVE_DAMAGED, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipNoExplicitSubdirCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("no-explicit-subdir.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir")));
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipNoExplicitSubdirCorrectNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.createDirectory("/xyz");
      fs.mount("no-explicit-subdir.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/xyz/subdir")));
  }

  @SuppressWarnings({ "static-method", "resource" }) @Test public
    void
    testFilesystemZipOpenFileCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("one.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello zip.", line);
  }

  @SuppressWarnings({ "static-method", "resource" }) @Test public
    void
    testFilesystemZipOpenFileCorrectNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.createDirectory("/xyz");
      fs.mount("one.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/xyz/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello zip.", line);
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipOpenFileDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("two.zip", new PathVirtual("/"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipOpenFileDirectoryNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.createDirectory("/xyz");
      fs.mount("two.zip", new PathVirtual("/xyz"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipOpenFileNoExplicitSubdirCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("no-explicit-subdir.zip", new PathVirtual("/"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipOpenFileNoExplicitSubdirCorrectNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.createDirectory("/xyz");
      fs.mount("no-explicit-subdir.zip", new PathVirtual("/xyz"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipOpenFileNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("two.zip", new PathVirtual("/"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipOpenFileNonexistentNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.createDirectory("/xyz");
      fs.mount("two.zip", new PathVirtual("/xyz"));
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

  @SuppressWarnings({ "static-method", "resource" }) @Test public
    void
    testFilesystemZipOpenFileOverlayCorrect()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("two.zip", new PathVirtual("/"));
      fs.mount("three.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello three.zip.", line);
  }

  @SuppressWarnings({ "static-method", "resource" }) @Test public
    void
    testFilesystemZipOpenFileOverlayCorrectNonRootMount()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.createDirectory("/xyz");
      fs.mount("two.zip", new PathVirtual("/xyz"));
      fs.mount("three.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/xyz/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello three.zip.", line);
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipOpenFileRoot()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("one.zip", new PathVirtual("/"));
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

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipSizeCorrect()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("three.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    final long size = f.fileSize(new PathVirtual("/file.txt"));
    Assert.assertEquals(17, size);
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipSizeCorrectNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("three.zip", new PathVirtual("/xyz"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    final long size = f.fileSize(new PathVirtual("/xyz/file.txt"));
    Assert.assertEquals(17, size);
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipSizeCorrectString()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("three.zip", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    final long size = f.fileSize("/file.txt");
    Assert.assertEquals(17, size);
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipSizeDirectory()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("three.zip", new PathVirtual("/"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipSizeDirectoryNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("three.zip", new PathVirtual("/xyz"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipSizeNonexistent()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("three.zip", new PathVirtual("/"));
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

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemZipSizeNonexistentNonRootMount()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory("/xyz");
      f.mount("three.zip", new PathVirtual("/xyz"));
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

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemZipUnmountCorrect()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    f.mount("one.zip", new PathVirtual("/"));
    f.unmount(new PathVirtual("/"));
  }
}
