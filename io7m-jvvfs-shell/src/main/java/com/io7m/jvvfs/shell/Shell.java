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

import java.io.IOException;
import java.util.Properties;

import javax.annotation.Nonnull;

import jline.console.ConsoleReader;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathReal;

public final class Shell implements Runnable
{
  /**
   * Run the shell.
   */

  public static void main(
    final String args[])
    throws IOException,
      ConstraintError
  {
    if (args.length < 2) {
      System.err.println("shell: usage: shell.conf archive-directory");
      System.exit(1);
    }

    final Properties p = PropertyUtils.loadFromFile(args[0]);
    final Log log = new Log(p, "com.io7m.jvvfs", "shell");

    final ShellConfig c =
      ShellConfig.loadFromProperties(p, new PathReal(args[1]));
    final Shell shell = new Shell(c, log);
    shell.run();
  }

  private final @Nonnull ShellConfig   config;
  private final @Nonnull Log           log;
  private final @Nonnull ConsoleReader reader;
  private final @Nonnull Filesystem    filesystem;

  private Shell(
    final @Nonnull ShellConfig config,
    final @Nonnull Log log)
    throws IOException,
      ConstraintError
  {
    this.config = config;
    this.log = log;
    this.filesystem =
      Filesystem.makeWithArchiveDirectory(log, config.getArchiveDirectory());

    this.reader = new ConsoleReader();
    this.reader.addCompleter(ShellCommand.commandCompleter());
    this.reader.setPrompt("jvvfs> ");
  }

  @Override public void run()
  {
    for (;;) {
      try {
        final String line = this.reader.readLine();
        final ShellCommand cmd = ShellCommand.parseCommand(line);
        cmd.run(this.log, this.config, this.filesystem);
      } catch (final IOException e) {
        this.log.error("i/o error: " + e.getMessage());
      } catch (final ConstraintError e) {
        this.log.error("constraint error: " + e.getMessage());
      } catch (final FilesystemError e) {
        this.log.error("filesystem error: " + e.getMessage());
      } catch (final ShellCommandError e) {
        switch (e.getType()) {
          case SHELL_COMMAND_CONSTRAINT_ERROR:
          {
            this.log.error("constraint error: " + e.getMessage());
            break;
          }
          case SHELL_COMMAND_PARSE_ERROR:
          {
            this.log.error("parse error");
            this.log.error("usage: " + e.getMessage());
            break;
          }
          case SHELL_COMMAND_UNKNOWN:
          {
            this.log.error("unknown command: " + e.getMessage());
            break;
          }
        }
      }
    }
  }
}
