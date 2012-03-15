package com.io7m.jvvfs;

import java.io.InputStream;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;

public interface FilesystemAPI
{
  /**
   * Create the directory <code>path</code>, including all ancestors of
   * <code>path</code> if necessary.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  void createDirectory(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Create the directory <code>path</code>, including all ancestors of
   * <code>path</code> if necessary.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  void createDirectory(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Return the size in bytes of the file specified by <code>path</code>. In
   * the case of compressed filesystems/archives, the size represents the
   * uncompressed size of the specified file.
   * 
   * @param path
   *          The file.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   */

  long fileSize(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Return the size in bytes of the file specified by <code>path</code>. In
   * the case of compressed filesystems/archives, the size represents the
   * uncompressed size of the specified file.
   * 
   * @param path
   *          The file.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   */

  long fileSize(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Returns <code>true</code> iff <code>path</code> exists and is a
   * directory.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  boolean isDirectory(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Returns <code>true</code> iff <code>path</code> exists and is a
   * directory.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  boolean isDirectory(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Returns <code>true</code> iff <code>path</code> exists and is a file.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  boolean isFile(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Returns <code>true</code> iff <code>path</code> exists and is a file.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   * @throws FilesystemError
   */

  boolean isFile(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError;

  /**
   * List the directory at <code>path</code>.
   * 
   * @param path
   *          The path.
   * @param items
   *          The set containing the contents of the directory.
   */

  void listDirectory(
    final @Nonnull PathVirtual path,
    final @Nonnull TreeSet<String> items)
    throws ConstraintError,
      FilesystemError;

  /**
   * List the directory at <code>path</code>.
   * 
   * @param path
   *          The path.
   * @param items
   *          The set containing the contents of the directory.
   */

  void listDirectory(
    final @Nonnull String path,
    final @Nonnull TreeSet<String> items)
    throws ConstraintError,
      FilesystemError;

  /**
   * Return the modification time of the file referenced by <code>path</code>.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   */

  long modificationTime(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Return the modification time of the file referenced by <code>path</code>.
   * 
   * @param path
   *          The path.
   * @throws ConstraintError
   *           Iff <code>path == null</code>.
   */

  long modificationTime(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError;

  /**
   * Mount the archive <code>archive</code> at <code>mount</code>. The path
   * specified by <code>mount</code> is required to refer to an existing
   * directory.
   * 
   * @param archive
   *          The archive to mount.
   * @param mount
   *          The mount point for the archive.
   * @throws ConstraintError
   *           Iff <code>archive == null || mount == null</code>.
   * @throws FilesystemError
   */

  void mount(
    final @Nonnull String archive,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError;

  /**
   * Mount the archive <code>archive</code> at <code>mount</code>. The path
   * specified by <code>mount</code> is required to refer to an existing
   * directory.
   * 
   * @param archive
   *          The archive to mount.
   * @param mount
   *          The mount point for the archive.
   * @throws ConstraintError
   *           Iff <code>archive == null || mount == null</code>.
   * @throws FilesystemError
   */

  void mount(
    final @Nonnull String archive,
    final @Nonnull String mount)
    throws ConstraintError,
      FilesystemError;

  /**
   * Opens the file referenced by <code>file</code>.
   * 
   * @param file
   *          The file path.
   * @throws ConstraintError
   *           Iff <code>file == null</code>.
   * @throws FilesystemError
   */

  @Nonnull InputStream openFile(
    final @Nonnull PathVirtual file)
    throws ConstraintError,
      FilesystemError;

  /**
   * Opens the file referenced by <code>file</code>.
   * 
   * @param file
   *          The file path.
   * @throws ConstraintError
   *           Iff <code>file == null</code>.
   * @throws FilesystemError
   */

  @Nonnull InputStream openFile(
    final @Nonnull String file)
    throws ConstraintError,
      FilesystemError;

  /**
   * Unmounts the most recently mounted archive at <code>mount</code>. If
   * another archive is mounted at a directory contained within the archive
   * that would be unmounted, the operation fails.
   * 
   * @param mount
   *          The mount point.
   * @throws ConstraintError
   *           Iff <code>mount == null</code>.
   * @throws FilesystemError
   */

  void unmount(
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError;

  /**
   * Unmounts the most recently mounted archive at <code>mount</code>. If
   * another archive is mounted at a directory contained within the archive
   * that would be unmounted, the operation fails.
   * 
   * @param mount
   *          The mount point.
   * @throws ConstraintError
   *           Iff <code>mount == null</code>.
   * @throws FilesystemError
   */

  void unmount(
    final @Nonnull String mount)
    throws ConstraintError,
      FilesystemError;
}
