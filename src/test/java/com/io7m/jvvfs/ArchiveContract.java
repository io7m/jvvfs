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

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
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
   */

  abstract @Nonnull Archive<T> getArchive(
    final @Nonnull String basename,
    final @Nonnull PathVirtual mount)
    throws FileNotFoundException,
      IOException,
      ConstraintError;

  @Test public void testFileSizeFile()
    throws FileNotFoundException,
      IOException,
      ConstraintError,
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
        ConstraintError,
        FilesystemError
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      a.getFileSize(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
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
        ConstraintError,
        FilesystemError
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent/file.txt");
      a.getFileSize(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public void testFileSizeNotFile()
    throws FileNotFoundException,
      IOException,
      ConstraintError,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      a.getFileSize(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(FilesystemError.Code.FS_ERROR_NOT_A_FILE, e.code);
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
        ConstraintError,
        FilesystemError
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt/file.txt");
      a.getFileSize(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(
        FilesystemError.Code.FS_ERROR_NOT_A_DIRECTORY,
        e.code);
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testFileTimeDirectory()
    throws FileNotFoundException,
      IOException,
      ConstraintError,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      final Calendar c = a.getModificationTime(p);

      Assert.assertEquals(2012, c.get(Calendar.YEAR));
      Assert.assertEquals(0, c.get(Calendar.MONTH));
      Assert.assertEquals(23, c.get(Calendar.DAY_OF_MONTH));
      Assert.assertEquals(21, c.get(Calendar.HOUR_OF_DAY));
      Assert.assertEquals(50, c.get(Calendar.MINUTE));
      Assert.assertEquals(42, c.get(Calendar.SECOND));
    } finally {
      a.close();
    }
  }

  @Test public void testFileTimeFile()
    throws FileNotFoundException,
      IOException,
      ConstraintError,
      FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");
      final Calendar c = a.getModificationTime(p);

      Assert.assertEquals(2012, c.get(Calendar.YEAR));
      Assert.assertEquals(0, c.get(Calendar.MONTH));
      Assert.assertEquals(23, c.get(Calendar.DAY_OF_MONTH));
      Assert.assertEquals(21, c.get(Calendar.HOUR_OF_DAY));
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
        ConstraintError,
        FilesystemError
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      a.getModificationTime(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleDirectory()
    throws FilesystemError,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      final Option<FileReference<T>> r = a.lookup(p);

      Assert.assertTrue(r.isSome());
      final Some<FileReference<T>> s = (Option.Some<FileReference<T>>) r;
      Assert.assertTrue(s.value.type == Type.TYPE_DIRECTORY);
      Assert.assertTrue(s.value.path.equals(p));
      Assert.assertTrue(s.value.archive == a);
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleFile()
    throws FilesystemError,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");
      final Option<FileReference<T>> r = a.lookup(p);

      Assert.assertTrue(r.isSome());
      final Some<FileReference<T>> s = (Option.Some<FileReference<T>>) r;
      Assert.assertTrue(s.value.type == Type.TYPE_FILE);
      Assert.assertTrue(s.value.path.equals(p));
      Assert.assertTrue(s.value.archive == a);
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleFileNonexistent()
    throws FilesystemError,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      final Option<FileReference<T>> r = a.lookup(p);

      Assert.assertTrue(r.isNone());
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleFileNonexistentAncestor()
    throws FilesystemError,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent/file.txt");
      final Option<FileReference<T>> r = a.lookup(p);
      Assert.assertTrue(r.isNone());
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testLookupSingleFileNotDirectory()
      throws FilesystemError,
        ConstraintError,
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
        e.code);
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testMountPath()
    throws ConstraintError,
      FileNotFoundException,
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
      ConstraintError,
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
        ConstraintError,
        FileNotFoundException,
        IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      a.openFile(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public void testOpenFileNotAFile()
    throws FilesystemError,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      a.openFile(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_FILE, e.code);
      throw e;
    } finally {
      a.close();
    }
  }

  @Test(expected = FilesystemError.class) public
    void
    testOpenFileParentNotDirectory()
      throws FilesystemError,
        ConstraintError,
        FileNotFoundException,
        IOException
  {
    final Archive<T> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt/file.txt");
      a.openFile(p);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NOT_A_DIRECTORY, e.code);
      throw e;
    } finally {
      a.close();
    }
  }

  @Test public void testOpenFileSubdirectory()
    throws FilesystemError,
      ConstraintError,
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
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    final Archive<T> a = this.getArchive("single-file", PathVirtual.ROOT);
    final Option<FileReference<T>> r = a.lookup(PathVirtual.ROOT);

    Assert.assertTrue(r.isSome());
    final Some<FileReference<T>> s = (Option.Some<FileReference<T>>) r;
    Assert.assertTrue(s.value.type == Type.TYPE_DIRECTORY);
    Assert.assertTrue(s.value.path.equals(PathVirtual.ROOT));
    Assert.assertTrue(s.value.archive == a);
  }
}
