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
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;

/**
 * <p>
 * A virtual path represents a path in the virtual filesystem.
 * </p>
 * <p>
 * Virtual paths are always absolute.
 * </p>
 * <p>
 * The concrete syntax of virtual paths is given by the following EBNF grammar
 * (where <code>name</code> indicates a string representing a valid name):
 * </p>
 * 
 * <pre>
 * path = "/" , [ name , ("/" , name)* ] ;
 * </pre>
 * <p>
 * A virtual path is conceptually a list of names, with the empty list
 * representing the root directory.
 * </p>
 * 
 * @see Name#isValid(String)
 */

@Immutable public final class PathVirtual implements Comparable<PathVirtual>
{
  private final @Nonnull List<String>      names;
  private final @Nonnull String            image;

  /**
   * <p>
   * A virtual path representing the root directory.
   * </p>
   */

  public static final @Nonnull PathVirtual ROOT;

  static {
    ROOT = new PathVirtual();
  }

  /**
   * <p>
   * Produce a virtual path from the list of names. The list is assumed to be
   * in the correct order. That is, the path <code>/usr/bin/ps</code> would be
   * represented by the list of names <code>"usr" "bin" "ps"</code>, in that
   * order.
   * </p>
   * 
   * @throws ConstraintError
   *           Iff any of the values in <code>names</code> are not valid.
   * @see Name#isValid(String)
   */

  public static @Nonnull PathVirtual ofNames(
    final @Nonnull List<String> names)
    throws ConstraintError
  {
    Constraints.constrainNotNull(names, "Names not null");
    for (final String name : names) {
      Constraints.constrainArbitrary(Name.isValid(name), "Name is valid");
    }

    return new PathVirtual(names);
  }

  /**
   * <p>
   * Parse the given <code>path</code> according to the EBNF grammar given at
   * the start of this file, and produce a virtual path.
   * </p>
   * 
   * @throws ConstraintError
   *           If the given path does not conform to the given grammar (such
   *           as being empty, not absolute, or containing a name that is not
   *           valid).
   * 
   * @see Name#isValid(String)
   */

  public static @Nonnull PathVirtual ofString(
    final @Nonnull String path)
    throws ConstraintError
  {
    Constraints.constrainArbitrary(path.length() > 0, "Path is not empty");
    Constraints.constrainArbitrary(
      path.charAt(0) == '/',
      "Path begins with slash");

    if (path.equals("/")) {
      return PathVirtual.ROOT;
    }

    final String[] elements = path.substring(1).split("/");
    final ArrayList<String> names = new ArrayList<String>();

    for (final String name : elements) {
      Constraints.constrainArbitrary(Name.isValid(name), "Name is valid");
      names.add(name);
    }

    return new PathVirtual(names);
  }

  /**
   * <p>
   * Normalize <code>path</code> by stripping trailing slashes and eliminating
   * multiple consecutive slashes, and then pass the result to
   * {@link #ofString(String)}.
   * </p>
   */

  public static @Nonnull PathVirtual ofStringLax(
    final @Nonnull String path)
    throws ConstraintError
  {
    final String result = PathVirtual.ofStringLaxScrub(path);
    if (result.equals("/")) {
      return PathVirtual.ROOT;
    }
    return PathVirtual.ofString(result);
  }

  static @Nonnull String ofStringLaxScrub(
    final @Nonnull String path)
  {
    final String result =
      path.replaceAll("/+", "/").replaceAll("/+$", "").replaceAll("^/", "");
    if (result.equals("")) {
      return "/";
    }
    return "/" + result;
  }

  private PathVirtual()
  {
    this.names = new ArrayList<String>();
    this.image = "/";
  }

  private PathVirtual(
    final @Nonnull List<String> p)
  {
    final int size = p.size();
    final StringBuilder b = new StringBuilder("/");

    for (int index = 0; index < size; ++index) {
      final String name = p.get(index);
      b.append(name);

      if ((index + 1) == size) {
        // No slash
      } else {
        b.append("/");
      }
    }

    this.names = p;
    this.image = b.toString();
  }

  /**
   * <p>
   * Append all elements of <code>p</code> to the current path, returning a
   * new path.
   * </p>
   */

  public @Nonnull PathVirtual append(
    final @Nonnull PathVirtual p)
  {
    final ArrayList<String> new_names = new ArrayList<String>(this.names);
    new_names.addAll(p.names);
    return new PathVirtual(new_names);
  }

  /**
   * <p>
   * Append the name <code>name</code> to the current path, returning a new
   * path.
   * </p>
   * 
   * @throws ConstraintError
   *           Iff <code>name</code> is not valid.
   * @see Name#isValid(String)
   */

  public @Nonnull PathVirtual appendName(
    final @Nonnull String name)
    throws ConstraintError
  {
    Constraints.constrainArbitrary(Name.isValid(name), "Name is valid");

    final ArrayList<String> new_names = new ArrayList<String>(this.names);
    new_names.add(name);
    return new PathVirtual(new_names);
  }

  /**
   * <p>
   * The ordering relation for virtual paths.
   * </p>
   * <p>
   * <ul>
   * <li>If <code>p0.size() &lt; p1.size()</code>, then
   * <code>p0 &lt; p1</code></li>
   * <li>If <code>p0.size() &gt; p1.size()</code>, then
   * <code>p0 &gt; p1</code></li>
   * <li>If <code>p0.size() == p1.size()</code>, then:
   * <ul>
   * <li><code>∃i. p0[i] &lt; p1[i] → p0 &lt; p1</code></li>
   * <li><code>∃i. p0[i] &gt; p1[i] → p0 &gt; p1</code></li>
   * <li>Otherwise, <code>p0 == p1</code>.</li>
   * </ul>
   * </li>
   * </ul>
   * </p>
   */

  @Override public int compareTo(
    final PathVirtual o)
  {
    if (this.names.size() < o.names.size()) {
      return -1;
    }
    if (this.names.size() > o.names.size()) {
      return 1;
    }

    for (int index = 0; index < this.names.size(); ++index) {
      final String s0 = this.names.get(index);
      final String s1 = o.names.get(index);

      final int r = s0.compareTo(s1);
      if (r != 0) {
        return r;
      }
    }

    return 0;
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final PathVirtual other = (PathVirtual) obj;
    return this.image.equals(other.image);
  }

  /**
   * Retrieve the last element of this path.
   * 
   * @return <code>None</code> iff this path is empty.
   */

  public @Nonnull Option<String> getBaseName()
  {
    if (this.names.isEmpty() == false) {
      return new Option.Some<String>(this.names.get(this.names.size() - 1));
    }
    return new Option.None<String>();
  }

  /**
   * Retrieve the component of the path at <code>index</code>, if any.
   */

  String getUnsafe(
    final int index)
  {
    return this.names.get(index);
  }

  @Override public int hashCode()
  {
    return this.image.hashCode();
  }

  /**
   * <p>
   * Determine whether <code>p</code> is an ancestor of the current path.
   * </p>
   * <p>
   * <code>p0</code> is an ancestor of <code>p1</code> iff:
   * </p>
   * 
   * <pre>
   * p0 != p1 /\ ∃ p : PathVirtual. p0.append(p) == p1
   * </pre>
   */

  public boolean isAncestorOf(
    final @Nonnull PathVirtual p)
  {
    /**
     * Early exit: the paths are equal and therefore one cannot be the
     * ancestor of the other.
     */

    if (this.image.equals(p.image)) {
      return false;
    }

    /**
     * Early exit: the current path is root, and therefore must be an ancestor
     * of the other (given that they're not equal).
     */

    if (this.isRoot()) {
      return true;
    }

    /**
     * Early exit: the other path is root, and therefore the current path
     * cannot be an ancestor of it.
     */

    if (p.isRoot()) {
      return false;
    }

    /**
     * Otherwise, the paths may share a common prefix.
     * 
     * If the current path has fewer components than the other, and the
     * components in this path match the first components of the other path,
     * then this path is an ancestor of the other.
     */

    if (this.names.size() < p.names.size()) {
      for (int index = 0; index < this.names.size(); ++index) {
        final String n0 = this.names.get(index);
        final String n1 = p.names.get(index);
        if (n0.equals(n1) == false) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  /**
   * <p>
   * Determine whether <code>p0</code> is the parent of <code>p1</code>.
   * </p>
   * <p>
   * <code>p0</code> is the parent of <code>p1</code> iff:
   * </p>
   * 
   * <pre>
   * ∃ n : String. p0.appendName(n) == p1
   * </pre>
   */

  public boolean isParentOf(
    final @Nonnull PathVirtual p)
  {
    if (this.image.equals(p.image)) {
      return false;
    }

    final int p0s = this.names.size();
    final int p1s = p.names.size();

    if ((p1s - 1) == p0s) {
      final int ms = Math.min(p0s, p1s);

      for (int index = 0; index < ms; ++index) {
        final String n0 = this.names.get(index);
        final String n1 = p.names.get(index);
        if (n0.equals(n1) == false) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  boolean isRoot()
  {
    return this.names.size() == 0;
  }

  /**
   * Return the number of components in the current path.
   */

  int length()
  {
    return this.names.size();
  }

  /**
   * <p>
   * "Subtract" the path <code>p</code> from the current path.
   * </p>
   * <p>
   * The subtraction of <code>p1</code> from <code>p0</code> is defined as
   * removing the first <code>p1.length</code> elements of <code>p0</code>, if
   * <code>p0</code> is an ancestor of or is equal to <code>p1</code>.
   * </p>
   */

  public @Nonnull PathVirtual subtract(
    final @Nonnull PathVirtual other)
  {
    final boolean ancestor = other.isAncestorOf(this);
    final boolean equal = other.equals(this);

    if (ancestor || equal) {
      final LinkedList<String> new_names = new LinkedList<String>(this.names);
      for (int index = 0; index < other.names.size(); ++index) {
        new_names.remove(0);
      }
      return new PathVirtual(new_names);
    }

    return this;
  }

  @Override public @Nonnull String toString()
  {
    return this.image;
  }
}
