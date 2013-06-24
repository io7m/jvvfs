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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.PartialFunction;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathReal;
import com.io7m.jvvfs.PathVirtual;

abstract class ShellCommand
{
  static final class ShellCommandHelp extends ShellCommand
  {
    private final @Nonnull String command;

    ShellCommandHelp(
      final @Nonnull String command)
    {
      this.command = command;
    }

    @SuppressWarnings("synthetic-access") @Override void run(
      final @Nonnull Log log,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      if (ShellCommand.commands.containsKey(this.command)) {
        final ShellCommandDefinition c =
          ShellCommand.commands.get(this.command);
        log.info("help: " + c.helpText());
      }
    }
  }

  static final class ShellCommandHelpNoArguments extends ShellCommand
  {
    @Override void run(
      final @Nonnull Log log,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      log.info("help: " + ShellCommand.HELP_TEXT);
    }
  }

  static @Nonnull String HELP_TEXT;

  abstract void run(
    final @Nonnull Log log,
    final @Nonnull ShellConfig config,
    final @Nonnull Filesystem fs)
    throws FilesystemError,
      ConstraintError;

  static final class ShellCommandMkdir extends ShellCommand
  {
    private final @Nonnull PathVirtual path;

    ShellCommandMkdir(
      final @Nonnull PathVirtual path)
    {
      this.path = path;
    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      fs.createDirectory(this.path);
    }
  }

  static final class ShellCommandMount extends ShellCommand
  {
    private final @Nonnull PathVirtual path;
    private final @Nonnull String      archive;

    ShellCommandMount(
      final @Nonnull String archive,
      final @Nonnull PathVirtual path)
    {
      this.archive = archive;
      this.path = path;
    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      fs.mountArchive(this.archive, this.path);
    }
  }

  static final class ShellCommandUnmount extends ShellCommand
  {
    private final @Nonnull PathVirtual path;

    ShellCommandUnmount(
      final @Nonnull PathVirtual path)
    {
      this.path = path;
    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      fs.unmount(this.path);
    }
  }

  static final class ShellCommandArchives extends ShellCommand
  {
    ShellCommandArchives()
    {

    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      final PathReal dir = config.getArchiveDirectory();
      final File file = new File(dir.toString());
      for (final String archive : file.list()) {
        log.info("archive: " + archive);
      }
    }
  }

  abstract static class ShellCommandDefinition
  {
    abstract @Nonnull
      PartialFunction<String[], ShellCommand, ShellCommandError>
      getParser();

    abstract @Nonnull String helpText();
  }

  private static final @Nonnull Map<String, ShellCommandDefinition> commands;

  static {
    commands = new HashMap<String, ShellCommandDefinition>();

    ShellCommand.commands.put("mkdir", new ShellCommandDefinition() {
      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: mkdir <path>");
        b.append(System.lineSeparator());
        b.append("  Create a directory at <path>");
        return b.toString();
      }

      @Override @Nonnull
        PartialFunction<String[], ShellCommand, ShellCommandError>
        getParser()
      {
        return new PartialFunction<String[], ShellCommand, ShellCommandError>() {
          @Override public ShellCommand call(
            final @Nonnull String[] arguments)
            throws ShellCommandError
          {
            try {
              if (arguments.length < 2) {
                throw new ShellCommandError.ShellCommandParseError(
                  "mkdir <path>");
              }
              return new ShellCommandMkdir(PathVirtual.ofString(arguments[1]));
            } catch (final ConstraintError e) {
              throw new ShellCommandError.ShellCommandConstraintError(e);
            }
          }
        };
      }
    });

    ShellCommand.commands.put("mount", new ShellCommandDefinition() {
      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: mount <archive> <path>");
        b.append(System.lineSeparator());
        b.append("  Mount the archive <archive> at <path>");
        return b.toString();
      }

      @Override @Nonnull
        PartialFunction<String[], ShellCommand, ShellCommandError>
        getParser()
      {
        return new PartialFunction<String[], ShellCommand, ShellCommandError>() {
          @Override public ShellCommand call(
            final @Nonnull String[] arguments)
            throws ShellCommandError
          {
            try {
              if (arguments.length < 3) {
                throw new ShellCommandError.ShellCommandParseError(
                  "mount <archive> <path>");
              }
              return new ShellCommandMount(arguments[1], PathVirtual
                .ofString(arguments[2]));
            } catch (final ConstraintError e) {
              throw new ShellCommandError.ShellCommandConstraintError(e);
            }
          }
        };
      }
    });

    ShellCommand.commands.put("unmount", new ShellCommandDefinition() {
      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: unmount <path>");
        b.append(System.lineSeparator());
        b.append("  Unmount the topmost archive mounted at <path>");
        return b.toString();
      }

      @Override @Nonnull
        PartialFunction<String[], ShellCommand, ShellCommandError>
        getParser()
      {
        return new PartialFunction<String[], ShellCommand, ShellCommandError>() {
          @Override public ShellCommand call(
            final @Nonnull String[] arguments)
            throws ShellCommandError
          {
            try {
              if (arguments.length < 2) {
                throw new ShellCommandError.ShellCommandParseError(
                  "unmount <path>");
              }
              return new ShellCommandUnmount(PathVirtual
                .ofString(arguments[1]));
            } catch (final ConstraintError e) {
              throw new ShellCommandError.ShellCommandConstraintError(e);
            }
          }
        };
      }
    });

    ShellCommand.commands.put("archives", new ShellCommandDefinition() {
      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: archives");
        b.append(System.lineSeparator());
        b.append("  List all available archive files");
        return b.toString();
      }

      @Override @Nonnull
        PartialFunction<String[], ShellCommand, ShellCommandError>
        getParser()
      {
        return new PartialFunction<String[], ShellCommand, ShellCommandError>() {
          @Override public ShellCommand call(
            final @Nonnull String[] arguments)
            throws ShellCommandError
          {
            return new ShellCommandArchives();
          }
        };
      }
    });

    ShellCommand.commands.put("help", new ShellCommandDefinition() {
      @Override @Nonnull String helpText()
      {
        return ShellCommand.HELP_TEXT;
      }

      @Override @Nonnull
        PartialFunction<String[], ShellCommand, ShellCommandError>
        getParser()
      {
        return new PartialFunction<String[], ShellCommand, ShellCommandError>() {
          @Override public ShellCommand call(
            final @Nonnull String[] arguments)
            throws ShellCommandError
          {
            if (arguments.length < 2) {
              return new ShellCommandHelpNoArguments();
            }
            return new ShellCommandHelp(arguments[1]);
          }
        };
      }
    });

    ShellCommand.HELP_TEXT = ShellCommand.makeHelpText().toString();
  }

  private static StringBuilder makeHelpText()
  {
    final StringBuilder b = new StringBuilder();
    b.append("syntax: help [");
    for (final String name : ShellCommand.commands.keySet()) {
      b.append(name);
      b.append(" ");
    }
    b.append("]");
    b.append(System.lineSeparator());
    b.append("  Get help on the given command");
    return b;
  }

  static @Nonnull Completer commandCompleter()
  {
    return new StringsCompleter(ShellCommand.commands.keySet());
  }

  static @Nonnull ShellCommand parseCommand(
    final @Nonnull String text)
    throws ShellCommandError
  {
    final String[] segments = text.split("\\s+");
    if (ShellCommand.commands.containsKey(segments[0])) {
      final ShellCommandDefinition c = ShellCommand.commands.get(segments[0]);
      return c.getParser().call(segments);
    }

    throw new ShellCommandError.ShellCommandUnknown(segments[0]);
  }
}
