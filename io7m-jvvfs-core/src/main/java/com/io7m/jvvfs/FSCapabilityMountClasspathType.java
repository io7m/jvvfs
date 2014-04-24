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

/**
 * <p>
 * The interface exposed by filesystem implementations that have the
 * capability to load archives from the classpath.
 * </p>
 */

public interface FSCapabilityMountClasspathType extends
  FSCapabilityCreateDirectoryType
{
  /**
   * <p>
   * Mount the archive containing class <code>c</code> at <code>mount</code>.
   * The path specified by <code>mount</code> is required to refer to an
   * existing directory.
   * </p>
   * <p>
   * If the directory at <code>mount</code> exists but was not explicitly
   * created with {@link #createDirectory(PathVirtual)}, then the
   * <code>mountArchive</code> function first calls
   * {@link #createDirectory(PathVirtual)} to mark <code>mount</code> as
   * explicitly created, and then attempts to mount the archive as normal.
   * </p>
   * 
   * @param c
   *          A class appearing in one of the archives on the classpath.
   * @param mount
   *          The mount point for the archive.
   * @throws FilesystemError
   *           If a filesystem error occurs.
   */

  void mountClasspathArchive(
    final Class<?> c,
    final PathVirtual mount)
    throws FilesystemError;
}
