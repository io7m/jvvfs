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
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.PathReal;

final class Shell implements Runnable
{
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
  private final @Nonnull StringBuilder line;

  private Shell(
    final @Nonnull ShellConfig config,
    final @Nonnull Log log)
  {
    this.config = config;
    this.log = log;
    this.line = new StringBuilder();
  }

  private static void readLine(
    final @Nonnull InputStream input,
    final @Nonnull StringBuilder buffer)
    throws IOException
  {
    for (;;) {
      final int c = input.read();
      if (c == '\n') {
        break;
      }
      buffer.append(c);
    }
  }

  @Override public void run()
  {
    try {
      for (;;) {
        this.line.setLength(0);
        Shell.readLine(System.in, this.line);
        this.log.debug(this.line.toString());
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
