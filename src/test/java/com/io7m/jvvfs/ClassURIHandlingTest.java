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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.FilesystemError.Code;

public class ClassURIHandlingTest
{
  private static enum Platform
  {
    PLATFORM_POSIX,
    PLATFORM_WINDOWS
  }

  /**
   * XXX: Portability: There are probably systems that aren't POSIX and aren't
   * Windows.
   */

  private static Platform currentPlatform()
  {
    final String os = System.getProperty("os.name");
    if ((os.indexOf("win") >= 0) || (os.indexOf("Win") >= 0)) {
      return Platform.PLATFORM_WINDOWS;
    }
    return Platform.PLATFORM_POSIX;
  }

  @SuppressWarnings("static-method") @Test public void testFileNormal()
    throws MalformedURLException,
      FilesystemError,
      ConstraintError
  {
    final URL url = new URL("file:/a/b/c/x/y/z/C.class");
    final String path = "/x/y/z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);

    switch (ClassURIHandlingTest.currentPlatform()) {
      case PLATFORM_POSIX:
      {
        Assert.assertEquals("/a/b/c", r);
        return;
      }
      case PLATFORM_WINDOWS:
      {
        Assert.assertTrue(r.endsWith(":\\a\\b\\c"));
        return;
      }
    }
  }

  @SuppressWarnings("static-method") @Test public void testFileSpaces()
    throws MalformedURLException,
      FilesystemError,
      ConstraintError
  {
    final URL url = new URL("file:/a a/b b/c c/x x/y y/z z/C.class");
    final String path = "/x x/y y/z z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);

    switch (ClassURIHandlingTest.currentPlatform()) {
      case PLATFORM_POSIX:
      {
        Assert.assertEquals("/a a/b b/c c", r);
        return;
      }
      case PLATFORM_WINDOWS:
      {
        Assert.assertTrue(r.endsWith(":\\a a\\b b\\c c"));
        return;
      }
    }
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

    switch (ClassURIHandlingTest.currentPlatform()) {
      case PLATFORM_POSIX:
      {
        Assert.assertEquals("/a a/b b/c c", r);
        return;
      }
      case PLATFORM_WINDOWS:
      {
        Assert.assertTrue(r.endsWith(":\\a a\\b b\\c c"));
        return;
      }
    }
  }

  @SuppressWarnings("static-method") @Test public void testJarNormal()
    throws MalformedURLException,
      FilesystemError,
      ConstraintError
  {
    final URL url = new URL("jar:file:/a/b/c/j.jar!/x/y/z/C.class");
    final String path = "/x/y/z/C.class";
    final String r = ClassURIHandling.getClassContainerPath(url, path);

    switch (ClassURIHandlingTest.currentPlatform()) {
      case PLATFORM_POSIX:
      {
        Assert.assertEquals("/a/b/c/j.jar", r);
        return;
      }
      case PLATFORM_WINDOWS:
      {
        Assert.assertTrue(r.endsWith(":\\a\\b\\c\\j.jar"));
        return;
      }
    }
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

    switch (ClassURIHandlingTest.currentPlatform()) {
      case PLATFORM_POSIX:
      {
        Assert.assertEquals("/a a/b b/c c/j.jar", r);
        return;
      }
      case PLATFORM_WINDOWS:
      {
        Assert.assertTrue(r.endsWith(":\\a a\\b b\\c c\\j.jar"));
        return;
      }
    }
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

    switch (ClassURIHandlingTest.currentPlatform()) {
      case PLATFORM_POSIX:
      {
        Assert.assertEquals("/a a/b b/c c/j.jar", r);
        return;
      }
      case PLATFORM_WINDOWS:
      {
        Assert.assertTrue(r.endsWith(":\\a a\\b b\\c c\\j.jar"));
        return;
      }
    }
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

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testNullNull()
      throws FilesystemError,
        ConstraintError
  {
    ClassURIHandling.getClassContainerPath(null, null);
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
    testNullURI()
      throws FilesystemError,
        ConstraintError
  {
    final String path = "/x/y/z/C.class";
    ClassURIHandling.getClassContainerPath(null, path);
  }
}
