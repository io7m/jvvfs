package com.io7m.jvvfs;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jlog.Log;

public class ArchiveZipTest
{
  private static Log getLog()
    throws IOException,
      ConstraintError
  {
    final Log log =
      new Log(
        PropertyUtils.loadFromFile("io7m-jvvfs.properties"),
        "com.io7m.jvvfs",
        "main");
    return log;
  }

  @SuppressWarnings("static-method") @Test public
    void
    testToStringDifferent()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    final PathReal r0 = new PathReal("test-archives/one.zip");
    final PathReal r1 = new PathReal("test-archives/two.zip");
    final ArchiveZip z0 =
      new ArchiveZip(r0, new PathVirtual("/"), ArchiveZipTest.getLog());
    final ArchiveZip z1 =
      new ArchiveZip(r1, new PathVirtual("/"), ArchiveZipTest.getLog());

    Assert.assertFalse(z0.toString().equals(z1.toString()));
  }

  @SuppressWarnings("static-method") @Test public void testToStringSame()
    throws IOException,
      ConstraintError,
      FilesystemError
  {
    final PathReal r = new PathReal("test-archives/one.zip");
    final ArchiveZip z0 =
      new ArchiveZip(r, new PathVirtual("/"), ArchiveZipTest.getLog());
    final ArchiveZip z1 =
      new ArchiveZip(r, new PathVirtual("/"), ArchiveZipTest.getLog());

    Assert.assertTrue(z0.toString().equals(z1.toString()));
  }
}
