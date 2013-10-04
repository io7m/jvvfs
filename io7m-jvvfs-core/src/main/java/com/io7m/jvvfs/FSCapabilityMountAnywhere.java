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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;

/**
 * <p>
 * The interface exposed by filesystem implementations that have the
 * capability to load archives from anywhere.
 * </p>
 */

public interface FSCapabilityMountAnywhere extends
  FSCapabilityCreateDirectory
{
  /**
   * <p>
   * Mount the archive <code>archive</code> at <code>mount</code>. The path
   * specified by <code>mount</code> is required to refer to an existing
   * directory.
   * </p>
   * <p>
   * If the directory at <code>mount</code> exists but was not explicitly
   * created with {@link #createDirectory(PathVirtual)}, then the
   * <code>mountArchive</code> function first calls
   * {@link #createDirectory(PathVirtual)} to mark <code>mount</code> as
   * explicitly created, and then attempts to mount the archive as normal.
   * </p>
   * 
   * @param archive
   *          The archive to mount.
   * @param mount
   *          The mount point for the archive.
   * @throws ConstraintError
   *           Iff <code>archive == null || mount == null</code>.
   * @throws FilesystemError
   */

  public void mountArchiveFromAnywhere(
    final @Nonnull File archive,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError;
}
