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

import com.io7m.jlog.LogUsableType;

/**
 * The interface exposed by archive handlers.
 */

abstract class ArchiveHandler<T extends ArchiveKind>
{
  /**
   * Return <code>true</code> iff the archive implementation can handle
   * archives of this type (based on the type guessed by examining
   * <code>name</code>).
   */

  abstract boolean canHandle(
    final PathReal name);

  /**
   * Load the archive at <code>name</code>, setting the mount path of the
   * archive to <code>mount</code>.
   */

  abstract Archive<T> load(
    final LogUsableType log,
    final PathReal name,
    final PathVirtual mount)
    throws FilesystemError;
}
