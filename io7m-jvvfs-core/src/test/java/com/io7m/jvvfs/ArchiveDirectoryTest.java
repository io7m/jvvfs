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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.ArchiveDirectory.ArchiveDirectoryReference;
import com.io7m.jvvfs.FileReference.Type;
import com.io7m.jvvfs.FilesystemError.Code;

public final class ArchiveDirectoryTest extends
  ArchiveContract<ArchiveDirectoryKind>
{
  @Override @Nonnull Archive<ArchiveDirectoryKind> getArchive(
    final @Nonnull String basename,
    final @Nonnull PathVirtual mount)
    throws FileNotFoundException,
      IOException,
      ConstraintError
  {
    final File tempdir = TestData.getTestDataDirectory();
    final PathReal r = new PathReal(new File(tempdir, basename).toString());
    return new ArchiveDirectory(TestData.getLog(), r, mount);
  }

  @Test(expected = FilesystemError.class) public void testFileVanished()
    throws FileNotFoundException,
      IOException,
      ConstraintError,
      FilesystemError
  {
    final Archive<ArchiveDirectoryKind> a =
      this.getArchive("single-file-and-subdir", PathVirtual.ROOT);
    try {
      final PathVirtual p = PathVirtual.ofString("/file.txt");

      final ArchiveDirectoryReference r =
        new ArchiveDirectory.ArchiveDirectoryReference(
          a,
          p,
          Type.TYPE_FILE,
          new File("/nonexistent"));

      a.openFileActual(r);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.getCode());
      throw e;
    } finally {
      a.close();
    }
  }
}
