package com.io7m.jvvfs.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemAPI;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemError.Code;
import com.io7m.jvvfs.PathReal;
import com.io7m.jvvfs.PathVirtual;

public class FilesystemTest
{
  private static final String archive_dir = "test-archives";

  @Before public void setUp()
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

  private static FilesystemAPI makeFS()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Log log =
      new Log(
        PropertyUtils.loadFromFile("io7m-jvvfs.properties"),
        "com.io7m.jvvfs",
        "main");
    return new Filesystem(log, new PathReal(FilesystemTest.archive_dir));
  }

  @SuppressWarnings("unused") @Test(expected = FilesystemError.class) public
    void
    testFilesystemBadArchiveDir()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Log log =
      new Log(
        PropertyUtils.loadFromFile("io7m-jvvfs.properties"),
        "com.io7m",
        "main");

    try {
      new Filesystem(log, new PathReal("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @Test public void testFilesystemCreateDirectory()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();
    fs.createDirectory(new PathVirtual("/dev"));
    fs.createDirectory(new PathVirtual("/usr/local/lib"));
  }

  @Test public void testFilesystemCreatedIsDirectory()
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
  }

  @Test public void testFilesystemDirectoryIsDirectoryCorrect()
    throws ConstraintError,
      IOException,
      FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    fs.createDirectory(new PathVirtual("/usr/local/lib"));
    fs.mount("archive0", new PathVirtual("/usr"));

    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));
  }

  @Test public void testFilesystemDirectoryIsFileCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    try {
      fs.mount("archive0", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertFalse(fs.isFile(new PathVirtual("/")));
    Assert.assertTrue(fs.isFile(new PathVirtual("/file.txt")));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/file.txt")));
  }

  @Test public void testFilesystemDirectoryListComplex0()
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

  @Test public void testFilesystemDirectoryListComplex1()
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

  @Test public void testFilesystemDirectoryListComplex2()
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

  @Test public void testFilesystemDirectoryListComplex3()
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

  /*
   * Attempting to look up a path /a/b where 'a' is not a directory raises
   * 'FS_ERROR_NOT_A_DIRECTORY'.
   */

  @Test public void testFilesystemDirectoryListComplex4()
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

  @Test public void testFilesystemDirectoryListComplex5()
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

  /*
   * If a file appears in a mounted archive that shadows a previously visible
   * directory, the directory is invisible.
   */

  @Test public void testFilesystemDirectoryListComplex6()
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

  @Test public void testFilesystemDirectoryListComplex7()
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

  @Test public void testFilesystemDirectoryListComplex8()
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

  @Test public void testFilesystemDirectoryListCorrectOne()
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

  @Test public void testFilesystemDirectoryListCorrectUnion()
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test public void testFilesystemDirectoryModificationTimeCorrect()
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test public void testFilesystemDirectoryOpenFileCorrect()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("archive0", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello archive0.", line);
  }

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryOpenFileDirectory()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("archive1", new PathVirtual("/"));
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

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryOpenFileNonexistent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("archive1", new PathVirtual("/"));
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

  @Test public void testFilesystemDirectoryOpenFileOverlayCorrect()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    FilesystemAPI fs = null;

    try {
      fs = FilesystemTest.makeFS();
      fs.mount("archive0", new PathVirtual("/"));
      fs.mount("archive1", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    assert fs != null;
    final InputStream i = fs.openFile(new PathVirtual("/file.txt"));
    final BufferedReader r = new BufferedReader(new InputStreamReader(i));
    final String line = r.readLine();
    Assert.assertEquals("Hello archive1.", line);
  }

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectoryOpenFileRoot()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive0", new PathVirtual("/"));
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

  @Test public void testFilesystemDirectorySizeCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive1", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    final long size = f.fileSize(new PathVirtual("/file.txt"));
    Assert.assertEquals(16, size);
  }

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectorySizeDirectory()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive1", new PathVirtual("/"));
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

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemDirectorySizeNonexistent()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.mount("archive1", new PathVirtual("/"));
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

  @Test public void testFilesystemDirectoryUnmountCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    f.mount("archive0", new PathVirtual("/"));
    f.unmount(new PathVirtual("/"));
  }

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountBadArchive()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    try {
      fs.mount("/nonexistent", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountBusy()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    try {
      f.createDirectory(new PathVirtual("/usr"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertFalse(f.isDirectory(new PathVirtual("/usr/subdir")));
      f.mount("archive1", new PathVirtual("/usr"));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr")));
      Assert.assertTrue(f.isDirectory(new PathVirtual("/usr/subdir")));
      f.mount("archive1", new PathVirtual("/usr/subdir"));
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

  @Test public void testFilesystemMountCollision()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    fs.createDirectory(new PathVirtual("/file.txt/lib"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/file.txt")));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/file.txt/lib")));

    fs.mount("archive0", new PathVirtual("/"));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/file.txt")));

    try {
      fs.isDirectory(new PathVirtual("/file.txt/lib"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
    }

    try {
      fs.mount("archive0", new PathVirtual("/file.txt"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountDuplicate()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    try {
      fs.mount("archive0", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.fail(e.getMessage());
    }

    try {
      fs.mount("archive0", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_DUPLICATE_MOUNT, e.code);
      throw e;
    }
  }

  @Test public void testFilesystemMountedIsDirectoryExternalMakeDir()
    throws ConstraintError,
      IOException,
      FilesystemError
  {
    final File dir = new File(FilesystemTest.archive_dir + "/local");
    dir.delete();

    try {
      final FilesystemAPI fs = FilesystemTest.makeFS();

      fs.createDirectory(new PathVirtual("/usr/local/lib"));
      fs.mount("archive0", new PathVirtual("/usr"));

      Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));

      final boolean ok = dir.mkdir();
      Assert.assertTrue(ok);
      Assert.assertTrue(dir.isDirectory());

      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local")));
      Assert.assertTrue(fs.isDirectory(new PathVirtual("/usr/local/lib")));
    } finally {
      dir.delete();
    }
  }

  @Test public void testFilesystemMountedIsDirectoryOverriddenFile()
    throws ConstraintError,
      IOException,
      FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    fs.createDirectory(new PathVirtual("/local/lib"));
    fs.mount("archive-with-local", new PathVirtual("/"));

    Assert.assertTrue(fs.isDirectory(new PathVirtual("/")));
    Assert.assertFalse(fs.isDirectory(new PathVirtual("/local")));

    try {
      fs.isDirectory(new PathVirtual("/local/lib"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
    }
  }

  @Test public void testFilesystemMountInMount()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    fs.mount("archive1", new PathVirtual("/"));
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir")));
    fs.mount("archive1", new PathVirtual("/subdir"));

    System.out.println(fs);
    Assert.assertTrue(fs.isDirectory(new PathVirtual("/subdir/subdir")));
  }

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountNoArchive()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    try {
      fs.mount("nonexistent", new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemMountNoMountpoint()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    try {
      fs.mount("empty.zip", new PathVirtual("/nonexistent"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    }
  }

  @Test public void testFilesystemMountOK()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    fs.createDirectory(new PathVirtual("/empty"));
    fs.mount("archive0", new PathVirtual("/empty"));
    fs.mount("one.zip", new PathVirtual("/empty"));
  }

  @Test public void testFilesystemUnmountCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();

    try {
      fs.mount("archive1", new PathVirtual("/"));
      fs.mount("archive0", new PathVirtual("/"));
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

  @Test(expected = FilesystemError.class) public
    void
    testFilesystemUnmountNotMountedRoot()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final FilesystemAPI fs = FilesystemTest.makeFS();
    try {
      fs.unmount(new PathVirtual("/"));
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_MOUNTED, e.code);
      throw e;
    }
  }

  @Test public void testFilesystemZipIsDirectoryCorrect()
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

  @Test public void testFilesystemZipIsFileCorrect()
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

  @Test public void testFilesystemZipListComplex0()
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

  @Test public void testFilesystemZipListComplex1()
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

  @Test public void testFilesystemZipListComplex2()
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

  @Test public void testFilesystemZipListComplex3()
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

  @Test public void testFilesystemZipListComplex4()
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

  @Test public void testFilesystemZipListComplex5()
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

  @Test public void testFilesystemZipListComplex6()
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

  @Test public void testFilesystemZipListComplex7()
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

  @Test public void testFilesystemZipListComplex8()
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

  @Test public void testFilesystemZipListCorrectOne()
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

  @Test public void testFilesystemZipListCorrectUnion()
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test public void testFilesystemZipModificationTimeCorrect()
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

    final long t = f.modificationTime(new PathVirtual("/file.txt"));
    Assert.assertEquals(0, t);
  }

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test public void testFilesystemZipNoExplicitSubdirCorrect()
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

  @Test public void testFilesystemZipOpenFileCorrect()
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test public void testFilesystemZipOpenFileOverlayCorrect()
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

  @Test(expected = FilesystemError.class) public
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

  @Test public void testFilesystemZipSizeCorrect()
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

  @Test(expected = FilesystemError.class) public
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

  @Test(expected = FilesystemError.class) public
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

  @Test public void testFilesystemZipUnmountCorrect()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    final FilesystemAPI f = FilesystemTest.makeFS();

    f.mount("one.zip", new PathVirtual("/"));
    f.unmount(new PathVirtual("/"));
  }
}
