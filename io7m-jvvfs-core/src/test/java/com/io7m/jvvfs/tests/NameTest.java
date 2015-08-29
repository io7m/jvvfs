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

package com.io7m.jvvfs.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jnull.NullCheckException;
import com.io7m.jvvfs.Name;

@SuppressWarnings("static-method") public final class NameTest
{
  @Test public void testInvalidBackslash()
  {
    Assert.assertFalse(Name.isValid("a\\b"));
  }

  @Test public void testInvalidColon()
  {
    Assert.assertFalse(Name.isValid("a:b"));
  }

  @Test public void testInvalidDot()
  {
    Assert.assertFalse(Name.isValid("a..b"));
  }

  @Test public void testInvalidEmpty()
  {
    Assert.assertFalse(Name.isValid(""));
  }

  @Test public void testInvalidNull()
  {
    Assert.assertFalse(Name.isValid("a\0b"));
  }

  @Test public void testInvalidSlash()
  {
    Assert.assertFalse(Name.isValid("a/b"));
  }

  @Test public void testValid()
  {
    Assert.assertTrue(Name.isValid("usr"));
  }

  @Test public void testValidDot()
  {
    Assert.assertTrue(Name.isValid("file.txt"));
  }

  @Test public void testValidDotEnd()
  {
    Assert.assertTrue(Name.isValid("file."));
  }

  @Test(expected = NullCheckException.class) public void testValidNull()
    throws Throwable
  {
    try {
      // Name.isValid(null);

      final Method m = Name.class.getMethod("isValid", String.class);
      assert m != null;
      m.invoke(null, new Object[] { null });

    } catch (final InvocationTargetException e) {
      throw e.getCause();
    }
  }
}
