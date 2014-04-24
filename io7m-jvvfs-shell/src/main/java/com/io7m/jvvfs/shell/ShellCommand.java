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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import com.io7m.jlog.LogUsableType;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;

abstract class ShellCommand implements ShellCommandType
{
  static final Map<String, ShellCommandDefinitionType> COMMANDS;

  static {
    COMMANDS = new HashMap<String, ShellCommandDefinitionType>();

    ShellCommand.COMMANDS.put(
      "archives",
      ShellCommandArchives.getDefinition());
    ShellCommand.COMMANDS.put("close", ShellCommandClose.getDefinition());
    ShellCommand.COMMANDS.put(
      "file-size",
      ShellCommandFileSize.getDefinition());
    ShellCommand.COMMANDS.put(
      "file-time",
      ShellCommandFileModificationTime.getDefinition());
    ShellCommand.COMMANDS.put(
      "file-read",
      ShellCommandFileRead.getDefinition());
    ShellCommand.COMMANDS.put(
      "file-list",
      ShellCommandFileList.getDefinition());
    ShellCommand.COMMANDS.put(
      "file-list-long",
      ShellCommandFileListLong.getDefinition());
    ShellCommand.COMMANDS.put(
      "list-mounts",
      ShellCommandListMounts.getDefinition());
    ShellCommand.COMMANDS.put("mkdir", ShellCommandMkdir.getDefinition());
    ShellCommand.COMMANDS.put("mount", ShellCommandMount.getDefinition());
    ShellCommand.COMMANDS.put("unmount", ShellCommandUnmount.getDefinition());
    ShellCommand.COMMANDS.put("help", ShellCommandHelp.getDefinition());
  }

  static Completer commandCompleter()
  {
    return new StringsCompleter(ShellCommand.COMMANDS.keySet());
  }

  static StringBuilder makeHelpText()
  {
    final StringBuilder b = new StringBuilder();
    b.append("syntax: help [");
    for (final String name : ShellCommand.COMMANDS.keySet()) {
      b.append(name);
      b.append(" ");
    }
    b.append("]");
    b.append(System.getProperty("line.separator"));
    b.append("  Get help on the given command");
    return b;
  }

  static ShellCommand parseCommand(
    final String text)
    throws ShellCommandError
  {
    final String[] segments = text.split("\\s+");
    if (ShellCommand.COMMANDS.containsKey(segments[0])) {
      final ShellCommandDefinitionType c =
        ShellCommand.COMMANDS.get(segments[0]);
      return c.getParser().call(segments);
    }

    throw new ShellCommandError.ShellCommandUnknown(segments[0]);
  }

  abstract void run(
    final LogUsableType log,
    final PrintStream out,
    final ShellConfig config,
    final FilesystemType filesystem)
    throws FilesystemError;

  static void printPadSpace(
    final PrintStream out,
    final int longest,
    final int length)
  {
    final int pad = longest - length;
    for (int index = 0; index < pad; ++index) {
      out.append(' ');
    }
  }

  static void printCalendarStamp(
    final PrintStream out,
    final Calendar c)
  {
    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    out.print(df.format(c.getTime()));
  }
}
