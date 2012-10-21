/*
 * Copyright © 2012 http://io7m.com
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

import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;

/**
 * Trivial class for iterating over elements of a {@link PathVirtual}.
 * 
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 */

@NotThreadSafe public final class PathVirtualEnum implements
  Enumeration<PathVirtual>
{
  private final @Nonnull PathVirtual path;
  private int                        index   = -1;
  private final StringBuilder        builder = new StringBuilder();

  public PathVirtualEnum(
    final @Nonnull PathVirtual path)
    throws ConstraintError
  {
    this.path = Constraints.constrainNotNull(path, "path");
  }

  @Override public boolean hasMoreElements()
  {
    return this.index < this.path.pathComponents().size();
  }

  @Override public PathVirtual nextElement()
  {
    try {
      this.builder.append("/");

      if (this.index >= 0) {
        this.builder.append(this.path.pathComponents().get(this.index));
      }

      ++this.index;
      return new PathVirtual(this.builder.toString());
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException();
    }
  }
}
