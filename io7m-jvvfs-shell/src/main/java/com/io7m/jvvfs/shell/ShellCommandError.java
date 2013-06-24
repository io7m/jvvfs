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

package com.io7m.jvvfs.shell;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;

abstract class ShellCommandError extends Exception
{
  private static final long serialVersionUID = -6668513869286909183L;

  static enum Type
  {
    SHELL_COMMAND_PARSE_ERROR,
    SHELL_COMMAND_CONSTRAINT_ERROR,
    SHELL_COMMAND_UNKNOWN
  }

  private final @Nonnull Type type;

  protected ShellCommandError(
    final @Nonnull Type type,
    final @Nonnull String message)
  {
    super(message);
    this.type = type;
  }

  @Nonnull Type getType()
  {
    return this.type;
  }

  protected ShellCommandError(
    final @Nonnull Type type,
    final @Nonnull Throwable error)
  {
    super(error);
    this.type = type;
  }

  static final class ShellCommandParseError extends ShellCommandError
  {
    private static final long serialVersionUID = 4582339751775592743L;

    ShellCommandParseError(
      final @Nonnull String message)
    {
      super(Type.SHELL_COMMAND_PARSE_ERROR, message);
    }
  }

  static final class ShellCommandUnknown extends ShellCommandError
  {
    private static final long serialVersionUID = 4582339751775592743L;

    ShellCommandUnknown(
      final @Nonnull String command)
    {
      super(Type.SHELL_COMMAND_UNKNOWN, command);
    }
  }

  static final class ShellCommandConstraintError extends ShellCommandError
  {
    private static final long serialVersionUID = -6219730618277127807L;

    ShellCommandConstraintError(
      final @Nonnull ConstraintError error)
    {
      super(Type.SHELL_COMMAND_CONSTRAINT_ERROR, error);
    }
  }
}
