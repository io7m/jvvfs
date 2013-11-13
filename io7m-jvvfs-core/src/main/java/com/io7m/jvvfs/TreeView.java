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

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

final class TreeView<T>
{
  enum Code
  {
    R_OK,
    R_NONEXISTENT,
    R_NOT_A_DIRECTORY
  }

  @Immutable static final class Result<T>
  {
    public static @Nonnull <T> Result<T> nonexistent()
    {
      return new Result<T>(Code.R_NONEXISTENT, null);
    }

    public static @Nonnull <T> Result<T> notADirectory()
    {
      return new Result<T>(Code.R_NOT_A_DIRECTORY, null);
    }

    public static @Nonnull <T> Result<T> ok(
      final @Nonnull TVNode<T> node)
    {
      return new Result<T>(Code.R_OK, node);
    }

    private final @Nonnull Code           code;

    private final @CheckForNull TVNode<T> node;

    private Result(
      final @Nonnull Code code,
      final @CheckForNull TVNode<T> node)
    {
      this.code = code;
      this.node = node;
    }

    public @Nonnull Code getCode()
    {
      return this.code;
    }

    public @CheckForNull TVNode<T> getNode()
    {
      return this.node;
    }
  }

  static final class TVDirectory<T> extends TVNode<T>
  {
    private final @CheckForNull TVNode<T>             parent;
    private final @CheckForNull String                name;
    private @CheckForNull T                           data;
    private final @Nonnull TreeMap<String, TVNode<T>> children;

    TVDirectory(
      final @CheckForNull TVNode<T> parent,
      final @CheckForNull String name,
      final @CheckForNull T data)
    {
      this.parent = parent;
      this.name = name;
      this.data = data;
      this.children = new TreeMap<String, TVNode<T>>();
    }

    public @Nonnull TVDirectory<T> addDirectory(
      final @Nonnull String new_name,
      final @CheckForNull T new_data)
      throws ConstraintError
    {
      Constraints.constrainNotNull(new_name, "Directory name");
      Constraints.constrainArbitrary(
        this.children.containsKey(new_name) == false,
        "Child directory does not exist");

      final TVDirectory<T> d = new TVDirectory<T>(this, new_name, new_data);
      this.children.put(new_name, d);
      return d;
    }

    public @Nonnull TVFile<T> addFile(
      final @Nonnull String new_name,
      final @CheckForNull T new_data)
      throws ConstraintError
    {
      Constraints.constrainNotNull(new_name, "File name");
      Constraints.constrainArbitrary(
        this.children.containsKey(new_name) == false,
        "Child file does not exist");

      final TVFile<T> f = new TVFile<T>(this, new_name, new_data);
      this.children.put(new_name, f);
      return f;
    }

    @Override public boolean equals(
      final Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if ((obj instanceof TVNode) == false) {
        return false;
      }
      return this.getName().equals(((TVNode<?>) obj).getName());
    }

    public @Nonnull SortedMap<String, TVNode<T>> getChildren()
    {
      return Collections.unmodifiableSortedMap(this.children);
    }

    @Override public T getData()
    {
      return this.data;
    }

    @Override public @CheckForNull String getName()
    {
      return this.name;
    }

    @Override public @CheckForNull TVNode<T> getParent()
    {
      return this.parent;
    }

    @Override public int hashCode()
    {
      return this.name.hashCode();
    }

    @Override public void setData(
      final T data)
    {
      this.data = data;
    }
  }

  static final class TVFile<T> extends TVNode<T>
  {
    private final @Nonnull TVNode<T> parent;
    private final @Nonnull String    name;
    private @CheckForNull T          data;

    TVFile(
      final @Nonnull TVNode<T> parent,
      final @Nonnull String name,
      final @CheckForNull T data)
    {
      this.parent = parent;
      this.name = name;
      this.data = data;
    }

    @Override public boolean equals(
      final Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if ((obj instanceof TVNode) == false) {
        return false;
      }
      return this.getName().equals(((TVNode<?>) obj).getName());
    }

    @Override public T getData()
    {
      return this.data;
    }

    @Override public @Nonnull String getName()
    {
      return this.name;
    }

    @Override public @Nonnull TVNode<T> getParent()
    {
      return this.parent;
    }

    @Override public int hashCode()
    {
      return this.name.hashCode();
    }

    @Override public void setData(
      final T data)
    {
      this.data = data;
    }
  }

  static abstract class TVNode<T>
  {
    public abstract @CheckForNull T getData();

    public abstract String getName();

    public abstract TVNode<T> getParent();

    public abstract void setData(
      final T data);
  }

  public static @Nonnull <T> TreeView<T> make(
    final @CheckForNull T root_data)
  {
    return new TreeView<T>(root_data);
  }

  private static void showMakeIndent(
    final @Nonnull StringBuilder m,
    final int indent)
  {
    if (indent == 0) {
      return;
    }

    for (int index = 0; index < indent; ++index) {
      if ((index + 1) == indent) {
        m.append("+-");
      } else {
        m.append("  ");
      }
    }
  }

  private final @Nonnull TVDirectory<T> root;

  private TreeView(
    final @CheckForNull T root_data)
  {
    this.root = new TVDirectory<T>(null, null, root_data);
  }

  public @CheckForNull Result<T> createDirectories(
    final @Nonnull PathVirtual path)
    throws ConstraintError
  {
    TVNode<T> n = this.root;
    PathVirtual p = PathVirtual.ROOT;

    for (final String name : path.asNames()) {
      p = p.appendName(name);
      final TVDirectory<T> dir = (TVDirectory<T>) n;
      final SortedMap<String, TVNode<T>> children = dir.getChildren();
      if (children.containsKey(name)) {
        final TVNode<T> c = children.get(name);
        if (c instanceof TVFile) {
          return Result.notADirectory();
        }
        n = c;
      } else {
        n = dir.addDirectory(name, null);
      }
    }

    return Result.ok(n);
  }

  public @CheckForNull Result<T> get(
    final @Nonnull PathVirtual path)
    throws ConstraintError
  {
    Constraints.constrainNotNull(path, "Path");
    if (path.isRoot()) {
      return Result.ok(this.root);
    }

    final List<String> names = path.asNames();
    TVNode<T> n = this.root;
    for (final String name : names) {
      if (n instanceof TVDirectory) {
        final SortedMap<String, TVNode<T>> children =
          ((TVDirectory<T>) n).getChildren();
        if (children.containsKey(name)) {
          n = children.get(name);
          continue;
        }
        return Result.nonexistent();
      }
      return Result.notADirectory();
    }

    assert n != null;
    return Result.ok(n);
  }

  public @Nonnull TVDirectory<T> getRoot()
  {
    return this.root;
  }

  public @Nonnull StringBuilder show()
  {
    final StringBuilder m = new StringBuilder();
    this.showDirectory(m, 0, this.root);
    return m;
  }

  private void showDirectory(
    final @Nonnull StringBuilder m,
    final int indent,
    final @Nonnull TVDirectory<T> dir)
  {
    TreeView.showMakeIndent(m, indent);
    if (dir.getName() == null) {
      m.append("/");
    } else {
      m.append(dir.getName());
    }
    m.append("\n");

    final SortedMap<String, TVNode<T>> children = dir.getChildren();
    for (final Entry<String, TVNode<T>> e : children.entrySet()) {
      if (e.getValue() instanceof TVDirectory) {
        this.showDirectory(m, indent + 1, (TVDirectory<T>) e.getValue());
      } else {
        this.showFile(m, indent + 1, (TVFile<T>) e.getValue());
      }
    }
  }

  private void showFile(
    final @Nonnull StringBuilder m,
    final int indent,
    final @Nonnull TVFile<T> file)
  {
    TreeView.showMakeIndent(m, indent);
    m.append(file.getName());
    m.append("\n");
  }
}
