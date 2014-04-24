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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jvvfs.FileReference.Type;
import com.io7m.jvvfs.FilesystemError.Code;

public final class ArchiveZipTest extends ArchiveContract<ArchiveZipKind>
{
  @Override Archive<ArchiveZipKind> getArchive(
    final String basename,
    final PathVirtual mount)
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r =
      new PathReal(new File(tempdir, basename).toString() + ".zip");
    return new ArchiveZip(TestData.getLog(), r, mount);
  }

  @Test(expected = FilesystemError.class) public void testCorrupt()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    try {
      this.getArchive("encrypted", PathVirtual.ROOT);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_ARCHIVE_DAMAGED, e.getCode());
      throw e;
    }
  }

  @Test public void testListDirectoryImplicit()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<ArchiveZipKind> a =
      this.getArchive("single-file-and-subdir-implicit", PathVirtual.ROOT);
    try {
      final Set<String> files = a.listDirectory(PathVirtual.ROOT);
      Assert.assertEquals(2, files.size());
      Assert.assertTrue(files.contains("file.txt"));
      Assert.assertTrue(files.contains("subdir"));
    } finally {
      a.close();
    }
  }

  @Test public void testListDirectoryImplicitImplicit()
    throws FileNotFoundException,
      IOException,
      FilesystemError
  {
    final Archive<ArchiveZipKind> a =
      this.getArchive("single-file-and-subdir-implicit", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      final Set<String> files = a.listDirectory(p);
      Assert.assertEquals(1, files.size());
      Assert.assertTrue(files.contains("file.txt"));
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleDirectoryImplicit()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<ArchiveZipKind> a =
      this.getArchive("single-file-and-subdir-implicit", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/subdir");
      final OptionType<FileReference<ArchiveZipKind>> r = a.lookup(p);

      Assert.assertTrue(r.isSome());
      final Some<FileReference<ArchiveZipKind>> s =
        (Some<FileReference<ArchiveZipKind>>) r;
      Assert.assertTrue(s.get().getType() == Type.TYPE_DIRECTORY);
      Assert.assertTrue(s.get().getPath().equals(p));
      Assert.assertTrue(s.get().getArchive() == a);
    } finally {
      a.close();
    }
  }

  @Test public void testLookupSingleFileDirectoryImplicit()
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final Archive<ArchiveZipKind> a =
      this.getArchive("single-file-and-subdir-implicit", PathVirtual.ROOT);

    try {
      final PathVirtual p = PathVirtual.ofString("/subdir/file.txt");
      final OptionType<FileReference<ArchiveZipKind>> r = a.lookup(p);

      Assert.assertTrue(r.isSome());
      final Some<FileReference<ArchiveZipKind>> s =
        (Some<FileReference<ArchiveZipKind>>) r;
      Assert.assertTrue(s.get().getType() == Type.TYPE_FILE);
      Assert.assertTrue(s.get().getPath().equals(p));
      Assert.assertTrue(s.get().getArchive() == a);
    } finally {
      a.close();
    }
  }
}
