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
import java.util.Enumeration;
import java.util.List;

import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * <p>
 * The sequential enumeration of the ancestors of a path is defined as a list
 * of the prefixes of the path, with the first element being the shortest (the
 * root).
 * </p>
 * <p>
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 * </p>
 */

public final class PathVirtualEnum implements Enumeration<PathVirtual>
{
  /**
   * Construct a new path enumeration.
   * 
   * @param p
   *          The path
   * @return A new enumeration.
   */

  public static PathVirtualEnum enumerate(
    final PathVirtual p)
  {
    return new PathVirtualEnum(p);
  }

  private int                index = -1;
  private final List<String> names;
  private final PathVirtual  path;

  private PathVirtualEnum(
    final PathVirtual in_path)
  {
    this.path = NullCheck.notNull(in_path, "Path");
    this.names = new ArrayList<String>();
  }

  @Override public boolean hasMoreElements()
  {
    return this.index < (this.path.length() - 1);
  }

  @Override public PathVirtual nextElement()
  {
    try {
      if (this.index == -1) {
        return PathVirtual.ROOT;
      }

      this.names.add(this.path.getUnsafe(this.index));
      return PathVirtual.ofNames(this.names);
    } catch (final FilesystemError e) {
      /**
       * Unreachable because this error can only occur on invalid names, and
       * an invalid name should never have been allowed into a virtual path in
       * the first place.
       */
      throw new UnreachableCodeException(e);
    } finally {
      ++this.index;
    }
  }
}
