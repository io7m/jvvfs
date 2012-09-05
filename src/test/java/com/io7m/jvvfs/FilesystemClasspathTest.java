package com.io7m.jvvfs;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FilesystemError.Code;

/**
 * Specifically test the slightly unsafe
 * {@link Filesystem#mountUnsafeClasspathItem(Class, PathVirtual)} function.
 */

public class FilesystemClasspathTest
{
  private static Filesystem makeFS()
    throws IOException,
      ConstraintError
  {
    final Log log =
      new Log(
        PropertyUtils.loadFromFile("io7m-jvvfs.properties"),
        "com.io7m.jvvfs",
        "main");
    return new Filesystem(log);
  }

  /**
   * Mount a file that exists inside a directory on the classpath.
   * 
   * @throws IOException
   * @throws FilesystemError
   * @throws ConstraintError
   */

  @SuppressWarnings("static-method") @Test public
    void
    testFilesystemClasspathMountFile()
      throws IOException,
        FilesystemError,
        ConstraintError
  {
    final Filesystem fs = FilesystemClasspathTest.makeFS();
    fs.mountUnsafeClasspathItem(Filesystem.class, new PathVirtual("/"));

    final InputStream stream =
      fs.openFile(new PathVirtual("/com/io7m/jvvfs/Filesystem.class"));
    assert stream != null;
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testFilesystemClasspathMountRejected()
      throws IOException,
        ConstraintError,
        FilesystemError
  {
    try {
      final Filesystem fs = FilesystemClasspathTest.makeFS();
      fs.mount("nonexistent", "/");
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_NONEXISTENT, e.code);
      throw e;
    }
  }
}
