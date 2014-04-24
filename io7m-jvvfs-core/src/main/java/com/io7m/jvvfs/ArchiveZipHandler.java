/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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
import java.io.IOException;

import com.io7m.jlog.LogUsableType;

/**
 * The handler responsible for loading zip/jar archives.
 */

final class ArchiveZipHandler extends ArchiveHandler<ArchiveZipKind>
{
  ArchiveZipHandler()
  {
    super();
  }

  @Override boolean canHandle(
    final PathReal name)
  {
    final String ns = name.toString();

    if (ns.endsWith(".zip")) {
      return new File(ns).isFile();
    }
    if (ns.endsWith(".jar")) {
      return new File(ns).isFile();
    }

    return false;
  }

  @Override Archive<ArchiveZipKind> load(
    final LogUsableType log,
    final PathReal name,
    final PathVirtual mount)
    throws FilesystemError
  {
    try {
      return new ArchiveZip(log, name, mount);
    } catch (final IOException e) {
      throw FilesystemError.ioError(e);
    }
  }
}
