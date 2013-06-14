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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jvvfs.FileReference.Type;

public abstract class ArchiveContract
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

  abstract @Nonnull Archive getArchive(
    final @Nonnull String basename,
    final @Nonnull PathVirtual mount)
    throws FileNotFoundException,
      IOException,
      ConstraintError;

  @Test public void testLookupSingleFile()
    throws FilesystemError,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    final Archive a = this.getArchive("single-file", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");
      final Option<FileReference> r = a.lookup(p);

      Assert.assertTrue(r.isSome());
      final Some<FileReference> s = (Option.Some<FileReference>) r;
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
    final Archive a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent");
      final Option<FileReference> r = a.lookup(p);

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
    final Archive a = this.getArchive("single-file", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/nonexistent/file.txt");
      final Option<FileReference> r = a.lookup(p);
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
    final Archive a = this.getArchive("single-file", PathVirtual.ROOT);

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
    final Archive a = this.getArchive("single-file", p);
    try {
      Assert.assertEquals(p, a.getMountPath());
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
    final Archive a = this.getArchive("single-file", PathVirtual.ROOT);
    final Option<FileReference> r = a.lookup(PathVirtual.ROOT);

    Assert.assertTrue(r.isSome());
    final Some<FileReference> s = (Option.Some<FileReference>) r;
    Assert.assertTrue(s.value.type == Type.TYPE_DIRECTORY);
    Assert.assertTrue(s.value.path.equals(PathVirtual.ROOT));
    Assert.assertTrue(s.value.archive == a);
  }
}
