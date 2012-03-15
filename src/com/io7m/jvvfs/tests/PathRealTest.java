package com.io7m.jvvfs.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.PathReal;
import com.io7m.jvvfs.PathVirtual;

public class PathRealTest
{
  @Test public void testConcatenateLeadingSlash()
    throws ConstraintError
  {
    final StringBuilder base = new StringBuilder();
    base.append(File.separatorChar);
    base.append("usr");
    base.append(File.separatorChar);
    base.append("local");
    base.append(File.separatorChar);
    base.append("lib");

    final PathReal p0 = new PathReal(base.toString());
    final PathReal p1 = p0.concatenate(new PathVirtual("/x/y/z"));

    final StringBuilder expected = new StringBuilder();
    expected.append(File.separatorChar);
    expected.append("usr");
    expected.append(File.separatorChar);
    expected.append("local");
    expected.append(File.separatorChar);
    expected.append("lib");
    expected.append(File.separatorChar);
    expected.append("x");
    expected.append(File.separatorChar);
    expected.append("y");
    expected.append(File.separatorChar);
    expected.append("z");

    Assert.assertEquals(expected.toString(), p1.value);
  }

  @Test public void testEqualsNot()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    final PathReal vb0 = new PathReal("/b");
    Assert.assertFalse(va0.equals(vb0));
    Assert.assertFalse(vb0.equals(va0));
  }

  @Test public void testEqualsNotClass()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    Assert.assertFalse(va0.equals(new Integer(23)));
  }

  @Test public void testEqualsNull()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    Assert.assertFalse(va0.equals(null));
  }

  @Test public void testEqualsReflexive()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    Assert.assertEquals(va0, va0);
  }

  @Test public void testEqualsSymmetric()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    final PathReal va1 = new PathReal("/a");
    Assert.assertEquals(va0, va1);
    Assert.assertEquals(va1, va0);
  }

  @Test public void testEqualsTransitive()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    final PathReal va1 = new PathReal("/a");
    final PathReal va2 = new PathReal("/a");
    Assert.assertEquals(va0, va1);
    Assert.assertEquals(va0, va2);
    Assert.assertEquals(va1, va2);
  }

  @Test public void testHash()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    final PathReal va1 = new PathReal("/a");
    Assert.assertEquals(va0.hashCode(), va1.hashCode());
  }

  @Test public void testInit()
    throws ConstraintError
  {
    Assert.assertEquals("/a/b/c", new PathReal("/a/b/c").value);
  }

  @Test public void testToString()
    throws ConstraintError
  {
    new PathReal("/a").toString().equals("/a");
  }
}
