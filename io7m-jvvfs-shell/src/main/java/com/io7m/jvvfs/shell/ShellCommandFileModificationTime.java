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

import java.io.PrintStream;
import java.util.Calendar;

import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathVirtual;

final class ShellCommandFileModificationTime extends ShellCommand
{
  public static ShellCommandDefinitionType getDefinition()
  {
    return new ShellCommandDefinitionType() {
      @Override public
        PartialFunctionType<String[], ShellCommand, ShellCommandError>
        getParser()
      {
        return new PartialFunctionType<String[], ShellCommand, ShellCommandError>() {
          @Override public ShellCommand call(
            final String[] arguments)
            throws ShellCommandError
          {
            try {
              if (arguments.length < 2) {
                throw new ShellCommandError.ShellCommandParseError(
                  "file-time <path>");
              }
              return new ShellCommandFileModificationTime(
                PathVirtual.ofString(arguments[1]));
            } catch (final FilesystemError e) {
              throw new ShellCommandError.ShellCommandFilesystemError(e);
            }
          }
        };
      }

      @Override public String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: file-time <path>");
        b.append(System.getProperty("line.separator"));
        b.append("  Retrieve the modification time of the file at <path>");
        return b.toString();
      }
    };
  }

  private final PathVirtual path;

  ShellCommandFileModificationTime(
    final PathVirtual in_path)
  {
    this.path = in_path;
  }

  @Override void run(
    final LogUsableType log,
    final PrintStream out,
    final ShellConfig config,
    final FilesystemType fs)
    throws FilesystemError
  {
    final Calendar c = fs.getModificationTime(this.path);
    ShellCommand.printCalendarStamp(out, c);
    out.println();
  }
}
