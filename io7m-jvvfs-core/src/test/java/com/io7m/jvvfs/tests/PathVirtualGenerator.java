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

package com.io7m.jvvfs.tests;

import java.util.ArrayList;

import net.java.quickcheck.Generator;

import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;

/**
 * A random virtual path generator.
 */

public final class PathVirtualGenerator implements Generator<PathVirtual>
{
  private final ValidNameGenerator gen;

  PathVirtualGenerator()
  {
    this.gen = new ValidNameGenerator();
  }

  @Override public PathVirtual next()
  {
    final int count = (int) Math.round(Math.random() * 16);
    final ArrayList<String> names = new ArrayList<String>();

    for (int index = 0; index < count; ++index) {
      final String name = this.gen.next();
      names.add(name);
    }

    try {
      return PathVirtual.ofNames(names);
    } catch (final FilesystemError e) {
      throw new AssertionError("Invalid name generated, report this bug!");
    }
  }
}
