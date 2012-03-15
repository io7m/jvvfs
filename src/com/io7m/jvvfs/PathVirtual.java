package com.io7m.jvvfs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

public final class PathVirtual implements Comparable<PathVirtual>
{
  public static boolean isSafe(
    final @Nonnull String path)
    throws ConstraintError
  {
    Constraints.constrainNotNull(path, "path");
    final boolean no_dots = path.contains("..") == false;
    final boolean no_backslash = path.contains("\\") == false;
    final boolean is_absolute = path.startsWith("/");
    return no_dots && no_backslash && is_absolute;
  }

  private static @Nonnull String scrubPath(
    final @Nonnull String path)
  {
    final String result =
      path.replaceAll("/+", "/").replaceAll("/+$", "").replaceAll("^/", "");
    if (result.equals("")) {
      return "/";
    }
    return result;
  }

  private final @Nonnull ArrayList<String> components;

  private final @Nonnull String            image;

  public PathVirtual(
    final @Nonnull String path)
    throws ConstraintError
  {
    Constraints.constrainArbitrary(PathVirtual.isSafe(path), "path is safe");
    final String scrubbed = PathVirtual.scrubPath(path);

    this.components = new ArrayList<String>();
    if (scrubbed.equals("/") == false) {
      final List<String> parts = Arrays.asList(scrubbed.split("/"));
      for (final String k : parts) {
        this.components.add(k);
      }
    }

    if (this.isRoot()) {
      this.image = "/";
      return;
    }

    final StringBuilder builder = new StringBuilder();
    for (final String component : this.components) {
      builder.append("/");
      builder.append(component);
    }
    this.image = builder.toString();
  }

  @Override public int compareTo(
    final PathVirtual other)
  {
    return this.toString().compareTo(other.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    return this.toString().equals(other.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.toString().hashCode();
    return result;
  }

  public @Nonnull String inRealNotation()
  {
    final String trimmed = this.toString().replaceAll("^/+", "");
    return trimmed.replace('/', File.separatorChar);
  }

  public boolean isParentOf(
    final @Nonnull PathVirtual other)
    throws ConstraintError
  {
    Constraints.constrainNotNull(other, "other path");

    if (this.isRoot()) {
      return true;
    }

    final String this_image = this.toString();
    final String other_image = other.toString();

    if (other_image.startsWith(this_image)) {
      if (other_image.equals(this_image)) {
        return false;
      }
      return true;
    }

    return false;
  }

  public boolean isRoot()
  {
    return this.components.size() == 0;
  }

  public @Nonnull PathVirtual parent()
    throws ConstraintError
  {
    if (this.isRoot()) {
      return new PathVirtual("/");
    }

    final int size = this.components.size() - 1;
    if (size > 0) {
      final StringBuilder builder = new StringBuilder();
      for (int index = 0; index < size; ++index) {
        builder.append("/");
        builder.append(this.components.get(index));
      }
      return new PathVirtual(builder.toString());
    }
    return new PathVirtual("/");
  }

  public @Nonnull List<String> pathComponents()
  {
    return Collections.unmodifiableList(this.components);
  }

  public PathVirtual subtract(
    final PathVirtual other)
    throws ConstraintError
  {
    if (other.isRoot()) {
      return this;
    }
    if (other.isParentOf(this)) {
      return new PathVirtual(this.toString().substring(
        other.toString().length()));
    }
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override public String toString()
  {
    return this.image;
  }
}
