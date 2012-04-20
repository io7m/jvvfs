package com.io7m.jvvfs;

import java.util.Enumeration;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

/**
 * Trivial class for iterating over elements of a {@link PathVirtual}.
 */

public final class PathVirtualEnum implements Enumeration<PathVirtual>
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
      /* UNREACHABLE */
      throw new AssertionError("bug: unreachable code");
    }
  }
}
