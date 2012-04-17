package com.io7m.jvvfs;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

/**
 * A reference to a file inside an archive.
 */

public final class FileReference
{
  public static enum Type
  {
    TYPE_FILE,
    TYPE_DIRECTORY
  }

  public final @CheckForNull Archive       archive;
  public final @Nonnull PathVirtual        path;
  public final @Nonnull FileReference.Type type;

  public FileReference(
    final @CheckForNull Archive archive,
    final @Nonnull PathVirtual path,
    final @Nonnull FileReference.Type type)
    throws ConstraintError
  {
    this.archive = archive;
    this.path = Constraints.constrainNotNull(path, "path");
    this.type = Constraints.constrainNotNull(type, "type");
  }
}
