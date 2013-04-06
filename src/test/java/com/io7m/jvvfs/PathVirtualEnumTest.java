package com.io7m.jvvfs;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;

public class PathVirtualEnumTest
{
  @SuppressWarnings("static-method") @Test public void testABC()
    throws ConstraintError
  {
    final PathVirtualEnum e = new PathVirtualEnum(new PathVirtual("/a/b/c"));

    Assert.assertTrue(e.hasMoreElements());

    {
      final PathVirtual p = e.nextElement();
      Assert.assertTrue(e.hasMoreElements());
      Assert.assertEquals("/", p.toString());
    }

    {
      final PathVirtual p = e.nextElement();
      Assert.assertTrue(e.hasMoreElements());
      Assert.assertEquals("/a", p.toString());
    }

    {
      final PathVirtual p = e.nextElement();
      Assert.assertTrue(e.hasMoreElements());
      Assert.assertEquals("/a/b", p.toString());
    }

    {
      final PathVirtual p = e.nextElement();
      Assert.assertFalse(e.hasMoreElements());
      Assert.assertEquals("/a/b/c", p.toString());
    }
  }

  @SuppressWarnings("static-method") @Test public void testRoot()
    throws ConstraintError
  {
    final PathVirtualEnum e = new PathVirtualEnum(new PathVirtual("/"));

    Assert.assertTrue(e.hasMoreElements());
    final PathVirtual p0 = e.nextElement();
    Assert.assertFalse(e.hasMoreElements());
    Assert.assertEquals("/", p0.toString());
  }
}
