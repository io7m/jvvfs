/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;

/**
 * The handler responsible for loading 7z archives.
 */

final class Archive7zHandler extends ArchiveHandler<Archive7zKind>
{
  static {
    ArchiveHandlerRegistration.addHandler(
      Archive7zHandler.class.getCanonicalName(),
      new Callable<ArchiveHandler<Archive7zKind>>() {
        @Override public ArchiveHandler<Archive7zKind> call()
          throws Exception
        {
          return new Archive7zHandler();
        }
      });
  }

  Archive7zHandler()
  {
    // Nothing.
  }

  @Override boolean canHandle(
    final @Nonnull PathReal name)
  {
    final String ns = name.toString();

    if (ns.endsWith(".7z")) {
      return new File(ns).isFile();
    }

    return false;
  }

  @Override @Nonnull Archive<Archive7zKind> load(
    final @Nonnull Log log,
    final @Nonnull PathReal name,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    return new Archive7z(log, name, mount);
  }
}
