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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import jline.console.ConsoleReader;

import com.io7m.jlog.Log;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathReal;

/**
 * An interactive shell for exploring filesystems.
 */

public final class ShellMain implements Runnable
{
  /**
   * Run the shell.
   * 
   * @param args
   *          Command line arguments
   * @throws JPropertyException
   *           If the given shell configuration is malformed
   * @throws IOException
   *           On I/O errors
   */

  public static void main(
    final String[] args)
    throws IOException,
      JPropertyException
  {
    NullCheck.notNull(args, "Command line arguments");
    for (int index = 0; index < args.length; ++index) {
      final String arg = args[index];
      NullCheck.notNull(arg, "Argument[" + index + "]");
    }

    if (args.length < 2) {
      System.err.println("shell: usage: shell.conf archive-directory");
      System.exit(1);
    }

    final Properties p = JProperties.fromFile(new File(args[0]));
    final LogPolicyType policy =
      LogPolicyProperties.newPolicy(p, "com.io7m.jvvfs");
    final LogType log = Log.newLog(policy, "shell");

    final ShellConfig c =
      ShellConfig.loadFromProperties(p, new PathReal(args[1]));
    final ShellMain shell = new ShellMain(c, log);
    shell.run();
  }

  private final ShellConfig    config;
  private final LogUsableType  log;
  private final ConsoleReader  reader;
  private final FilesystemType filesystem;

  private ShellMain(
    final ShellConfig in_config,
    final LogUsableType in_log)
    throws IOException
  {
    this.config = in_config;
    this.log = in_log;
    this.filesystem =
      Filesystem.makeWithArchiveDirectory(
        in_log,
        in_config.getArchiveDirectory());

    this.reader = new ConsoleReader();
    this.reader.addCompleter(ShellCommand.commandCompleter());
    this.reader.setPrompt("jvvfs> ");
  }

  private void cleanup()
  {
    try {
      this.filesystem.close();
    } catch (final FilesystemError e) {
      e.printStackTrace();
    }
  }

  @Override public void run()
  {
    for (;;) {
      try {
        final String line = this.reader.readLine();
        if (line == null) {
          this.cleanup();
          break;
        }
        if (line.isEmpty()) {
          continue;
        }

        final ShellCommand cmd = ShellCommand.parseCommand(line);
        cmd.run(this.log, System.out, this.config, this.filesystem);
      } catch (final IOException e) {
        this.log.error("i/o error: " + e.getMessage());
      } catch (final FilesystemError e) {
        this.log.error("filesystem error: " + e.getMessage());
      } catch (final ShellCommandError e) {
        switch (e.getType()) {
          case SHELL_COMMAND_FILESYSTEM_ERROR:
          {
            this.log.error("filesystem error: " + e.getMessage());
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
