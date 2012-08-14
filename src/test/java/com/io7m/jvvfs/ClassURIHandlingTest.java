package com.io7m.jvvfs;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.FilesystemError.Code;

public class ClassURIHandlingTest
{
  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNullURI()
      throws FilesystemError,
        ConstraintError
  {
    final String path = "/x/y/z/C.class";
    ClassURIHandling.getClassContainerPath(null, path);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNullPath()
      throws MalformedURLException,
        FilesystemError,
        ConstraintError
  {
    final URL url = new URL("file:/a/b/c/x/y/z/C.class");
    ClassURIHandling.getClassContainerPath(url, null);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNullNull()
      throws FilesystemError,
        ConstraintError
  {
    ClassURIHandling.getClassContainerPath(null, null);
  }

  @SuppressWarnings("static-method") @Test(expected = FilesystemError.class) public
    void
    testNotFileNotJar()
      throws MalformedURLException,
        FilesystemError,
        ConstraintError
  {
    try {
      final URL url = new URL("http://example.org/z/C.class");
      final String path = "/x/y/z/C.class";
      ClassURIHandling.getClassContainerPath(url, path);
    } catch (final FilesystemError e) {
      Assert.assertEquals(Code.FS_ERROR_UNHANDLED_TYPE, e.code);
      throw e;
    }
  }

  @SuppressWarnings("static-method") @Test public void testFileNormal()
    throws MalformedURLException,
      FilesystemError,
      ConstraintError
  {
    final URL url = new URL("file:/a/b/c/x/y/z/C.class");
    final String path = "/x/y/z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);
    Assert.assertEquals("/a/b/c", r);
  }

  @SuppressWarnings("static-method") @Test public void testFileSpaces()
    throws MalformedURLException,
      FilesystemError,
      ConstraintError
  {
    final URL url = new URL("file:/a a/b b/c c/x x/y y/z z/C.class");
    final String path = "/x x/y y/z z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);
    Assert.assertEquals("/a a/b b/c c", r);
  }

  @SuppressWarnings("static-method") @Test public
    void
    testFileSpacesEncoded()
      throws MalformedURLException,
        FilesystemError,
        ConstraintError
  {
    final URL url =
      new URL("file:/a%20a/b%20b/c%20c/x%20x/y%20y/z%20z/C.class");
    final String path = "/x x/y y/z z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);
    Assert.assertEquals("/a a/b b/c c", r);
  }

  @SuppressWarnings("static-method") @Test public void testJarNormal()
    throws MalformedURLException,
      FilesystemError,
      ConstraintError
  {
    final URL url = new URL("jar:file:/a/b/c/j.jar!/x/y/z/C.class");
    final String path = "/x/y/z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);
    Assert.assertEquals("/a/b/c/j.jar", r);
  }

  @SuppressWarnings("static-method") @Test public void testJarSpaces()
    throws MalformedURLException,
      FilesystemError,
      ConstraintError
  {
    final URL url =
      new URL("jar:file:/a a/b b/c c/j.jar!/x x/y y/z z/C.class");
    final String path = "/x x/y y/z z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);
    Assert.assertEquals("/a a/b b/c c/j.jar", r);
  }

  @SuppressWarnings("static-method") @Test public void testJarSpacesEncoded()
    throws MalformedURLException,
      FilesystemError,
      ConstraintError
  {
    final URL url =
      new URL("jar:file:/a%20a/b%20b/c%20c/j.jar!/x%20x/y%20y/z%20z/C.class");
    final String path = "/x x/y y/z z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);
    Assert.assertEquals("/a a/b b/c c/j.jar", r);
  }
}
