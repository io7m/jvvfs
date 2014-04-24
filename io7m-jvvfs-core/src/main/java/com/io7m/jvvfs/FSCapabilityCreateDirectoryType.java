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
 * capability to create directories in the (virtual) filesystem.
 * </p>
 */

public interface FSCapabilityCreateDirectoryType
{
  /**
   * <p>
   * Create a directory in the virtual filesystem at <code>path</code>.
   * </p>
   * 
   * @param path
   *          The path.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>An ancestor of <code>path</code> is not a directory.</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  void createDirectory(
    final PathVirtual path)
    throws FilesystemError;
}
