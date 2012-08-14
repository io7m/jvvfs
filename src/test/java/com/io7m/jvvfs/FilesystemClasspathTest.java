package com.io7m.jvvfs;

import java.io.IOException;

import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;

/**
 * Specifically test the slightly unsafe
 * {@link Filesystem#mountUnsafeClasspathItem(Class, PathVirtual)} function.
 */

public class FilesystemClasspathTest
{
  // private static final String archive_dir = "test-archives";
  // private static Filesystem makeFS()
  // throws IOException,
  // ConstraintError,
  // FilesystemError
  // {
  // final Log log =
  // new Log(
  // PropertyUtils.loadFromFile("io7m-jvvfs.properties"),
  // "com.io7m.jvvfs",
  // "main");
  // return new Filesystem(log, new PathReal(
  // FilesystemClasspathTest.archive_dir));
  // }

  /**
   * Mount a file that exists inside a directory on the classpath.
   * 
   * @throws IOException
   * @throws FilesystemError
   * @throws ConstraintError
   */

  @Test public void testFilesystemClasspathMountFile()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    // TODO: Work out how to make this test work with maven builds.
    // final Filesystem fs = FilesystemClasspathTest.makeFS();
    // fs.mountUnsafeClasspathItem(this.getClass(), new PathVirtual("/"));
    //
    // final InputStream stream =
    // fs.openFile(new PathVirtual(
    // "/com/io7m/jvvfs/tests/FilesystemClasspathTest.class"));
    // assert stream != null;
  }

  /**
   * Mount a file that exists inside Example.jar on the classpath.
   * 
   * @throws IOException
   * @throws FilesystemError
   * @throws ConstraintError
   */

  @Test public void testFilesystemClasspathMountJar()
    throws IOException,
      FilesystemError,
      ConstraintError
  {
    // TODO: Work out how to make this test work with maven builds.
    // final Filesystem fs = FilesystemClasspathTest.makeFS();
    // fs.mountUnsafeClasspathItem(
    // com.io7m.jvvfs_examples.Example.class,
    // new PathVirtual("/"));
    //
    // final InputStream stream =
    // fs.openFile(new PathVirtual("/com/io7m/jvvfs_examples/Example.class"));
    // assert stream != null;
  }
}
