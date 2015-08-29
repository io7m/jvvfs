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

import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfunctional.Pair;
import com.io7m.jvvfs.FileReference.Type;
import com.io7m.jvvfs.tests.PathVirtualTest;

public final class FileReferenceTest
{
  static final class FileReferenceId extends FileReference<ArchiveKind>
  {
    FileReferenceId(
      final Archive<ArchiveKind> in_archive,
      final PathVirtual in_path,
      final Type in_type)
    {
      super(in_archive, in_path, in_type);
    }
  }

  @SuppressWarnings("static-method") @Test public void testToString()
  {
    PathVirtualTest
      .runWithPairGenerator(new AbstractCharacteristic<Pair<PathVirtual, PathVirtual>>() {
        @Override protected void doSpecify(
          final Pair<PathVirtual, PathVirtual> pair)
          throws Throwable
        {
          final FileReference<ArchiveKind> r0 =
            new FileReferenceId(new ArchiveIdentity(), pair
              .getLeft()
              .appendName("xyz"), Type.TYPE_FILE);
          final FileReference<ArchiveKind> r1 =
            new FileReferenceId(new ArchiveIdentity(), pair
              .getRight()
              .appendName("abc"), Type.TYPE_FILE);

          Assert.assertFalse(r0.toString().equals(r1.toString()));
        }
      });
  }
}
