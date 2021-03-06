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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jvvfs.FileReference.Type;
import com.io7m.jvvfs.FilesystemError.Code;

public abstract class ArchiveContract<T extends ArchiveKind>
{
  /**
   * Retrieve a reference to the test archive named <code>basename</code>.
   * 
   * This function is responsible for unpacking any resources needed to create
   * the archive.
   * 
   * @throws IOException
   * @throws FileNotFoundException
   * @throws ConstraintError
   * @throws FilesystemError
   */

  abstract Archive<T> getArchive(
    final String basename,
    final PathVirtual mount)
    throws FileNotFoundException,
      IOException,
      FilesystemError;

  @Test public void testFileSizeFile()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");
      final long s = a.getFileSize(p);
      Assert.assertEquals(15, s);
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testFileSizeNonexistent()
      throws FileNotFoundException,
        IOException,
        FilesystemError
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      a.getFileSize(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_NONEXISTENT,
        e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testFileSizeNonexistentParent()
      throws FileNotFoundException,
        IOException,
        FilesystemError
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent/file.txt");
      a.getFileSize(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_NONEXISTENT,
        e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public void testFileSizeNotFile()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      a.getFileSize(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_NOT_A_FILE,
        e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testFileSizeParentNotDirectory()
      throws FileNotFoundException,
        IOException,
        FilesystemError
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt/file.txt");
      a.getFileSize(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_NOT_A_DIRECTORY,
        e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testFileTimeDirectory()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      final Calendar c = a.getModificationTime(p);

      Assert.assertEquals(2012, c.get(Calendar.YEAR));
      Assert.assertEquals(0, c.get(Calendar.MONTH));
      // See ticket [bba03ad9e15]
      // Assert.assertEquals(23, c.get(Calendar.DAY_OF_MONTH));
      // Assert.assertEquals(21, c.get(Calendar.HOUR_OF_DAY));
      Assert.assertEquals(50, c.get(Calendar.MINUTE));
      Assert.assertEquals(42, c.get(Calendar.SECOND));
    } finally {
      a.close();
    }
  }

  @Test public void testFileTimeFile()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");
      final Calendar c = a.getModificationTime(p);

      Assert.assertEquals(2012, c.get(Calendar.YEAR));
      Assert.assertEquals(0, c.get(Calendar.MONTH));
      // See ticket [bba03ad9e15]
      // Assert.assertEquals(23, c.get(Calendar.DAY_OF_MONTH));
      // Assert.assertEquals(21, c.get(Calendar.HOUR_OF_DAY));
      Assert.assertEquals(50, c.get(Calendar.MINUTE));
      Assert.assertEquals(10, c.get(Calendar.SECOND));
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testFileTimeNonexistent()
      throws FileNotFoundException,
        IOException,
        FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      a.getModificationTime(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testGetRealPath()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    Assert.assertTrue(a
      .getRealPath()
      .toFile()
      .getName()
      .startsWith("single-file-and-subdir"));
  }

  @Test public void testListDirectory()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final Set<String> files = a.listDirectory(PathVirtual.ROOT);
      Assert.assertEquals(2, files.size());
      Assert.assertTrue(files.contains("file.txt"));
      Assert.assertTrue(files.contains("subdir"));
    } finally {
      a.close();
    }
  }

  @Test public void testListDirectoryComplex()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a = this.getArchive("complex", PathVirtual.ROOT);
    try {

      {
        final Set<String> files = a.listDirectory(PathVirtual.ROOT);
        Assert.assertEquals(2, files.size());
        Assert.assertTrue(files.contains("a"));
        Assert.assertTrue(files.contains("b"));
      }

      {
        final PathVirtual p = PathVirtual.ofString("/a");
        final Set<String> files = a.listDirectory(p);
        Assert.assertEquals(6, files.size());
        Assert.assertTrue(files.contains("a"));
        Assert.assertTrue(files.contains("b"));
        Assert.assertTrue(files.contains("c"));
        Assert.assertTrue(files.contains("a1.txt"));
        Assert.assertTrue(files.contains("a2.txt"));
        Assert.assertTrue(files.contains("a3.txt"));
      }

      {
        final PathVirtual p = PathVirtual.ofString("/a/a");
        final Set<String> files = a.listDirectory(p);
        Assert.assertEquals(3, files.size());
        Assert.assertTrue(files.contains("aa1.txt"));
        Assert.assertTrue(files.contains("aa2.txt"));
        Assert.assertTrue(files.contains("aa3.txt"));
      }

      {
        final PathVirtual p = PathVirtual.ofString("/a/b");
        final Set<String> files = a.listDirectory(p);
        Assert.assertEquals(3, files.size());
        Assert.assertTrue(files.contains("ab1.txt"));
        Assert.assertTrue(files.contains("ab2.txt"));
        Assert.assertTrue(files.contains("ab3.txt"));
      }

      {
        final PathVirtual p = PathVirtual.ofString("/a/c");
        final Set<String> files = a.listDirectory(p);
        Assert.assertEquals(3, files.size());
        Assert.assertTrue(files.contains("ac1.txt"));
        Assert.assertTrue(files.contains("ac2.txt"));
        Assert.assertTrue(files.contains("ac3.txt"));
      }

      {
        final PathVirtual p = PathVirtual.ofString("/b");
        final Set<String> files = a.listDirectory(p);
        Assert.assertEquals(6, files.size());
        Assert.assertTrue(files.contains("a"));
        Assert.assertTrue(files.contains("b"));
        Assert.assertTrue(files.contains("c"));
        Assert.assertTrue(files.contains("b1.txt"));
        Assert.assertTrue(files.contains("b2.txt"));
        Assert.assertTrue(files.contains("b3.txt"));
      }

      {
        final PathVirtual p = PathVirtual.ofString("/b/a");
        final Set<String> files = a.listDirectory(p);
        Assert.assertEquals(3, files.size());
        Assert.assertTrue(files.contains("ba1.txt"));
        Assert.assertTrue(files.contains("ba2.txt"));
        Assert.assertTrue(files.contains("ba3.txt"));
      }

      {
        final PathVirtual p = PathVirtual.ofString("/b/b");
        final Set<String> files = a.listDirectory(p);
        Assert.assertEquals(3, files.size());
        Assert.assertTrue(files.contains("bb1.txt"));
        Assert.assertTrue(files.contains("bb2.txt"));
        Assert.assertTrue(files.contains("bb3.txt"));
      }

      {
        final PathVirtual p = PathVirtual.ofString("/b/c");
        final Set<String> files = a.listDirectory(p);
        Assert.assertEquals(3, files.size());
        Assert.assertTrue(files.contains("bc1.txt"));
        Assert.assertTrue(files.contains("bc2.txt"));
        Assert.assertTrue(files.contains("bc3.txt"));
      }

    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public void testListDirectoryFile()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");
      a.listDirectory(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testListDirectoryNonexistent()
      throws FileNotFoundException,
        IOException,
        FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      a.listDirectory(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testListDirectoryParentFile()
      throws FileNotFoundException,
        IOException,
        FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt/file.txt");
      a.listDirectory(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleDirectory()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      final OptionType<FileReference<T>> r = a.lookup(p);

      Assert.assertTrue(r.isSome());
      final Some<FileReference<T>> s = (Some<FileReference<T>>) r;
      Assert.assertTrue(s.get().getType() == Type.TYPE_DIRECTORY);
      Assert.assertTrue(s.get().getPath().equals(p));
      Assert.assertTrue(s.get().getArchive() == a);
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleFile()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");
      final OptionType<FileReference<T>> r = a.lookup(p);

      Assert.assertTrue(r.isSome());
      final Some<FileReference<T>> s = (Some<FileReference<T>>) r;
      Assert.assertTrue(s.get().getType() == Type.TYPE_FILE);
      Assert.assertTrue(s.get().getPath().equals(p));
      Assert.assertTrue(s.get().getArchive() == a);
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleFileNonexistent()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      final OptionType<FileReference<T>> r = a.lookup(p);

      Assert.assertTrue(r.isNone());
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleFileNonexistentAncestor()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent/file.txt");
      final OptionType<FileReference<T>> r = a.lookup(p);
      Assert.assertTrue(r.isNone());
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testLookupSingleFileNotDirectory()
      throws FilesystemError,
        FileNotFoundException,
        IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt/xyz");
      a.lookup(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_NOT_A_DIRECTORY,
        e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleFileSubdirectory()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/subdir/file.txt");
      final OptionType<FileReference<T>> r = a.lookup(p);

      Assert.assertTrue(r.isSome());
      final Some<FileReference<T>> s = (Some<FileReference<T>>) r;
      Assert.assertTrue(s.get().getType() == Type.TYPE_FILE);
      Assert.assertTrue(s.get().getPath().equals(p));
      Assert.assertTrue(s.get().getArchive() == a);
    } finally {
      a.close();
    }
  }

  @Test public void testMountPath()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final PathVirtual p = PathVirtual.ofString("/x/y/z");
    final Archive<T> a = this.getArchive("single-file", p);
    try {
      Assert.assertEquals(p, a.getMountPath());
    } finally {
      a.close();
    }
  }

  @Test public void testOpenFile()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");
      final BufferedReader r =
        new BufferedReader(new InputStreamReader(a.openFile(p)));

      final String s = r.readLine();
      Assert.assertEquals(s, "Hello zip.");

      r.close();
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testOpenFileNonexistent()
      throws FilesystemError,
        FileNotFoundException,
        IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      a.openFile(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public void testOpenFileNotAFile()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      a.openFile(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_FILE, e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testOpenFileParentNotDirectory()
      throws FilesystemError,
        FileNotFoundException,
        IOException
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt/file.txt");
      a.openFile(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testOpenFileSubdirectory()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/subdir/file.txt");
      final BufferedReader r =
        new BufferedReader(new InputStreamReader(a.openFile(p)));

      final String s = r.readLine();
      Assert.assertEquals(s, "Hello two.zip subdir.");

      r.close();
    } finally {
      a.close();
    }
  }

  @Test public void testRoot()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    final OptionType<FileReference<T>> r = a.lookup(PathVirtual.ROOT);

    Assert.assertTrue(r.isSome());
    final Some<FileReference<T>> s = (Some<FileReference<T>>) r;
    Assert.assertTrue(s.get().getType() == Type.TYPE_DIRECTORY);
    Assert.assertTrue(s.get().getPath().equals(PathVirtual.ROOT));
    Assert.assertTrue(s.get().getArchive() == a);
  }

  @Test public void testString()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    final Archive<T> b = this.getArchive("single-file", PathVirtual.ROOT);

    Assert.assertTrue(a.toString().equals(a.toString()));
    Assert.assertFalse(a.toString().equals(b.toString()));
  }
}
