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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;

public class PathRealTest
{
  @SuppressWarnings("static-method") @Test public
    void
    testConcatenateLeadingSlash()
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

  @SuppressWarnings("static-method") @Test public void testEqualsNot()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    final PathReal vb0 = new PathReal("/b");
    Assert.assertFalse(va0.equals(vb0));
    Assert.assertFalse(vb0.equals(va0));
  }

  @SuppressWarnings("static-method") @Test public void testEqualsNotClass()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    Assert.assertFalse(va0.equals(new Integer(23)));
  }

  @SuppressWarnings("static-method") @Test public void testEqualsNull()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    Assert.assertFalse(va0.equals(null));
  }

  @SuppressWarnings("static-method") @Test public void testEqualsReflexive()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    Assert.assertEquals(va0, va0);
  }

  @SuppressWarnings("static-method") @Test public void testEqualsSymmetric()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    final PathReal va1 = new PathReal("/a");
    Assert.assertEquals(va0, va1);
    Assert.assertEquals(va1, va0);
  }

  @SuppressWarnings("static-method") @Test public void testEqualsTransitive()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    final PathReal va1 = new PathReal("/a");
    final PathReal va2 = new PathReal("/a");
    Assert.assertEquals(va0, va1);
    Assert.assertEquals(va0, va2);
    Assert.assertEquals(va1, va2);
  }

  @SuppressWarnings("static-method") @Test public void testHash()
    throws ConstraintError
  {
    final PathReal va0 = new PathReal("/a");
    final PathReal va1 = new PathReal("/a");
    Assert.assertEquals(va0.hashCode(), va1.hashCode());
  }

  @SuppressWarnings("static-method") @Test public void testInit()
    throws ConstraintError
  {
    Assert.assertEquals("/a/b/c", new PathReal("/a/b/c").value);
  }

  @SuppressWarnings("static-method") @Test public void testToString()
    throws ConstraintError
  {
    new PathReal("/a").toString().equals("/a");
  }
}
