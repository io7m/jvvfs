package com.io7m.jvvfs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.FilesystemError.Code;

final class ClassURIHandling
{
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
   * The function raises {@link FilesystemError} for URL protocols that cannot
   * be handled.
   * 
   * @throws ConstraintError
   *           Iff <code>url == null || file == null</code>.
   * @throws FilesystemError
   */

  static String getClassContainerPath(
    final @Nonnull URL url,
    final @Nonnull String file)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(url, "URL");
    Constraints.constrainNotNull(file, "File");

    final String proto = url.getProtocol();
    if ((proto.equals("file") == false) && (proto.equals("jar") == false)) {
      throw new FilesystemError(
        Code.FS_ERROR_UNHANDLED_TYPE,
        "Cannot handle non-file or non-jar URLs");
    }

    if (proto.equals("file")) {
      return ClassURIHandling.getClassContainerPathForFile(url, file);
    }

    return ClassURIHandling.getClassContainerPathForJar(url);
  }

  private static String getClassContainerPathForFile(
    final @Nonnull URL url,
    final @Nonnull String file)
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
    return path.substring(0, path.length() - file.length());
  }

  private static String getClassContainerPathForJar(
    final @Nonnull URL url)
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

      return f.getAbsolutePath();
    } catch (final MalformedURLException e) {
      throw new FilesystemError(
        Code.FS_ERROR_CONSTRAINT_ERROR,
        "invalid intermediate URL: " + e.getMessage());
    }
  }
}
