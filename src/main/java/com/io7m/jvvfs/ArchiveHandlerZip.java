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
