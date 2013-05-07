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

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.FilesystemError.Code;

/**
 * Specifically test the slightly unsafe
 * {@link Filesystem#mountUnsafeClasspathItem(Class, PathVirtual)} function.
 */

public class FilesystemClasspathTest
{
  private static Filesystem makeFS()
    throws IOException,
      ConstraintError
  {
    return new Filesystem(TestData.getLog());
  }

  /**
   * Mount a file that exists inside a directory on the classpath.
   * 
   * @throws IOException
   * @throws FilesystemError
   * @throws ConstraintError
   */

  @SuppressWarnings({ "static-method", "resource" }) @Test public
    void
    testFilesystemClasspathMountFile()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final Filesystem fs = FilesystemClasspathTest.makeFS();
    fs.mountUnsafeClasspathItem(Filesystem.class, new PathVirtual("/"));

    final InputStream stream =
      fs.openFile(new PathVirtual("/com/io7m/jvvfs/Filesystem.class"));
    Assert.assertNotNull(stream);
    stream.close();
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemClasspathMountRejected()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    try {
      final Filesystem fs = FilesystemClasspathTest.makeFS();
      fs.mount("nonexistent", "/");
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }
}
