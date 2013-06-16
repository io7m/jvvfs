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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.java.quickcheck.Characteristic;
import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.support.CharacterGenerator;
import net.java.quickcheck.generator.support.IntegerGenerator;
import net.java.quickcheck.generator.support.StringGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Pair;
import com.io7m.jvvfs.NameTest.ValidNameGenerator;

public final class PathVirtualTest
{
  /**
   * A random virtual path generator.
   */

  final static class PathVirtualGenerator implements Generator<PathVirtual>
  {
    private final @Nonnull ValidNameGenerator gen;

    PathVirtualGenerator()
    {
      this.gen = new NameTest.ValidNameGenerator();
    }

    @Override public PathVirtual next()
    {
      final int count = (int) Math.round(Math.random() * 16);
      final ArrayList<String> names = new ArrayList<String>();

      for (int index = 0; index < count; ++index) {
        final String name = this.gen.next();
        names.add(name);
      }

      try {
        return PathVirtual.ofNames(names);
      } catch (final ConstraintError e) {
        throw new AssertionError("Invalid name generated, report this bug!");
      }
    }
  }

  /**
   * A generator for pairs of virtual paths.
   */

  final static class PathVirtualPairGenerator implements
    Generator<Pair<PathVirtual, PathVirtual>>
  {
    private final @Nonnull PathVirtualGenerator gen;

    public PathVirtualPairGenerator()
    {
      this.gen = new PathVirtualGenerator();
    }

    @Override public Pair<PathVirtual, PathVirtual> next()
    {
      return new Pair<PathVirtual, PathVirtual>(
        this.gen.next(),
        this.gen.next());
    }
  }

  static void runWithGenerator(
    final Characteristic<PathVirtual> c)
  {
    QuickCheck.forAll(new PathVirtualGenerator(), c);
  }

  static void runWithNameListGenerator(
    final Characteristic<List<String>> c)
  {
    QuickCheck.forAll(new NameTest.ValidNameListGenerator(), c);
  }

  static void runWithPairGenerator(
    final Characteristic<Pair<PathVirtual, PathVirtual>> c)
  {
    QuickCheck.forAll(new PathVirtualPairGenerator(), c);
  }

  @SuppressWarnings("static-method") @Test public void testEqualsClass()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          final IntegerGenerator g = new IntegerGenerator();
          Assert.assertFalse(p.equals(g.next()));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testEqualsContent()
  {
    PathVirtualTest
      .runWithNameListGenerator(new AbstractCharacteristic<List<String>>() {
        @Override protected void doSpecify(
          final @Nonnull List<String> names)
          throws Throwable
        {
          final PathVirtual p0 = PathVirtual.ofNames(names);
          final PathVirtual p1 = PathVirtual.ofNames(names);
          Assert.assertEquals(p0, p1);
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testEqualsNull()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          Assert.assertFalse(p.equals(null));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testHashcodeContent()
  {
    PathVirtualTest
      .runWithNameListGenerator(new AbstractCharacteristic<List<String>>() {
        @Override protected void doSpecify(
          final @Nonnull List<String> names)
          throws Throwable
        {
          final PathVirtual p0 = PathVirtual.ofNames(names);
          final PathVirtual p1 = PathVirtual.ofNames(names);
          Assert.assertEquals(p0.hashCode(), p1.hashCode());
        }
      });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsAncestorOfAppend()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p0)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final PathVirtual p1 = p0.appendName(g.next());
          final PathVirtual p2 = p1.appendName(g.next());
          Assert.assertTrue(p0.isAncestorOf(p2));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsAncestorOfAppendPath()
  {
    PathVirtualTest
      .runWithPairGenerator(new AbstractCharacteristic<Pair<PathVirtual, PathVirtual>>() {
        @Override protected void doSpecify(
          final @Nonnull Pair<PathVirtual, PathVirtual> pair)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final PathVirtual p0l = pair.first.appendName(g.next());
          Assert.assertTrue(p0l.isAncestorOf(p0l.append(p0l)));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsAncestorOfEqualFalse()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p0)
          throws Throwable
        {
          Assert.assertFalse(p0.isAncestorOf(p0));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsAncestorOfFalse()
  {
    PathVirtualTest
      .runWithPairGenerator(new AbstractCharacteristic<Pair<PathVirtual, PathVirtual>>() {
        @Override protected void doSpecify(
          final @Nonnull Pair<PathVirtual, PathVirtual> pair)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final PathVirtual p0 = pair.first.appendName(g.next());
          final PathVirtual p1 = pair.second.appendName(g.next());
          Assert.assertFalse(p0.isAncestorOf(p1));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testIsAncestorOfRoot()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final PathVirtual p0 = p.appendName(g.next());
          Assert.assertFalse(p0.isAncestorOf(PathVirtual.ROOT));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testIsParentOfAppend()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p0)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final PathVirtual p1 = p0.appendName(g.next());
          Assert.assertTrue(p0.isParentOf(p1));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsParentOfAppendFalse()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p0)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final PathVirtual p0l0 = p0.appendName(g.next());
          final PathVirtual p0r0 = p0.appendName(g.next());
          final PathVirtual p0r1 = p0r0.appendName(g.next());
          Assert.assertFalse(p0l0.isParentOf(p0r1));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testIsParentOfEqualFalse()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p0)
          throws Throwable
        {
          Assert.assertFalse(p0.isParentOf(p0));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testIsParentOfFalse()
  {
    PathVirtualTest
      .runWithPairGenerator(new AbstractCharacteristic<Pair<PathVirtual, PathVirtual>>() {
        @Override protected void doSpecify(
          final @Nonnull Pair<PathVirtual, PathVirtual> pair)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();

          final PathVirtual p0 = pair.first;
          PathVirtual p1 = pair.second;
          if (Math.abs(pair.first.length() - pair.second.length()) == 1) {
            p1 = p1.appendName(g.next());
          }

          Assert.assertFalse(p0.isParentOf(p1));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testIsRootAncestorOf()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final PathVirtual p0 = p.appendName(g.next());
          Assert.assertTrue(PathVirtual.ROOT.isAncestorOf(p0));
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testIsRootFalse()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          Assert.assertFalse(p.appendName(g.next()).isRoot());
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testOfString()
  {
    PathVirtualTest
      .runWithNameListGenerator(new AbstractCharacteristic<List<String>>() {
        @Override protected void doSpecify(
          final @Nonnull List<String> names)
          throws Throwable
        {
          final StringBuilder s = new StringBuilder("/");
          for (int index = 0; index < names.size(); ++index) {
            s.append(names.get(index));

            if ((index + 1) == names.size()) {
              // No slash
            } else {
              s.append("/");
            }
          }

          final String s0 = PathVirtual.ofString(s.toString()).toString();
          final String s1 = s.toString();
          Assert.assertEquals(s0, s1);
        }
      });
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testOfStringEmpty()
      throws ConstraintError
  {
    PathVirtual.ofString("");
  }

  @SuppressWarnings("static-method") @Test public void testOfStringLax()
  {
    PathVirtualTest
      .runWithNameListGenerator(new AbstractCharacteristic<List<String>>() {
        @Override protected void doSpecify(
          final @Nonnull List<String> names)
          throws Throwable
        {
          final StringGenerator slash_g =
            new StringGenerator(
              new IntegerGenerator(1, 16),
              new CharacterGenerator('/', '/'));

          final StringBuilder s = new StringBuilder(slash_g.next());
          for (int index = 0; index < names.size(); ++index) {
            s.append(names.get(index));
            s.append(slash_g.next());
          }

          final String s0 = PathVirtual.ofStringLax(s.toString()).toString();
          final String s1 = PathVirtual.ofStringLaxScrub(s.toString());
          Assert.assertEquals(s0, s1);
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testOfStringLaxRoot()
    throws ConstraintError
  {
    final PathVirtual p = PathVirtual.ofStringLax("/////////");
    Assert.assertTrue(p.isRoot());
    Assert.assertEquals(p.toString(), "/");
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testOfStringNonEmpty()
      throws ConstraintError
  {
    PathVirtual.ofString(" ");
  }

  @SuppressWarnings("static-method") @Test public void testOfStringRoot()
    throws ConstraintError
  {
    final PathVirtual p = PathVirtual.ofString("/");
    Assert.assertTrue(p.isRoot());
    Assert.assertEquals(p.toString(), "/");
  }

  @SuppressWarnings("static-method") @Test public void testRootCompare()
  {
    Assert.assertEquals(PathVirtual.ROOT.compareTo(PathVirtual.ROOT), 0);
  }

  @SuppressWarnings("static-method") @Test public void testRootEquals()
  {
    Assert.assertEquals(PathVirtual.ROOT, PathVirtual.ROOT);
  }

  @SuppressWarnings("static-method") @Test public void testRootImage()
  {
    Assert.assertEquals(PathVirtual.ROOT.toString(), "/");
  }

  @SuppressWarnings("static-method") @Test public void testRootIsRoot()
  {
    Assert.assertTrue(PathVirtual.ROOT.isRoot());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testSubtractAppendNameIdentity()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final String n0 = g.next();
          final String n1 = g.next();

          final PathVirtual p0 = p.appendName(n0);
          final PathVirtual p1 = p0.appendName(n1);
          final PathVirtual pr = p1.subtract(p0);

          Assert.assertEquals("/" + n1, pr.toString());
        }
      });
  }

  @SuppressWarnings("static-method") @Test public
    void
    testSubtractAppendSelfIdentity()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          final ValidNameGenerator g = new NameTest.ValidNameGenerator();
          final String n0 = g.next();

          final PathVirtual p0 = p.appendName(n0);
          final PathVirtual p1 = p0.append(p0);
          final PathVirtual pr = p1.subtract(p0);

          Assert.assertEquals(pr, p0);
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testSubtractFromRoot()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          Assert.assertEquals(PathVirtual.ROOT.subtract(p), PathVirtual.ROOT);
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testSubtractRootFrom()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          Assert.assertEquals(p.subtract(PathVirtual.ROOT), p);
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testSubtractSelfRoot()
  {
    PathVirtualTest
      .runWithGenerator(new AbstractCharacteristic<PathVirtual>() {
        @Override protected void doSpecify(
          final @Nonnull PathVirtual p)
          throws Throwable
        {
          Assert.assertEquals(p.subtract(p), PathVirtual.ROOT);
        }
      });
  }

  @SuppressWarnings("static-method") @Test public void testAncestorSpecific()
    throws ConstraintError
  {
    final PathVirtual p = PathVirtual.ofString("/a");
    final PathVirtual u = PathVirtual.ofString("/a/c");

    Assert.assertFalse(u.isAncestorOf(p));
    Assert.assertTrue(p.isAncestorOf(u));
  }
}
