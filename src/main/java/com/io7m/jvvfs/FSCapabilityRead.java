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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;

/**
 * <p>
 * The interface exposed by filesystem implementations that have the
 * capability to read from mounted archives.
 * </p>
 */

public interface FSCapabilityRead
{
  /**
   * <p>
   * Returns <code>true</code> iff <code>path</code> exists.
   * </p>
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  public boolean exists(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * <p>
   * Retrieve the size of the file at <code>path</code>.
   * </p>
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a file</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  public long getFileSize(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError;

  /**
   * <p>
   * Retrieve the modification time of the file or directory at
   * <code>path</code>.
   * </p>
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>r</code>.</li>
   *           <li>An I/O error occurs.</li>
   *           </ul>
   */

  public @Nonnull Calendar getModificationTime(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError;

  /**
   * <p>
   * Returns <code>true</code> iff <code>path</code> exists and is a
   * directory.
   * </p>
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  public boolean isDirectory(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * <p>
   * Returns <code>true</code> iff <code>path</code> exists and is a file.
   * </p>
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  public boolean isFile(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * <p>
   * Open the file at <code>path</code>.
   * </p>
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a file</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  public @Nonnull InputStream openFile(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError;
}
