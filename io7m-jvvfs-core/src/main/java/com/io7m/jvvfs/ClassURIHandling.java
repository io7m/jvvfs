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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FilesystemError.Code;

final class ClassURIHandling
{
  private ClassURIHandling()
  {
    throw new UnreachableCodeException();
  }

  /**
   * For a given URL returned by a classloader <code>url</code>, and the given
   * <code>file</code>, return the filesystem path that contains
   * <code>file</code> directly.
   * 
   * For a <code>jar</code> protocol URL, this will be the path of the jar
   * file itself. For a <code>file</code> URL, this will be the directory that
   * contains <code>file</code, not including any subdirectories in
   * <code>file</code>'s name.
   * 
   * As an example:
   * 
   * <code>("jar:file:/x/y/z/j.jar!/a/b/c/Class.class", "/a/b/c/Class.class") => "/x/y/z/j.jar")</code>
   * <code>("file:/x/y/z/a/b/c/Class.class", "/a/b/c/Class.class") => "/x/y/z")</code>
   * 
   * Note that the result will actually be in an OS-specific format.
   * 
   * The function raises {@link FilesystemError} for URL protocols that cannot
   * be handled.
   * 
   * @throws ConstraintError
   *           Iff <code>url == null || file == null</code>.
   * @throws FilesystemError
   */

  static String getClassContainerPath(
    final URL url,
    final String file)
    throws FilesystemError
  {
    NullCheck.notNull(url, "URL");
    NullCheck.notNull(file, "File");

    final String proto = url.getProtocol();
    if (("file".equals(proto) == false) && ("jar".equals(proto) == false)) {
      throw new FilesystemError(
        Code.FS_ERROR_ARCHIVE_TYPE_UNSUPPORTED,
        "Cannot handle non-file or non-jar URLs");
    }

    if ("file".equals(proto)) {
      return ClassURIHandling.getClassContainerPathForFile(url, file);
    }

    return ClassURIHandling.getClassContainerPathForJar(url);
  }

  private static String getClassContainerPathForFile(
    final URL url,
    final String file)
  {
    /**
     * URL is of the form "file:/path/to/file.class"
     */

    File f = null;
    try {
      f = new File(url.toURI());
    } catch (final URISyntaxException e) {
      f = new File(url.getPath());
    }

    final String path = f.getAbsolutePath();
    return NullCheck
      .notNull(path.substring(0, path.length() - file.length()));
  }

  private static String getClassContainerPathForJar(
    final URL url)
    throws FilesystemError
  {
    try {
      /**
       * URL is of the form "jar:file:/x/y/z.jar!/path/to/file.class"
       */

      final String path = url.getPath();
      final String[] segments = path.split("!");
      final URL base_url = new URL(segments[0]);

      File f = null;
      try {
        f = new File(base_url.toURI());
      } catch (final URISyntaxException e) {
        f = new File(base_url.getPath());
      }

      return NullCheck.notNull(f.getAbsolutePath());
    } catch (final MalformedURLException e) {
      throw new FilesystemError(
        Code.FS_ERROR_CONSTRAINT_ERROR,
        "invalid intermediate URL: " + e.getMessage());
    }
  }
}
