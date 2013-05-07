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

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

/**
 * Type representing "real" paths. That is, paths to files in the real OS
 * filesystem.
 * 
 * @see PathVirtual
 */

@Immutable public final class PathReal
{
  public final @Nonnull String value;

  /**
   * Construct a path from the given {@link String}.
   * 
   * @throws ConstraintError
   *           Iff <code>path == null</code>
   */

  public PathReal(
    final @Nonnull String path)
    throws ConstraintError
  {
    this.value = Constraints.constrainNotNull(path, "path");
  }

  /**
   * Construct a path from the given {@link File}.
   * 
   * @throws ConstraintError
   *           Iff <code>path == null</code>
   * @since 2.5.0
   */

  public PathReal(
    final @Nonnull File path)
    throws ConstraintError
  {
    this.value = Constraints.constrainNotNull(path, "path").toString();
  }

  /**
   * Concatenate a virtual path onto this real path.
   * 
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   */

  public PathReal concatenate(
    final @Nonnull PathVirtual path)
    throws ConstraintError
  {
    Constraints.constrainNotNull(path, "path");
    return new PathReal(this.value
      + File.separatorChar
      + path.inRealNotation());
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
    final PathReal other = (PathReal) obj;
    return this.value.equals(other.value);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (this.value.hashCode());
    return result;
  }

  @Override public String toString()
  {
    return this.value;
  }
}
