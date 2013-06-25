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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

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
  static final class ShellCommandArchives extends ShellCommand
  {
    ShellCommandArchives()
    {

    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull PrintStream out,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      final PathReal dir = config.getArchiveDirectory();
      final File file = new File(dir.toString());
      final String[] archives = file.list();
      Arrays.sort(archives);

      for (final String archive : archives) {
        out.println(archive);
      }
    }
  }

  static final class ShellCommandClose extends ShellCommand
  {
    ShellCommandClose()
    {

    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull PrintStream out,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      fs.close();
    }
  }

  abstract static class ShellCommandDefinition
  {
    abstract @Nonnull
      PartialFunction<String[], ShellCommand, ShellCommandError>
      getParser();

    abstract @Nonnull String helpText();
  }

  static final class ShellCommandFileModificationTime extends ShellCommand
  {
    private final @Nonnull PathVirtual path;

    ShellCommandFileModificationTime(
      final @Nonnull PathVirtual path)
    {
      this.path = path;
    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull PrintStream out,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      final Calendar c = fs.getModificationTime(this.path);
      final SimpleDateFormat df =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
      out.println(df.format(c.getTime()));
    }
  }

  static final class ShellCommandFileRead extends ShellCommand
  {
    private final @Nonnull PathVirtual path;

    ShellCommandFileRead(
      final @Nonnull PathVirtual path)
    {
      this.path = path;
    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull PrintStream out,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      final InputStream s = fs.openFile(this.path);

      try {
        final byte[] buffer = new byte[8192];
        for (;;) {
          final int r = s.read(buffer);
          if (r == -1) {
            break;
          }
          out.write(buffer, 0, r);
        }
      } catch (final IOException e) {
        log.error("i/o error: " + e.getMessage());
      } finally {
        try {
          s.close();
        } catch (final IOException e) {
          log.error("i/o error: " + e.getMessage());
        }
      }
    }
  }

  static final class ShellCommandFileSize extends ShellCommand
  {
    private final @Nonnull PathVirtual path;

    ShellCommandFileSize(
      final @Nonnull PathVirtual path)
    {
      this.path = path;
    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull PrintStream out,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      out.println(Long.toString(fs.getFileSize(this.path)));
    }
  }

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
      final @Nonnull PrintStream out,
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
      final @Nonnull PrintStream out,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      log.info("help: " + ShellCommand.makeHelpText());
    }
  }

  static final class ShellCommandListMounts extends ShellCommand
  {
    ShellCommandListMounts()
    {

    }

    @Override void run(
      final @Nonnull Log log,
      final @Nonnull PrintStream out,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      final SortedMap<PathVirtual, Deque<PathReal>> mounts =
        fs.getMountedArchives();

      int longest = 0;
      for (final PathVirtual mount : mounts.keySet()) {
        longest = Math.max(longest, mount.toString().length());
      }
      longest += 2;

      for (final PathVirtual mount : mounts.keySet()) {
        final Deque<PathReal> stack = mounts.get(mount);
        for (final PathReal p : stack) {
          out.print(mount.toString());
          final int pad = longest - mount.toString().length();
          for (int index = 0; index < pad; ++index) {
            out.append(' ');
          }
          out.println(p.toFile().getName());
        }
      }
    }
  }

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
      final @Nonnull PrintStream out,
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
      final @Nonnull PrintStream out,
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
      final @Nonnull PrintStream out,
      final @Nonnull ShellConfig config,
      final @Nonnull Filesystem fs)
      throws FilesystemError,
        ConstraintError
    {
      fs.unmount(this.path);
    }
  }

  private static final @Nonnull Map<String, ShellCommandDefinition> commands;

  static {
    commands = new HashMap<String, ShellCommandDefinition>();

    ShellCommand.commands.put("archives", new ShellCommandDefinition() {
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

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: archives");
        b.append(System.lineSeparator());
        b.append("  List all available archive files");
        return b.toString();
      }
    });

    ShellCommand.commands.put("close", new ShellCommandDefinition() {
      @Override @Nonnull
        PartialFunction<String[], ShellCommand, ShellCommandError>
        getParser()
      {
        return new PartialFunction<String[], ShellCommand, ShellCommandError>() {
          @Override public ShellCommand call(
            final @Nonnull String[] arguments)
            throws ShellCommandError
          {
            return new ShellCommandClose();
          }
        };
      }

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: close");
        b.append(System.lineSeparator());
        b.append("  Close (reset) the current filesystem");
        return b.toString();
      }
    });

    ShellCommand.commands.put("file-size", new ShellCommandDefinition() {
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
                  "file-size <path>");
              }
              return new ShellCommandFileSize(PathVirtual
                .ofString(arguments[1]));
            } catch (final ConstraintError e) {
              throw new ShellCommandError.ShellCommandConstraintError(e);
            }
          }
        };
      }

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: file-size <path>");
        b.append(System.lineSeparator());
        b.append("  Retrieve the size of the file at <path>");
        return b.toString();
      }
    });

    ShellCommand.commands.put("file-time", new ShellCommandDefinition() {
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
                  "file-time <path>");
              }
              return new ShellCommandFileModificationTime(PathVirtual
                .ofString(arguments[1]));
            } catch (final ConstraintError e) {
              throw new ShellCommandError.ShellCommandConstraintError(e);
            }
          }
        };
      }

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: file-time <path>");
        b.append(System.lineSeparator());
        b.append("  Retrieve the modification time of the file at <path>");
        return b.toString();
      }
    });

    ShellCommand.commands.put("file-read", new ShellCommandDefinition() {
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
                  "file-read <path>");
              }
              return new ShellCommandFileRead(PathVirtual
                .ofString(arguments[1]));
            } catch (final ConstraintError e) {
              throw new ShellCommandError.ShellCommandConstraintError(e);
            }
          }
        };
      }

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: file-read <path>");
        b.append(System.lineSeparator());
        b.append("  Display the contents of the file at <path>");
        return b.toString();
      }
    });

    ShellCommand.commands.put("list-mounts", new ShellCommandDefinition() {
      @Override @Nonnull
        PartialFunction<String[], ShellCommand, ShellCommandError>
        getParser()
      {
        return new PartialFunction<String[], ShellCommand, ShellCommandError>() {
          @Override public ShellCommand call(
            final @Nonnull String[] arguments)
            throws ShellCommandError
          {
            return new ShellCommandListMounts();
          }
        };
      }

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: list-mounts");
        b.append(System.lineSeparator());
        b.append("  List all currently mounted archives.");
        b
          .append("For each mount point, most recently mounted archives are listed first.");
        return b.toString();
      }
    });

    ShellCommand.commands.put("mkdir", new ShellCommandDefinition() {
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

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: mkdir <path>");
        b.append(System.lineSeparator());
        b.append("  Create a directory at <path>");
        return b.toString();
      }
    });

    ShellCommand.commands.put("mount", new ShellCommandDefinition() {
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

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: mount <archive> <path>");
        b.append(System.lineSeparator());
        b.append("  Mount the archive <archive> at <path>");
        return b.toString();
      }
    });

    ShellCommand.commands.put("unmount", new ShellCommandDefinition() {
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

      @Override @Nonnull String helpText()
      {
        final StringBuilder b = new StringBuilder();
        b.append("syntax: unmount <path>");
        b.append(System.lineSeparator());
        b.append("  Unmount the topmost archive mounted at <path>");
        return b.toString();
      }
    });

    ShellCommand.commands.put("help", new ShellCommandDefinition() {
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

      @Override @Nonnull String helpText()
      {
        return ShellCommand.makeHelpText().toString();
      }
    });
  }

  static @Nonnull Completer commandCompleter()
  {
    return new StringsCompleter(ShellCommand.commands.keySet());
  }

  static StringBuilder makeHelpText()
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

  abstract void run(
    final @Nonnull Log log,
    final @Nonnull PrintStream out,
    final @Nonnull ShellConfig config,
    final @Nonnull Filesystem fs)
    throws FilesystemError,
      ConstraintError;
}
