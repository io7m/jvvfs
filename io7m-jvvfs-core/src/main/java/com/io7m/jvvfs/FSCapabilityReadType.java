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

import java.io.InputStream;
import java.util.Calendar;
import java.util.SortedSet;

/**
 * <p>
 * The interface exposed by filesystem implementations that have the
 * capability to read from mounted archives.
 * </p>
 */

public interface FSCapabilityReadType
{
  /**
   * @return <code>true</code> iff <code>path</code> exists.
   * @param path
   *          The path.
   * @throws FilesystemError
   *           If a filesystem error occurs.
   */

  boolean exists(
    final PathVirtual path)
    throws FilesystemError;

  /**
   * @return The size of the file at <code>path</code>.
   * @param path
   *          The path.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a file</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  long getFileSize(
    final PathVirtual path)
    throws FilesystemError;

  /**
   * @return The modification time of the file or directory at
   *         <code>path</code>.
   * @param path
   *          The path.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>r</code>.</li>
   *           <li>An I/O error occurs.</li>
   *           </ul>
   */

  Calendar getModificationTime(
    final PathVirtual path)
    throws FilesystemError;

  /**
   * @return <code>true</code> iff <code>path</code> exists and is a
   *         directory.
   * @param path
   *          The path.
   * @throws FilesystemError
   *           If a filesystem error occurs.
   */

  boolean isDirectory(
    final PathVirtual path)
    throws FilesystemError;

  /**
   * @return <code>true</code> iff <code>path</code> exists and is a file.
   * @param path
   *          The path.
   * @throws FilesystemError
   *           If a filesystem error occurs.
   */

  boolean isFile(
    final PathVirtual path)
    throws FilesystemError;

  /**
   * @return A list of the contents of the directory at <code>path</code>.
   * @param path
   *          The path.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a directory</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  SortedSet<String> listDirectory(
    final PathVirtual path)
    throws FilesystemError;

  /**
   * <p>
   * Open the file at <code>path</code>.
   * </p>
   * 
   * @return An input stream.
   * @param path
   *          The path.
   * @throws FilesystemError
   *           If:
   *           <ul>
   *           <li>No object exists at <code>path</code>.</li>
   *           <li>The object at <code>path</code> is not a file</li>
   *           <li>An I/O error occurs</li>
   *           </ul>
   */

  InputStream openFile(
    final PathVirtual path)
    throws FilesystemError;
}
