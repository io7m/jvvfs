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

import java.io.InputStream;
import java.util.Calendar;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;

final class ArchiveIdentity extends Archive<ArchiveKind>
{
  @Override void close()
    throws FilesystemError
  {
    throw new UnreachableCodeException();
  }

  @Override long getFileSizeActual(
    final @Nonnull FileReference<ArchiveKind> r)
    throws FilesystemError,
      ConstraintError
  {
    throw new UnreachableCodeException();
  }

  @Override @Nonnull Calendar getModificationTimeActual(
    final FileReference<ArchiveKind> r)
  {
    throw new UnreachableCodeException();
  }

  @Override @Nonnull PathVirtual getMountPath()
  {
    throw new UnreachableCodeException();
  }

  @Override @Nonnull PathReal getRealPath()
  {
    throw new UnreachableCodeException();
  }

  @Override @Nonnull Set<String> listDirectory(
    final PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    throw new UnreachableCodeException();
  }

  @Override @CheckForNull FileReference<ArchiveKind> lookupActual(
    final @Nonnull PathVirtual path)
    throws ConstraintError
  {
    throw new UnreachableCodeException();
  }

  @Override @Nonnull InputStream openFileActual(
    final FileReference<ArchiveKind> r)
    throws FilesystemError,
      ConstraintError
  {
    throw new UnreachableCodeException();
  }
}
