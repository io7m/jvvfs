package com.io7m.jvvfs;

import java.io.File;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

public final class PathReal
{
  public final @Nonnull String value;

  public PathReal(
    final @Nonnull String path)
    throws ConstraintError
  {
    this.value = Constraints.constrainNotNull(path, "path");
  }

  public PathReal concatenate(
    final @Nonnull PathVirtual path)
    throws ConstraintError
  {
    Constraints.constrainNotNull(path, "path");
    return new PathReal(this.value
      + File.separatorChar
      + path.inRealNotation());
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
    final PathReal other = (PathReal) obj;
    return this.value.equals(other.value);
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
    result = (prime * result) + (this.value.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override public String toString()
  {
    return this.value;
  }
}
