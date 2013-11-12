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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.io7m.jaux.UnreachableCodeException;

final class ArchiveHandlerRegistration
{
  private static final @Nonnull HashMap<String, Callable<? extends ArchiveHandler<?>>> HANDLERS;

  static {
    HANDLERS = new HashMap<String, Callable<? extends ArchiveHandler<?>>>();

    ArchiveHandlerRegistration.addHandler(
      ArchiveDirectoryHandler.class.getCanonicalName(),
      new ArchiveDirectoryHandlerCallable());

    ArchiveHandlerRegistration.addHandler(
      ArchiveZipHandler.class.getCanonicalName(),
      new ArchiveZipHandlerCallable());
  }

  private static final class ArchiveDirectoryHandlerCallable implements
    Callable<ArchiveHandler<ArchiveDirectoryKind>>
  {
    ArchiveDirectoryHandlerCallable()
    {
      // Nothing
    }

    @Override public ArchiveHandler<ArchiveDirectoryKind> call()
      throws Exception
    {
      return new ArchiveDirectoryHandler();
    }
  }

  private static final class ArchiveZipHandlerCallable implements
    Callable<ArchiveHandler<ArchiveZipKind>>
  {
    ArchiveZipHandlerCallable()
    {
      // Nothing
    }

    @Override public ArchiveHandler<ArchiveZipKind> call()
      throws Exception
    {
      return new ArchiveZipHandler();
    }
  }

  synchronized static void addHandler(
    final @Nonnull String name,
    final @Nonnull Callable<? extends ArchiveHandler<?>> c)
  {
    ArchiveHandlerRegistration.HANDLERS.put(name, c);
  }

  synchronized static @Nonnull
    Map<String, Callable<? extends ArchiveHandler<?>>>
    getHandlers()
  {
    return Collections
      .unmodifiableMap(new HashMap<String, Callable<? extends ArchiveHandler<?>>>(
        ArchiveHandlerRegistration.HANDLERS));
  }

  synchronized static @Nonnull List<ArchiveHandler<?>> makeHandlers()
  {
    final Set<Entry<String, Callable<? extends ArchiveHandler<?>>>> es =
      ArchiveHandlerRegistration.HANDLERS.entrySet();
    final ArrayList<ArchiveHandler<?>> xs =
      new ArrayList<ArchiveHandler<?>>();

    for (final Entry<String, Callable<? extends ArchiveHandler<?>>> e : es) {
      try {
        xs.add(e.getValue().call());
      } catch (final Exception x) {
        throw new UnreachableCodeException(x);
      }
    }

    return xs;
  }
}
