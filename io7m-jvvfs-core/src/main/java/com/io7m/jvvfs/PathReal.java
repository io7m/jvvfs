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

import java.io.File;

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * <p>
 * A real path represents a path in the real (not virtual) filesystem.
 * </p>
 * <p>
 * The concrete syntax of real paths is operating-system specific.
 * </p>
 */

public final class PathReal
{
  private final File actual;

  /**
   * Construct a new path from the given file.
   * 
   * @param in_actual
   *          The file
   */

  public PathReal(
    final File in_actual)
  {
    this.actual = NullCheck.notNull(in_actual, "Path");
  }

  /**
   * Construct a new path from the given string.
   * 
   * @param in_actual
   *          The string
   */

  public PathReal(
    final String in_actual)
  {
    this(new File(NullCheck.notNull(in_actual, "Path")));
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
    final PathReal other = (PathReal) obj;
    return this.actual.equals(other.actual);
  }

  @Override public int hashCode()
  {
    return this.actual.hashCode();
  }

  /**
   * @return The current path as a {@link File}.
   */

  public File toFile()
  {
    return this.actual;
  }

  @Override public String toString()
  {
    return NullCheck.notNull(this.actual.toString());
  }
}
