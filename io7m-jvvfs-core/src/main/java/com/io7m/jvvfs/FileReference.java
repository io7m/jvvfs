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

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * <p>
 * A reference to a file inside an archive.
 * </p>
 * <p>
 * The type parameter <code>T</code> is used as a phantom type to distinguish
 * between references from different archive implementations at the type
 * level.
 * </p>
 * <p>
 * Values of this type cannot be accessed safely from multiple threads without
 * explicit synchronization.
 * </p>
 */

abstract class FileReference<T extends ArchiveKind>
{
  static enum Type
  {
    TYPE_DIRECTORY,
    TYPE_FILE
  }

  private final @Nullable Archive<T> archive;
  private final PathVirtual          path;
  private final FileReference.Type   type;

  FileReference(
    final @Nullable Archive<T> in_archive,
    final PathVirtual in_path,
    final FileReference.Type in_type)
  {
    this.archive = in_archive;
    this.path = NullCheck.notNull(in_path, "Path");
    this.type = NullCheck.notNull(in_type, "type");
  }

  final @Nullable Archive<T> getArchive()
  {
    return this.archive;
  }

  final PathVirtual getPath()
  {
    return this.path;
  }

  final FileReference.Type getType()
  {
    return this.type;
  }

  @Override public final String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[FileReference ");
    builder.append(this.type);
    builder.append(" ");
    builder.append(this.path);
    builder.append("]");
    return NullCheck.notNull(builder.toString());
  }
}
