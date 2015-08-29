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

package com.io7m.jvvfs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jvvfs.FilesystemError.Code;

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

public final class PathVirtual implements Comparable<PathVirtual>
{
  /**
   * <p>
   * A virtual path representing the root directory.
   * </p>
   */

  public static final PathVirtual ROOT;
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
   * @param names
   *          The list of names
   * @return A new virtual path
   * @throws FilesystemError
   *           If any of the given names are not valid
   * @see Name#isValid(String)
   */

  public static PathVirtual ofNames(
    final List<String> names)
    throws FilesystemError
  {
    NullCheck.notNull(names, "Names");

    for (int index = 0; index < names.size(); ++index) {
      final String name =
        NullCheck.notNull(names.get(index), "Names(" + index + ")");

      if (Name.isValid(name) == false) {
        final StringBuilder m = new StringBuilder();
        m.append("Name ");
        m.append(index);
        m.append(" (");
        m.append(name);
        m.append(") is not valid");
        final String r = m.toString();
        assert r != null;
        throw new FilesystemError(Code.FS_ERROR_CONSTRAINT_ERROR, r);
      }
    }

    return new PathVirtual(names);
  }

  /**
   * <p>
   * Parse the given <code>path</code> according to the EBNF grammar given at
   * the start of this file, and produce a virtual path.
   * </p>
   * 
   * @return A new virtual path
   * @param path
   *          The path to be parsed
   * @throws FilesystemError
   *           If the given path does not conform to the given grammar (such
   *           as being empty, not absolute, or containing a name that is not
   *           valid).
   * 
   * @see Name#isValid(String)
   */

  public static PathVirtual ofString(
    final String path)
    throws FilesystemError
  {
    if (path.length() == 0) {
      throw new FilesystemError(
        Code.FS_ERROR_CONSTRAINT_ERROR,
        "Path is empty");
    }
    if (path.charAt(0) != '/') {
      throw new FilesystemError(
        Code.FS_ERROR_CONSTRAINT_ERROR,
        "Path does not begin with slash (U+002F)");
    }
    if ("/".equals(path)) {
      return PathVirtual.ROOT;
    }

    final String[] elements = path.substring(1).split("/");
    final List<String> names = new ArrayList<String>();

    for (int index = 0; index < elements.length; ++index) {
      final String name =
        NullCheck.notNull(elements[index], "Elements(" + index + ")");

      if (Name.isValid(name) == false) {
        final StringBuilder m = new StringBuilder();
        m.append("Name ");
        m.append(index);
        m.append(" (");
        m.append(name);
        m.append(") is not valid");
        final String r = m.toString();
        assert r != null;
        throw new FilesystemError(Code.FS_ERROR_CONSTRAINT_ERROR, r);
      }

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
   * 
   * @return A new virtual path
   * @param path
   *          The path to be parsed
   * @throws FilesystemError
   *           If the path is not valid
   */

  public static PathVirtual ofStringLax(
    final String path)
    throws FilesystemError
  {
    final String result = PathVirtual.ofStringLaxScrub(path);
    if ("/".equals(result)) {
      return PathVirtual.ROOT;
    }
    return PathVirtual.ofString(result);
  }

  static String ofStringLaxScrub(
    final String path)
  {
    final String result =
      path.replaceAll("/+", "/").replaceAll("/+$", "").replaceAll("^/", "");
    if ("".equals(result)) {
      return "/";
    }
    return "/" + result;
  }

  private final String       image;
  private final List<String> names;

  private PathVirtual()
  {
    this.names = new ArrayList<String>();
    this.image = "/";
  }

  private PathVirtual(
    final List<String> p)
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
    this.image = NullCheck.notNull(b.toString());
  }

  /**
   * <p>
   * Append all elements of <code>p</code> to the current path, returning a
   * new path.
   * </p>
   * 
   * @param p
   *          The other path
   * @return The current path with the elements of the other path appended.
   */

  public PathVirtual append(
    final PathVirtual p)
  {
    NullCheck.notNull(p, "Path");

    final List<String> new_names = new ArrayList<String>(this.names);
    new_names.addAll(p.names);
    return new PathVirtual(new_names);
  }

  /**
   * <p>
   * Append the name <code>name</code> to the current path, returning a new
   * path.
   * </p>
   * 
   * @param name
   *          The name to append
   * @return The current path with <code>name</code> appended
   * @throws FilesystemError
   *           If the given name is not valid
   * 
   * @see Name#isValid(String)
   */

  public PathVirtual appendName(
    final String name)
    throws FilesystemError
  {
    final String actual = NullCheck.notNull(name, "Name");
    if (Name.isValid(actual) == false) {
      throw new FilesystemError(
        Code.FS_ERROR_CONSTRAINT_ERROR,
        "Name is not valid");
    }

    final List<String> new_names = new ArrayList<String>(this.names);
    new_names.add(actual);
    return new PathVirtual(new_names);
  }

  /**
   * <p>
   * The ordering relation for virtual paths.
   * </p>
   * <ul>
   *   <li>If <code>p0.size() &lt; p1.size()</code>, then <code>p0 &lt; p1</code></li>
   *   <li>If <code>p0.size() &gt; p1.size()</code>, then <code>p0 &gt; p1</code></li>
   *   <li>
   *     If <code>p0.size() == p1.size()</code>, then:
   *     <ul>
   *       <li><code>∃i. p0[i] &lt; p1[i] → p0 &lt; p1</code></li>
   *       <li><code>∃i. p0[i] &gt; p1[i] → p0 &gt; p1</code></li>
   *       <li>Otherwise, <code>p0 == p1</code>.</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * @param o
   *          The other path
   * @return A negative integer, zero, or a positive integer as this path is
   *         less than, equal to, or greater than the specified path.
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
    final @Nullable Object obj)
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

  public OptionType<String> getBaseName()
  {
    if (this.names.isEmpty() == false) {
      final String x = this.names.get(this.names.size() - 1);
      assert x != null;
      return Option.some(x);
    }
    return Option.none();
  }

  /**
   * Retrieve the component of the path at <code>index</code>, if any.
   */

  String getUnsafe(
    final int index)
  {
    final String r = this.names.get(index);
    assert r != null;
    return r;
  }

  @Override public int hashCode()
  {
    return this.image.hashCode();
  }

  /**
   * <p>
   * Determine whether the current path is an ancestor of <code>p</code>.
   * </p>
   * <p>
   * <code>p0</code> is an ancestor of <code>p1</code> iff:
   * </p>
   * 
   * <pre>
   * p0 != p1 /\ ∃ p : PathVirtual. p0.append(p) == p1
   * </pre>
   * 
   * @param p
   *          The path that may be an ancestor of the current path
   * @return <code>true</code> if the current path is an ancestor of
   *         <code>p</code>.
   */

  public boolean isAncestorOf(
    final PathVirtual p)
  {
    NullCheck.notNull(p, "Path");

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
   * Determine whether the current path is the parent of <code>p</code>.
   * </p>
   * <p>
   * <code>p0</code> is the parent of <code>p1</code> iff:
   * </p>
   * 
   * <pre>
   * ∃ n : String. p0.appendName(n) == p1
   * </pre>
   * 
   * @param p
   *          The path that may be an ancestor of the current path
   * @return <code>true</code> if the current path is the parent of
   *         <code>p</code>.
   */

  public boolean isParentOf(
    final PathVirtual p)
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

  /**
   * @return <code>true</code> if this path is the root path.
   */

  public boolean isRoot()
  {
    return this.names.size() == 0;
  }

  /**
   * @return The number of components in the current path.
   */

  public int length()
  {
    return this.names.size();
  }

  /**
   * <p>
   * "Subtract" the path <code>other</code> from the current path.
   * </p>
   * <p>
   * The subtraction of <code>p1</code> from <code>p0</code> is defined as
   * removing the first <code>p1.length</code> elements of <code>p0</code>, if
   * <code>p0</code> is an ancestor of or is equal to <code>p1</code>.
   * </p>
   * 
   * @param other
   *          The other path
   * @return The current path with <code>other</code> subtracted.
   */

  public PathVirtual subtract(
    final PathVirtual other)
  {
    final boolean ancestor = other.isAncestorOf(this);
    final boolean equal = this.equals(other);

    if (ancestor || equal) {
      final List<String> new_names = new LinkedList<String>(this.names);
      for (int index = 0; index < other.names.size(); ++index) {
        new_names.remove(0);
      }
      return new PathVirtual(new_names);
    }

    return this;
  }

  @Override public String toString()
  {
    return this.image;
  }
}
