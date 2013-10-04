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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jlog.Log;

/**
 * Functions for unpacking test data during unit tests.
 */

final class TestData
{
  static class TemporaryDirectory
  {
    private final @Nonnull File file;

    /**
     * Request a new temporary directory, create it, tell the JVM to remove it
     * on exit.
     */

    TemporaryDirectory()
    {
      final SecureRandom r = new SecureRandom();
      final byte[] bytes = new byte[16];
      r.nextBytes(bytes);

      final String tmpdir_name = System.getProperty("java.io.tmpdir");
      if (tmpdir_name == null) {
        throw new AssertionError(
          "test-data: System property java.io.tmpdir is unset!");
      }

      final File tmpdir = new File(tmpdir_name);
      final StringBuilder name = new StringBuilder();
      name.append("io7m-jvvfs-test-");
      for (final byte b : bytes) {
        name.append(String.format("%x", Byte.valueOf(b)));
      }

      this.file = new File(tmpdir, name.toString());
      System.err.println("test-data: Creating temporary directory: "
        + this.file);

      if (this.file.toString().length() < 2) {
        throw new AssertionError(
          "test-data: Paranoia: temporary directory name is too short");
      }
      if (this.file.exists()) {
        throw new AssertionError("test-data: Temporary directory "
          + this.file
          + " already exists");
      }
      if (this.file.mkdirs() == false) {
        throw new AssertionError("test-data: Temporary directory "
          + this.file
          + " could not be created");
      }

      TestData.deleteOnExit(this.file);
    }

    public @Nonnull File getFile()
    {
      return this.file;
    }
  }

  /**
   * A list of zip files that should be copied to the filesystem.
   */

  private static @Nonnull Set<String>             zip_list;

  /**
   * A mapping from zip file names to the names of the directories to which
   * they will be unpacked.
   */

  private static @Nonnull Map<String, String>     zip_unpack_map;

  static {
    TestData.zip_list = new HashSet<String>();
    TestData.zip_list.add("single-file.zip");
    TestData.zip_list.add("single-file.jar");
    TestData.zip_list.add("single-file-and-subdir.zip");
    TestData.zip_list.add("single-file-and-subdir-implicit.zip");
    TestData.zip_list.add("single-file-in-subdir-subdir.zip");
    TestData.zip_list.add("complex.zip");
    TestData.zip_list.add("subdir-shadow.zip");
    TestData.zip_list.add("subdir-subdir-shadow.zip");
    TestData.zip_list.add("encrypted.zip");
    TestData.zip_list.add("files1-3.zip");
    TestData.zip_list.add("files4-6.zip");
    TestData.zip_list.add("unknown.unknown");

    TestData.zip_unpack_map = new HashMap<String, String>();
    TestData.zip_unpack_map.put("single-file.zip", "single-file");
    TestData.zip_unpack_map.put(
      "single-file-and-subdir.zip",
      "single-file-and-subdir");
    TestData.zip_unpack_map.put(
      "single-file-in-subdir-subdir.zip",
      "single-file-in-subdir-subdir");
    TestData.zip_unpack_map.put("complex.zip", "complex");
    TestData.zip_unpack_map.put("subdir-shadow.zip", "subdir-shadow");
    TestData.zip_unpack_map.put(
      "subdir-subdir-shadow.zip",
      "subdir-subdir-shadow");
    TestData.zip_unpack_map.put("files1-3.zip", "files1-3");
    TestData.zip_unpack_map.put("files4-6.zip", "files4-6");
  }

  /**
   * The current test data directory. Initialized by
   * {@link #getTestDataDirectory()}.
   */

  private static @CheckForNull TemporaryDirectory test_data_directory;

  /**
   * Copy the resource <code>name</code> to the file <code>out</code>.
   */

  @SuppressWarnings("resource") private static void copyResourceOut(
    final @Nonnull String name,
    final @Nonnull File out)
    throws FileNotFoundException,
      IOException
  {
    InputStream stream = null;
    try {
      stream = TestData.class.getResourceAsStream(name);
      if (stream == null) {
        throw new FileNotFoundException(
          "test-data: Could not find resource: " + name);
      }
      TestData.copyStreamOut(stream, out);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Copy the contents of <code>stream</code> to the file <code>out</code>.
   */

  private static void copyStreamOut(
    final @Nonnull InputStream input,
    final @Nonnull File out)
    throws FileNotFoundException,
      IOException
  {
    FileOutputStream stream = null;

    try {
      stream = new FileOutputStream(out);
      TestData.copyStreams(input, stream);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Copy the contents of stream <code>input<code> to <code>output</code>.
   */

  private static void copyStreams(
    final @Nonnull InputStream input,
    final @Nonnull OutputStream output)
    throws IOException
  {
    final byte buffer[] = new byte[8192];

    for (;;) {
      final int r = input.read(buffer);
      if (r == -1) {
        output.flush();
        return;
      }
      output.write(buffer, 0, r);
    }
  }

  /**
   * Unpack the zip file identified by <code>zip_stream</code> to the
   * directory <code>outdir</code>.
   */

  private static void copyZipStreamUnpack(
    final @Nonnull ZipInputStream zip_stream,
    final @Nonnull File outdir)
    throws IOException
  {
    for (;;) {
      final ZipEntry entry = zip_stream.getNextEntry();
      if (entry == null) {
        return;
      }

      final long time = entry.getTime();

      final File output_file = new File(outdir, entry.getName());
      if (entry.isDirectory()) {
        System.err.println("test-data: unzip: Creating directory "
          + output_file);
        if (output_file.mkdirs() == false) {
          throw new IOException(
            "test-data: unzip: could not create directory: " + output_file);
        }
      } else {
        System.err.println("test-data: unzip: Creating file " + output_file);

        final File parent = output_file.getParentFile();
        System.err.println("test-data: unzip: Creating parent directory "
          + parent);
        if (parent.exists() == false) {
          if (parent.mkdirs() == false) {
            throw new IOException(
              "test-data: unzip: could not create directory: " + parent);
          }
        }

        System.err.println("test-data: unzip: Creating file " + output_file);
        TestData.copyStreamOut(zip_stream, output_file);
      }

      /**
       * Force modification time to match that of the zip file.
       */

      System.err.println("test-data: unzip: Setting time to "
        + time
        + " on file "
        + output_file);
      if (output_file.setLastModified(time) == false) {
        throw new IOException(
          "test-data: unzip: could not set time on file: " + output_file);
      }

      /**
       * Reset the modification time of the parent directory.
       */

      final File parent = output_file.getParentFile();
      System.err.println("test-data: unzip: Setting time to "
        + time
        + " on parent directory "
        + parent);
      if (parent.setLastModified(time) == false) {
        throw new IOException(
          "test-data: unzip: could not set time on directory: " + parent);
      }

      TestData.deleteOnExit(output_file);
      zip_stream.closeEntry();
    }
  }

  static void deleteOnExit(
    final @Nonnull File file)
  {
    System.err.println("test-data: Marking for deletion: " + file);
    file.deleteOnExit();
  }

  @SuppressWarnings("resource") static Log getLog()
    throws IOException
  {
    InputStream stream = null;

    try {
      stream =
        TestData.class.getResourceAsStream(TestData
          .resourcePath("io7m-jvvfs.properties"));

      final Properties properties = new Properties();
      properties.load(stream);
      return new Log(properties, "com.io7m.jvvfs", "main");
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Return the name of the temporary test data directory, creating it
   * unpacking test data into it, if necessary.
   */

  public static @Nonnull File getTestDataDirectory()
    throws FileNotFoundException,
      IOException
  {
    if (TestData.test_data_directory == null) {
      TestData.test_data_directory = TestData.unpackTestData();
    }
    return TestData.test_data_directory.getFile();
  }

  private static @Nonnull String resourcePath(
    final @Nonnull String file)
  {
    return "/com/io7m/jvvfs/" + file;
  }

  /**
   * Unpack all zip files given in the resource lists above to a temporary
   * directory prior to test execution.
   */

  static @Nonnull TemporaryDirectory unpackTestData()
    throws FileNotFoundException,
      IOException
  {
    final TemporaryDirectory d = new TemporaryDirectory();
    final File outdir = d.getFile();

    for (final String file : TestData.zip_list) {
      final File outfile = new File(outdir, file);
      System.err.println("test-data: Unpacking " + file + " to " + outfile);
      TestData.copyResourceOut(file, outfile);
      TestData.deleteOnExit(outfile);
    }

    for (final Entry<String, String> e : TestData.zip_unpack_map.entrySet()) {
      final String source = e.getKey();
      final String target = e.getValue();

      final File outfile = new File(outdir, target);
      System.err.println("test-data: Unzipping " + source + " to " + outfile);

      final InputStream input = TestData.class.getResourceAsStream(source);
      if (input == null) {
        throw new IOException("test-data: Could not find resource " + source);
      }

      final ZipInputStream zip_stream =
        new ZipInputStream(new BufferedInputStream(input));
      TestData.copyZipStreamUnpack(zip_stream, outfile);
      zip_stream.close();
    }

    return d;
  }
}
