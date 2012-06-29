package com.io7m.jvvfs;

import java.io.File;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;

/**
 * Handler for loading archives of type {@link ArchiveZip}.
 */

public final class ArchiveHandlerZip implements ArchiveHandler
{
  private final @Nonnull Log log;

  public ArchiveHandlerZip(
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log = new Log(Constraints.constrainNotNull(log, "log"), "zip");
  }

  @Override public boolean canHandle(
    final @Nonnull PathReal name)
  {
    boolean ok = true;

    ok = ok && name.value.endsWith(".zip");
    ok = ok || name.value.endsWith(".jar");
    ok = ok && new File(name.value).isFile();

    return ok;
  }

  @Override public Archive load(
    final @Nonnull PathReal name,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    this.log.debug("load " + name);
    return new ArchiveZip(name, mount, this.log);
  }

  @Override public Archive loadWithParent(
    final @Nonnull Archive archive,
    final @Nonnull PathReal name,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    this.log.debug("load-with-parent " + name);
    return new ArchiveZip(archive, name, mount, this.log);
  }
}
