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
import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

/**
 * The sequential enumeration of a path is defined as a list of the prefixes
 * of the path, with the first element being the shortest (the root).
 * 
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 */

@NotThreadSafe public final class PathVirtualEnum implements
  Enumeration<PathVirtual>
{
  private final @Nonnull PathVirtual       path;
  private final @Nonnull ArrayList<String> names;
  private int                              index = -1;

  PathVirtualEnum(
    final @Nonnull PathVirtual path)
    throws ConstraintError
  {
    this.path = Constraints.constrainNotNull(path, "Path");
    this.names = new ArrayList<String>();
  }

  @Override public boolean hasMoreElements()
  {
    return this.index < this.path.length();
  }

  @Override public PathVirtual nextElement()
  {
    try {
      if (this.index == -1) {
        return PathVirtual.ROOT;
      }

      this.names.add(this.path.getUnsafe(this.index));
      return PathVirtual.ofNames(this.names);
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } finally {
      ++this.index;
    }
  }
}
