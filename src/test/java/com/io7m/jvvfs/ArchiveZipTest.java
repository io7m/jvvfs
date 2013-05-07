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
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;

public class ArchiveZipTest
{
  @SuppressWarnings("static-method") @Test public
    void
    testToStringDifferent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final Log log = TestData.getLog();
    final File tempdir = TestData.getTestDataDirectory();

    final File f1 = new File("single-file.zip");
    final File f2 = new File("single-file-and-subdir.zip");
    final PathReal r0 = new PathReal(new File(tempdir, f1.toString()));
    final PathReal r1 = new PathReal(new File(tempdir, f2.toString()));

    final ArchiveZip z0 = new ArchiveZip(r0, new PathVirtual("/"), log);
    final ArchiveZip z1 = new ArchiveZip(r1, new PathVirtual("/"), log);

    Assert.assertFalse(z0.toString().equals(z1.toString()));
  }

  @SuppressWarnings("static-method") @Test public void testToStringSame()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final Log log = TestData.getLog();
    final File tempdir = TestData.getTestDataDirectory();

    final File f1 = new File("single-file.zip");
    final PathReal r = new PathReal(new File(tempdir, f1.toString()));

    final ArchiveZip z0 = new ArchiveZip(r, new PathVirtual("/"), log);
    final ArchiveZip z1 = new ArchiveZip(r, new PathVirtual("/"), log);

    Assert.assertTrue(z0.toString().equals(z1.toString()));
  }
}
