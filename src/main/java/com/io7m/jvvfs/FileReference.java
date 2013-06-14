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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

/**
 * <p>
 * A reference to a file inside an archive.
 * </p>
 * <p>
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 * </p>
 */

@NotThreadSafe final class FileReference
{
  static enum Type
  {
    TYPE_FILE,
    TYPE_DIRECTORY
  }

  final @CheckForNull Archive       archive;
  final @Nonnull PathVirtual        path;
  final @Nonnull FileReference.Type type;

  FileReference(
    final @CheckForNull Archive archive,
    final @Nonnull PathVirtual path,
    final @Nonnull FileReference.Type type)
    throws ConstraintError
  {
    this.archive = archive;
    this.path = Constraints.constrainNotNull(path, "path");
    this.type = Constraints.constrainNotNull(type, "type");
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[FileReference ");
    builder.append(this.type);
    builder.append(" ");
    builder.append(this.path);
    builder.append("]");
    return builder.toString();
  }
}
