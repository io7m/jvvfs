/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

import java.io.File;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.support.CharacterGenerator;
import net.java.quickcheck.generator.support.IntegerGenerator;
import net.java.quickcheck.generator.support.StringGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfunctional.Pair;
import com.io7m.jvvfs.PathReal;

public class PathRealTest
{
  /**
   * A generator for pairs of strings.
   */

  final static class StringPairGenerator implements
    Generator<Pair<String, String>>
  {
    private final StringGenerator gen;

    public StringPairGenerator()
    {
      this.gen =
        new StringGenerator(
          new IntegerGenerator(1, 16),
          new CharacterGenerator());
    }

    @Override public Pair<String, String> next()
    {
      final String p0 = this.gen.next();
      assert p0 != null;
      final String p1 = this.gen.next();
      assert p1 != null;
      return Pair.pair(p0, p1);
    }
  }

  @SuppressWarnings("static-method") @Test public void testEqualsClass()
  {
    final StringGenerator gen =
      new StringGenerator(
        new IntegerGenerator(1, 16),
        new CharacterGenerator());

    QuickCheck.forAll(gen, new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final String s)
        throws Throwable
      {
        final PathReal p = new PathReal(s);
        final IntegerGenerator g = new IntegerGenerator();
        Assert.assertFalse(p.equals(g.next()));
      }
    });
  }

  @SuppressWarnings("static-method") @Test public void testEqualsContent()
  {
    final StringGenerator gen =
      new StringGenerator(
        new IntegerGenerator(1, 16),
        new CharacterGenerator());

    QuickCheck.forAll(gen, new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final String s)
        throws Throwable
      {
        final PathReal p0 = new PathReal(s);
        final PathReal p1 = new PathReal(s);
        Assert.assertEquals(p0, p1);
      }
    });
  }

  @SuppressWarnings("static-method") @Test public void testEqualsNull()
  {
    final StringGenerator gen =
      new StringGenerator(
        new IntegerGenerator(1, 16),
        new CharacterGenerator());

    QuickCheck.forAll(gen, new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final String s)
        throws Throwable
      {
        final PathReal p = new PathReal(s);
        Assert.assertFalse(p.equals(null));
      }
    });
  }

  @SuppressWarnings("static-method") @Test public void testEqualsSame()
  {
    final StringGenerator gen =
      new StringGenerator(
        new IntegerGenerator(1, 16),
        new CharacterGenerator());

    QuickCheck.forAll(gen, new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final String s)
        throws Throwable
      {
        final PathReal p = new PathReal(s);
        Assert.assertEquals(p, p);
      }
    });
  }

  @SuppressWarnings("static-method") @Test public void testFileIdentity()
  {
    final StringGenerator gen =
      new StringGenerator(
        new IntegerGenerator(1, 16),
        new CharacterGenerator());

    QuickCheck.forAll(gen, new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final String s)
        throws Throwable
      {
        final File f = new File(s);
        final PathReal p = new PathReal(f);
        Assert.assertEquals(f, p.toFile());
      }
    });
  }

  @SuppressWarnings("static-method") @Test public void testHashcodeEquals()
  {
    final StringGenerator gen =
      new StringGenerator(
        new IntegerGenerator(1, 16),
        new CharacterGenerator());

    QuickCheck.forAll(gen, new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final String s)
        throws Throwable
      {
        final PathReal p0 = new PathReal(s);
        final PathReal p1 = new PathReal(s);
        Assert.assertEquals(p0.hashCode(), p1.hashCode());
      }
    });
  }

  @SuppressWarnings("static-method") @Test public void testToString()
  {
    final StringGenerator gen =
      new StringGenerator(
        new IntegerGenerator(1, 16),
        new CharacterGenerator());

    QuickCheck.forAll(gen, new AbstractCharacteristic<String>() {
      @Override protected void doSpecify(
        final String s)
        throws Throwable
      {
        final PathReal p = new PathReal(s);
        Assert.assertFalse(p.equals(p.toString()));
      }
    });
  }
}
