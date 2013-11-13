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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.TreeView.Code;
import com.io7m.jvvfs.TreeView.Result;
import com.io7m.jvvfs.TreeView.TVDirectory;
import com.io7m.jvvfs.TreeView.TVFile;
import com.io7m.jvvfs.TreeView.TVNode;

public final class TreeViewTest
{
  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testAddExists_0()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    root.addFile("file.txt", null);
    root.addFile("file.txt", null);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testAddExists_1()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    root.addFile("file.txt", null);
    root.addDirectory("file.txt", null);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testAddExists_2()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    root.addDirectory("file.txt", null);
    root.addDirectory("file.txt", null);
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testAddExists_3()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    root.addDirectory("file.txt", null);
    root.addFile("file.txt", null);
  }

  @SuppressWarnings("static-method") @Test public void testAddGet_0()
    throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    root.addFile("file.txt", null);

    final Result<Integer> got = tv.get(PathVirtual.ofString("/file.txt"));
    final TVNode<Integer> got_node = got.getNode();
    Assert.assertTrue(got_node instanceof TVFile);
    Assert.assertEquals("file.txt", got_node.getName());
    Assert.assertNull(got_node.getData());
    Assert.assertEquals(root, got_node.getParent());
  }

  @SuppressWarnings("static-method") @Test public void testAddGet_1()
    throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    root.addDirectory("directory", null);

    final Result<Integer> got = tv.get(PathVirtual.ofString("/directory"));
    final TVNode<Integer> got_node = got.getNode();
    Assert.assertTrue(got_node instanceof TVDirectory);
    Assert.assertEquals("directory", got_node.getName());
    Assert.assertNull(got_node.getData());
    Assert.assertEquals(root, got_node.getParent());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testAddGetNonexistent()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    root.addDirectory("directory", null);

    final Result<Integer> got = tv.get(PathVirtual.ofString("/directory/x"));
    Assert.assertEquals(Code.R_NONEXISTENT, got.getCode());
    Assert.assertNull(got.getNode());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testAddGetNotADirectory()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    root.addFile("file.txt", null);

    final Result<Integer> got = tv.get(PathVirtual.ofString("/file.txt/x"));
    Assert.assertEquals(Code.R_NOT_A_DIRECTORY, got.getCode());
    Assert.assertNull(got.getNode());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testCreateDirectories()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final Result<Integer> r =
      tv.createDirectories(PathVirtual.ofString("/a/b/c"));
    Assert.assertEquals(Code.R_OK, r.getCode());
    Assert.assertTrue(r.getNode() instanceof TVDirectory);
    Assert.assertEquals("c", r.getNode().getName());
    Assert.assertEquals("b", r.getNode().getParent().getName());
    Assert.assertEquals("a", r.getNode().getParent().getParent().getName());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testCreateDirectoriesShow()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);

    tv.createDirectories(PathVirtual.ofString("/a/b/c"));
    tv.createDirectories(PathVirtual.ofString("/a/b/d"));
    tv.createDirectories(PathVirtual.ofString("/a/b/e"));
    tv.createDirectories(PathVirtual.ofString("/a/b/f"));
    tv.createDirectories(PathVirtual.ofString("/a/c/a"));
    tv.createDirectories(PathVirtual.ofString("/a/c/b"));
    tv.createDirectories(PathVirtual.ofString("/a/c/c"));

    System.out.println(tv.show());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testCreateDirectoriesTwice()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);

    {
      final Result<Integer> r =
        tv.createDirectories(PathVirtual.ofString("/a/b/c"));
      Assert.assertEquals(Code.R_OK, r.getCode());
      Assert.assertTrue(r.getNode() instanceof TVDirectory);
      Assert.assertEquals("c", r.getNode().getName());
      Assert.assertEquals("b", r.getNode().getParent().getName());
      Assert.assertEquals("a", r.getNode().getParent().getParent().getName());
    }

    {
      final Result<Integer> r =
        tv.createDirectories(PathVirtual.ofString("/a/b/c"));
      Assert.assertEquals(Code.R_OK, r.getCode());
      Assert.assertTrue(r.getNode() instanceof TVDirectory);
      Assert.assertEquals("c", r.getNode().getName());
      Assert.assertEquals("b", r.getNode().getParent().getName());
      Assert.assertEquals("a", r.getNode().getParent().getParent().getName());
    }

    System.out.println(tv.show());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testCreateDirectoriesNotADirectory()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);

    {
      final Result<Integer> r =
        tv.createDirectories(PathVirtual.ofString("/a"));
      Assert.assertEquals(Code.R_OK, r.getCode());
      final TVDirectory<Integer> d = (TVDirectory<Integer>) r.getNode();
      d.addFile("b", null);
    }

    {
      final Result<Integer> r =
        tv.createDirectories(PathVirtual.ofString("/a/b/c"));
      Assert.assertEquals(Code.R_NOT_A_DIRECTORY, r.getCode());
    }
  }

  @SuppressWarnings("static-method") @Test public void testEmpty()
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    Assert.assertNotNull(root);
    Assert.assertNull(root.getName());
    Assert.assertNull(root.getParent());
  }

  @SuppressWarnings("static-method") @Test(expected = ConstraintError.class) public
    void
    testGetNull()
      throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    tv.get(null);
  }

  @SuppressWarnings("static-method") @Test public void testGetRoot()
    throws ConstraintError
  {
    final TreeView<Integer> tv = TreeView.make(null);
    final TVDirectory<Integer> root = tv.getRoot();
    Assert.assertNotNull(root);
    Assert.assertNull(root.getName());
    Assert.assertNull(root.getParent());

    final Result<Integer> got = tv.get(PathVirtual.ROOT);
    Assert.assertEquals(root, got.getNode());
  }
}
