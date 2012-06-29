package com.io7m.jvvfs;

import java.io.File;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;

/**
 * Handler for loading archives of type {@link ArchiveDirectory}.
 */

public final class ArchiveHandlerDirectory implements ArchiveHandler
{
  private final @Nonnull Log log;

  public ArchiveHandlerDirectory(
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log = new Log(Constraints.constrainNotNull(log, "log"), "directory");
  }

  @Override public boolean canHandle(
    final @Nonnull PathReal name)
  {
    return new File(name.value).isDirectory();
  }

  @Override public Archive load(
    final @Nonnull PathReal name,
    final @Nonnull PathVirtual mount)
    throws ConstraintError
  {
    this.log.debug("load " + name);
    return new ArchiveDirectory(name, mount, this.log);
  }

  @Override public Archive loadWithParent(
    final @Nonnull Archive archive,
    final @Nonnull PathReal name,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    this.log.debug("load-with-parent " + name);
    return new ArchiveDirectory(archive, name, mount, this.log);
  }
}
