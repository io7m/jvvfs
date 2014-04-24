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

package com.io7m.jvvfs.shell;

import com.io7m.jvvfs.FilesystemError;

abstract class ShellCommandError extends Exception
{
  static final class ShellCommandFilesystemError extends ShellCommandError
  {
    private static final long serialVersionUID = -6219730618277127807L;

    ShellCommandFilesystemError(
      final FilesystemError error)
    {
      super(Type.SHELL_COMMAND_FILESYSTEM_ERROR, error);
    }
  }

  static final class ShellCommandParseError extends ShellCommandError
  {
    private static final long serialVersionUID = 4582339751775592743L;

    ShellCommandParseError(
      final String message)
    {
      super(Type.SHELL_COMMAND_PARSE_ERROR, message);
    }
  }

  static final class ShellCommandUnknown extends ShellCommandError
  {
    private static final long serialVersionUID = 4582339751775592743L;

    ShellCommandUnknown(
      final String command)
    {
      super(Type.SHELL_COMMAND_UNKNOWN, command);
    }
  }

  static enum Type
  {
    SHELL_COMMAND_PARSE_ERROR,
    SHELL_COMMAND_FILESYSTEM_ERROR,
    SHELL_COMMAND_UNKNOWN
  }

  private static final long serialVersionUID = -6668513869286909183L;
  private final Type        type;

  protected ShellCommandError(
    final Type in_type,
    final String message)
  {
    super(message);
    this.type = in_type;
  }

  protected ShellCommandError(
    final Type in_type,
    final Throwable error)
  {
    super(error);
    this.type = in_type;
  }

  Type getType()
  {
    return this.type;
  }
}
