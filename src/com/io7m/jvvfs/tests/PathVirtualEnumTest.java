package com.io7m.jvvfs.tests;

import junit.framework.Assert;

import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.jvvfs.PathVirtualEnum;

public class PathVirtualEnumTest
{
  @Test public void testABC()
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

  @Test public void testRoot()
    throws ConstraintError
  {
    final PathVirtualEnum e = new PathVirtualEnum(new PathVirtual("/"));

    Assert.assertTrue(e.hasMoreElements());
    final PathVirtual p0 = e.nextElement();
    Assert.assertFalse(e.hasMoreElements());
    Assert.assertEquals("/", p0.toString());
  }
}
