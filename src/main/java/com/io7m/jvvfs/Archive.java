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

import java.io.InputStream;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;

/**
 * Type representing a mounted archive.
 */

public interface Archive
{
  /**
   * Close the archive.
   * 
   * @throws FilesystemError
   */

  void close()
    throws FilesystemError;

  /**
   * Return the size in bytes of the file specified by <code>path</code>. In
   * the case of compressed filesystems/archives, the size represents the
   * uncompressed size of the specified file.
   * 
   * @param path
   *          The file.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   */

  long fileSize(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Return the path at which the archive is mounted.
   */

  @Nonnull PathVirtual getMountPath();

  /**
   * Return the archive that provides the directory at which the archive is
   * mounted.
   */

  @CheckForNull Archive getParent();

  /**
   * Return the real path of the archive.
   */

  @Nonnull PathReal getRealPath();

  /**
   * List the directory at <code>path</code>.
   * 
   * @param path
   *          The path.
   * @param items
   *          The set containing the contents of the directory.
   */

  void listDirectory(
    final @Nonnull PathVirtual path,
    final @Nonnull TreeSet<String> items)
    throws ConstraintError,
      FilesystemError;

  /**
   * Look up the given <code>path</code> in the archive.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  @Nonnull FileReference lookup(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Return the time that <code>path</code> was last modified.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  long modificationTime(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Open the file referenced by <code>path</code>. The returned
   * <code>InputStream</code> is as "raw" as possible - it is not buffered and
   * should be wrapped in a <code>BufferedInputStream</code> if required.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  @Nonnull InputStream openFile(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;
}
