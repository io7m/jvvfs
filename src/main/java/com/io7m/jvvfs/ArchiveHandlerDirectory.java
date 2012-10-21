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
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;

/**
 * Handler for loading archives of type {@link ArchiveDirectory}.
 * 
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 */

@NotThreadSafe public final class ArchiveHandlerDirectory implements
  ArchiveHandler
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
