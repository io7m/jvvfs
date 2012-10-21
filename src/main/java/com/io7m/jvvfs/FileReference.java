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
