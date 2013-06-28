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

package com.io7m.jvvfs;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;

/**
 * <p>
 * Names of files and directories.
 * </p>
 */

public final class Name
{
  /**
   * <p>
   * Determine whether or not the given <code>name</code> is valid.
   * </p>
   * <p>
   * Names in <code>jvvfs</code> are specifically not allowed to contain:
   * </p>
   * <ul>
   * <li>Forward slashes <code>(['/'], ASCII [0x2f])</code>, as this is used
   * as a path separator on UNIX and in <code>jvvfs</code> virtual paths.</li>
   * <li>Backslashes <code>(['\'], ASCII [0x5c])</code>, as this is used as a
   * path separator on Microsoft Windows.</li>
   * <li>A series of two or more dots <code>(['.'], ASCII [0x2e])</code>, as
   * this is a reserved name on UNIX-like platforms.</li>
   * <li>Colons <code>([':'], ASCII [0x3a])</code>, as these are used to
   * identify "drives" on some operating systems.</li>
   * <li>Null <code>(ASCII [0x0])</code>, as almost no operating systems
   * permit these in file names.</li>
   * </ul>
   * <p>
   * Empty strings are not considered to be valid names.
   * </p>
   * 
   * @throws ConstraintError
   *           Iff <code>name == null</code>.
   */

  public static boolean isValid(
    final @Nonnull String name)
    throws ConstraintError
  {
    Constraints.constrainNotNull(name, "Name is not null");

    if (name.length() == 0) {
      return false;
    }

    for (int index = 0; index < name.length(); ++index) {
      switch (name.charAt(index)) {
        case '/':
          return false;
        case '\\':
          return false;
        case ':':
          return false;
        case '.':
          if ((index + 1) < name.length()) {
            if (name.charAt(index + 1) == '.') {
              return false;
            }
          }
          break;
        case '\0':
          return false;
        default:
          break;
      }
    }

    return true;
  }

  private Name()
  {
    throw new UnreachableCodeException();
  }
}
