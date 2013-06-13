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

import javax.annotation.Nonnull;

import com.io7m.jaux.functional.Option;

/**
 * <p>
 * An archive is an object in the operating system filesystem that can be
 * "mounted" in the virtual filesystem.
 * </p>
 * <p>
 * An archive mounted at mount point <code>m</code> makes its contents
 * available at paths prefixed with <code>m</code>. As an example, if an
 * archive contains the file <code>/x/y/z/file.txt</code> and the archive is
 * mounted at <code>/usr</code>, then the file is accessible via
 * <code>/usr/x/y/z/file.txt</code> in the virtual filesystem. The
 * implementation is responsible for converting virtual paths to
 * archive-relative paths. That is, if a user tries to open
 * <code>/usr/x/y/z/file.txt</code> in the example above, the filesystem is
 * responsible for translating that to <code>/x/y/z/file.txt</code> before
 * passing the path to the archive interface.
 * </p>
 */

abstract class Archive
{
  /**
   * <p>
   * The path at which the archive is mounted.
   * </p>
   */

  abstract @Nonnull PathVirtual getMountPath();

  /**
   * <p>
   * Retrieve a reference to the object at the given path. This is a
   * "primitive" operation; all other functions should use <code>lookup</code>
   * internally in order to provide consistent semantics.
   * </p>
   */

  abstract @Nonnull Option<FileReference> lookup(
    final @Nonnull PathVirtual path)
    throws FilesystemError;
}
