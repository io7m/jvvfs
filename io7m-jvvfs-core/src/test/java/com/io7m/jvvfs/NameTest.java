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

import java.util.List;

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.CharacterGenerator;
import net.java.quickcheck.generator.support.IntegerGenerator;
import net.java.quickcheck.generator.support.ListGenerator;
import net.java.quickcheck.generator.support.StringGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;

public final class NameTest
{
  final static class ValidNameGenerator implements Generator<String>
  {
    private final @Nonnull StringGenerator gen;

    ValidNameGenerator()
    {
      final CharacterGenerator cgen = new CharacterGenerator('a', 'z');
      final IntegerGenerator igen = new IntegerGenerator(1, 16);
      this.gen = new StringGenerator(igen, cgen);
    }

    @Override public String next()
    {
      return this.gen.next();
    }
  }

  final static class ValidNameListGenerator implements
    Generator<List<String>>
  {
    private final ListGenerator<String> gen;

    public ValidNameListGenerator()
    {
      this.gen = new ListGenerator<String>(new ValidNameGenerator(), 1, 16);
    }

    @Override public List<String> next()
    {
      return this.gen.next();
    }
  }

  @SuppressWarnings("static-method") @Test public void testInvalidBackslash()
    throws ConstraintError
  {
    Assert.assertFalse(Name.isValid("a\\b"));
  }

  @SuppressWarnings("static-method") @Test public void testInvalidColon()
    throws ConstraintError
  {
    Assert.assertFalse(Name.isValid("a:b"));
  }

  @SuppressWarnings("static-method") @Test public void testInvalidDot()
    throws ConstraintError
  {
    Assert.assertFalse(Name.isValid("a..b"));
  }

  @SuppressWarnings("static-method") @Test public void testInvalidEmpty()
    throws ConstraintError
  {
    Assert.assertFalse(Name.isValid(""));
  }

  @SuppressWarnings("static-method") @Test public void testInvalidNull()
    throws ConstraintError
  {
    Assert.assertFalse(Name.isValid("a\0b"));
  }

  @SuppressWarnings("static-method") @Test public void testInvalidSlash()
    throws ConstraintError
  {
    Assert.assertFalse(Name.isValid("a/b"));
  }

  @SuppressWarnings("static-method") @Test public void testValid()
    throws ConstraintError
  {
    Assert.assertTrue(Name.isValid("usr"));
  }

  @SuppressWarnings("static-method") @Test public void testValidDot()
    throws ConstraintError
  {
    Assert.assertTrue(Name.isValid("file.txt"));
  }

  @SuppressWarnings("static-method") @Test public void testValidDotEnd()
    throws ConstraintError
  {
    Assert.assertTrue(Name.isValid("file."));
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testValidNull()
      throws ConstraintError
  {
    Name.isValid(null);
  }
}
