package com.io7m.jvvfs;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;

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
