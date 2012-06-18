package com.io7m.jvvfs;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;

/**
 * The interface exposed by the "handler" for each archive type.
 */

public interface ArchiveHandler
{
  boolean canHandle(
    final @Nonnull PathReal name);

  @Nonnull Archive load(
    final @Nonnull PathReal name,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError;

  @Nonnull Archive loadWithParent(
    final @Nonnull Archive archive,
    final @Nonnull PathReal name,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError;
}
