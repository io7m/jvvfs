package com.io7m.jvvfs;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;

public class PathVirtualTest
{
  @SuppressWarnings("static-method") @Test public void testBasenameRoot()
    throws ConstraintError
  {
    Assert.assertTrue(new PathVirtual("/").baseName().equals("/"));
  }

  @SuppressWarnings("static-method") @Test public void testClean()
    throws ConstraintError
  {
    Assert.assertEquals("/a/b/c", new PathVirtual(
      "//////a//////b//////c/////").toString());
  }

  @SuppressWarnings("static-method") @Test public void testComponentsThree()
    throws ConstraintError
  {
    final List<String> components =
      new PathVirtual("/a/b/c").pathComponents();

    Assert.assertEquals(3, components.size());
    Assert.assertEquals("a", components.get(0));
    Assert.assertEquals("b", components.get(1));
    Assert.assertEquals("c", components.get(2));
  }

  @SuppressWarnings("static-method") @Test public void testComponentsZero()
    throws ConstraintError
  {
    final List<String> components = new PathVirtual("/").pathComponents();
    Assert.assertEquals(0, components.size());
  }

  @SuppressWarnings("static-method") @Test public void testEqualsNot()
    throws ConstraintError
  {
    final PathVirtual va0 = new PathVirtual("/a");
    final PathVirtual vb0 = new PathVirtual("/b");
    Assert.assertFalse(va0.equals(vb0));
    Assert.assertFalse(vb0.equals(va0));
  }

  @SuppressWarnings("static-method") @Test public void testEqualsNotClass()
    throws ConstraintError
  {
    final PathVirtual va0 = new PathVirtual("/a");
    Assert.assertFalse(va0.equals(new Integer(23)));
  }

  @SuppressWarnings("static-method") @Test public void testEqualsNull()
    throws ConstraintError
  {
    final PathVirtual va0 = new PathVirtual("/a");
    Assert.assertFalse(va0.equals(null));
  }

  @SuppressWarnings("static-method") @Test public void testEqualsReflexive()
    throws ConstraintError
  {
    final PathVirtual va0 = new PathVirtual("/a");
    Assert.assertEquals(va0, va0);
  }

  @SuppressWarnings("static-method") @Test public void testEqualsSymmetric()
    throws ConstraintError
  {
    final PathVirtual va0 = new PathVirtual("/a");
    final PathVirtual va1 = new PathVirtual("/a");
    Assert.assertEquals(va0, va1);
    Assert.assertEquals(va1, va0);
  }

  @SuppressWarnings("static-method") @Test public void testEqualsTransitive()
    throws ConstraintError
  {
    final PathVirtual va0 = new PathVirtual("/a");
    final PathVirtual va1 = new PathVirtual("/a");
    final PathVirtual va2 = new PathVirtual("/a");
    Assert.assertEquals(va0, va1);
    Assert.assertEquals(va0, va2);
    Assert.assertEquals(va1, va2);
  }

  @SuppressWarnings("static-method") @Test public void testHash()
    throws ConstraintError
  {
    final PathVirtual va0 = new PathVirtual("/a");
    final PathVirtual va1 = new PathVirtual("/a");
    Assert.assertEquals(va0.hashCode(), va1.hashCode());
  }

  @SuppressWarnings({ "unused", "static-method" }) @Test(
    expected = ConstraintError.class) public void testInitNoLeadingSlash()
    throws ConstraintError
  {
    new PathVirtual("a/b/c");
  }

  @SuppressWarnings("static-method") @Test public void testInitOK()
    throws ConstraintError
  {
    final PathVirtual path = new PathVirtual("/a/b/c");
    Assert.assertEquals("/a/b/c", path.toString());
  }

  @SuppressWarnings({ "unused", "static-method" }) @Test(
    expected = ConstraintError.class) public void testInitWithBackslash()
    throws ConstraintError
  {
    new PathVirtual("/a\\b\\c");
  }

  @SuppressWarnings({ "unused", "static-method" }) @Test(
    expected = ConstraintError.class) public void testInitWithDots()
    throws ConstraintError
  {
    new PathVirtual("/a/../c");
  }

  @SuppressWarnings("static-method") @Test public void testIsParent()
    throws ConstraintError
  {
    final PathVirtual abc = new PathVirtual("/a/b/c");
    final PathVirtual ab = new PathVirtual("/a/b");
    final PathVirtual a = new PathVirtual("/a");
    final PathVirtual r = new PathVirtual("/");

    Assert.assertTrue(r.isParentOf(r));
    Assert.assertTrue(r.isParentOf(a));
    Assert.assertTrue(r.isParentOf(ab));
    Assert.assertTrue(r.isParentOf(abc));

    Assert.assertFalse(a.isParentOf(r));
    Assert.assertFalse(a.isParentOf(a));
    Assert.assertTrue(a.isParentOf(ab));
    Assert.assertTrue(a.isParentOf(abc));

    Assert.assertFalse(ab.isParentOf(r));
    Assert.assertFalse(ab.isParentOf(a));
    Assert.assertFalse(ab.isParentOf(ab));
    Assert.assertTrue(ab.isParentOf(abc));

    Assert.assertFalse(abc.isParentOf(r));
    Assert.assertFalse(abc.isParentOf(a));
    Assert.assertFalse(abc.isParentOf(ab));
    Assert.assertFalse(abc.isParentOf(abc));
  }

  @SuppressWarnings("static-method") @Test public void testIsParentRoot()
    throws ConstraintError
  {
    Assert.assertTrue(new PathVirtual("/").isParentOf(new PathVirtual("/")));
  }

  @SuppressWarnings("static-method") @Test public void testIsRoot()
    throws ConstraintError
  {
    Assert.assertTrue(new PathVirtual("/").isRoot());
  }

  @SuppressWarnings("static-method") @Test public void testParentRoot()
    throws ConstraintError
  {
    Assert.assertEquals("/", new PathVirtual("/").parent().toString());
  }

  @SuppressWarnings("static-method") @Test public void testParents()
    throws ConstraintError
  {
    final PathVirtual abc = new PathVirtual("/a/b/c");

    final PathVirtual ab = abc.parent();
    Assert.assertEquals("/a/b", ab.toString());
    final PathVirtual a = ab.parent();
    Assert.assertEquals("/a", a.toString());
    final PathVirtual root = a.parent();
    Assert.assertTrue(root.isRoot());
    Assert.assertEquals("/", root.toString());
  }

  @SuppressWarnings("static-method") @Test public void testRoot()
    throws ConstraintError
  {
    Assert.assertEquals("/", new PathVirtual("/").toString());
  }

  @SuppressWarnings("static-method") @Test public void testSubtract()
    throws ConstraintError
  {
    final PathVirtual abc = new PathVirtual("/a/b/c");
    final PathVirtual ab = new PathVirtual("/a/b");
    Assert.assertEquals("/c", abc.subtract(ab).toString());
  }

  @SuppressWarnings("static-method") @Test public void testSubtractAnyRoot()
    throws ConstraintError
  {
    final PathVirtual abc = new PathVirtual("/a/b/c");
    final PathVirtual r = new PathVirtual("/");
    Assert.assertEquals("/a/b/c", abc.subtract(r).toString());
  }

  @SuppressWarnings("static-method") @Test public void testSubtractNotRoot()
    throws ConstraintError
  {
    final PathVirtual abc = new PathVirtual("/a/b/c");
    final PathVirtual r = new PathVirtual("/");
    Assert.assertEquals("/", r.subtract(abc).toString());
  }

  @SuppressWarnings("static-method") @Test public void testSubtractRoot()
    throws ConstraintError
  {
    final PathVirtual r = new PathVirtual("/");
    Assert.assertEquals("/", r.subtract(r).toString());
  }

  @SuppressWarnings("static-method") @Test public void testToString()
    throws ConstraintError
  {
    Assert.assertTrue(new PathVirtual("/a").toString().equals("/a"));
  }
}
