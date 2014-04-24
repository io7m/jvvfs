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
import java.util.Deque;

import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathReal;
import com.io7m.jvvfs.PathVirtual;

final class ShellCommandListMounts extends ShellCommand
{
  ShellCommandListMounts()
  {

  }

  @Override void run(
    final LogUsableType log,
    final PrintStream out,
    final ShellConfig config,
    final FilesystemType fs)
    throws FilesystemError
  {
    final Deque<Pair<PathReal, PathVirtual>> mounts = fs.getMountedArchives();

    int longest = 0;
    for (final Pair<PathReal, PathVirtual> pair : mounts) {
      longest = Math.max(longest, pair.getRight().toString().length());
    }
    longest += 2;

    for (final Pair<PathReal, PathVirtual> pair : mounts) {
      out.print(pair.getRight());
      ShellCommand.printPadSpace(out, longest, pair
        .getRight()
        .toString()
        .length());
      out.println(pair.getLeft().toString());
    }
  }

  static ShellCommandDefinitionType getDefinition()
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
            return new ShellCommandListMounts();
          }
        };
      }

      @Override public String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: list-mounts");
        b.append(System.getProperty("line.separator"));
        b.append("  List all currently mounted archives.");
        b
          .append("For each mount point, most recently mounted archives are listed first.");
        return b.toString();
      }
    };
  }
}
