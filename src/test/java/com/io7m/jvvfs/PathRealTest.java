package com.io7m.jvvfs;

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.support.CharacterGenerator;
import net.java.quickcheck.generator.support.IntegerGenerator;
import net.java.quickcheck.generator.support.StringGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.functional.Pair;

public class PathRealTest
{
  /**
   * A generator for pairs of strings.
   */

  final static class StringPairGenerator implements
    Generator<Pair<String, String>>
  {
    private final @Nonnull StringGenerator gen;

    public StringPairGenerator()
    {
      this.gen =
        new StringGenerator(
          new IntegerGenerator(1, 16),
          new CharacterGenerator());
    }

    @Override public Pair<String, String> next()
    {
      return new Pair<String, String>(this.gen.next(), this.gen.next());
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
}
