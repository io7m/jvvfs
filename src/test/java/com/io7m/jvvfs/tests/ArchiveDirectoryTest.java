package com.io7m.jvvfs.tests;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.ArchiveDirectory;
import com.io7m.jvvfs.PathReal;
import com.io7m.jvvfs.PathVirtual;

public class ArchiveDirectoryTest
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

  @Test public void testToStringDifferent()
    throws IOException,
      ConstraintError
  {
    final PathReal r0 = new PathReal("test-archives/archive0");
    final PathReal r1 = new PathReal("test-archives/archive1");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r0,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());
    final ArchiveDirectory z1 =
      new ArchiveDirectory(
        r1,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    Assert.assertFalse(z0.toString().equals(z1.toString()));
  }

  @Test public void testToStringSame()
    throws IOException,
      ConstraintError
  {
    final PathReal r = new PathReal("test-archives/archive0");
    final ArchiveDirectory z0 =
      new ArchiveDirectory(
        r,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());
    final ArchiveDirectory z1 =
      new ArchiveDirectory(
        r,
        new PathVirtual("/"),
        ArchiveDirectoryTest.getLog());

    Assert.assertTrue(z0.toString().equals(z1.toString()));
  }
}
