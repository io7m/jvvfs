package com.io7m.jvvfs;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;

public class ArchiveHandlerZipTest
{
  @SuppressWarnings("static-method") @Test public void testCanHandleCases()
    throws ConstraintError
  {
    final ArchiveHandlerZip a =
      new ArchiveHandlerZip(
        new Log(new Properties(), "com.io7m.jlog", "test"));

    {
      final boolean ok = a.canHandle(new PathReal("test-archives/empty.zip"));
      Assert.assertTrue(ok);
    }

    {
      final boolean ok =
        a.canHandle(new PathReal("test-archives/Example.jar"));
      Assert.assertTrue(ok);
    }

    {
      final boolean ok =
        a.canHandle(new PathReal("test-archives/archive0/file.txt"));
      Assert.assertFalse(ok);
    }
  }
}
