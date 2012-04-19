package com.io7m.jvvfs;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FileReference.Type;
import com.io7m.jvvfs.FilesystemError.Code;

/**
 * The default implementation of the filesystem.
 */

public final class Filesystem implements FilesystemAPI
{
  private static enum MountCheck
  {
    MOUNT_ARCHIVE_DANGEROUSLY_AND_DIRECTLY,
    MOUNT_ARCHIVE_FILE_SAFELY_FROM_DIRECTORY
  }

  private static boolean archiveExists(
    final @Nonnull PathReal archive_real)
  {
    final File file = new File(archive_real.value);
    return file.exists();
  }

  private static boolean archiveNameUnsafe(
    final @Nonnull String archive_name)
  {
    return archive_name.contains("/") || archive_name.contains("..");
  }

  private final @Nonnull Log                                  log;
  private final @Nonnull PathReal                             archive_path;
  private final @Nonnull ArrayList<ArchiveHandler>            handlers;
  private final @Nonnull HashMap<PathVirtual, Stack<Archive>> mounts;

  private final @Nonnull TreeSet<PathVirtual>                 directories;

  public Filesystem(
    final @Nonnull Log log,
    final @Nonnull PathReal archives)
    throws ConstraintError,
      FilesystemError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "log"), "filesystem");
    this.archive_path =
      Constraints.constrainNotNull(archives, "archive path");

    final File archive_dir = new File(this.archive_path.value);
    if (archive_dir.isDirectory() == false) {
      throw FilesystemError.notDirectory(this.archive_path.value);
    }

    this.handlers = new ArrayList<ArchiveHandler>();
    this.handlers.add(new ArchiveHandlerDirectory(this.log));
    this.handlers.add(new ArchiveHandlerZip(this.log));

    this.mounts = new HashMap<PathVirtual, Stack<Archive>>();
    this.directories = new TreeSet<PathVirtual>();
    this.directories.add(new PathVirtual("/"));
  }

  private Archive archiveCurrentForMount(
    final @Nonnull PathVirtual mount)
    throws ConstraintError
  {
    for (final Entry<PathVirtual, Stack<Archive>> e : this.mounts.entrySet()) {
      final PathVirtual p = e.getKey();
      if (p.isParentOf(mount)) {
        final Stack<Archive> archives = e.getValue();
        assert archives != null;
        assert archives.size() > 0;
        return archives.peek();
      }
    }

    /* UNREACHABLE */
    assert false;
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.io7m.jvvfs.FilesystemAPI#createDirectory(com.io7m.jvvfs.PathVirtual)
   */

  private PathReal archiveReal(
    final @Nonnull String archive_name)
    throws ConstraintError
  {
    return new PathReal(this.archive_path.value
      + File.separatorChar
      + archive_name);
  }

  @Override public void createDirectory(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("create-directory " + path.toString());
    this.createDirectoryInternal(Constraints.constrainNotNull(path, "path"));
  }

  @Override public void createDirectory(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError
  {
    this.createDirectory(new PathVirtual(path));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.io7m.jvvfs.FilesystemAPI#isDirectory(com.io7m.jvvfs.PathVirtual)
   */

  private void createDirectoryInternal(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final PathVirtualEnum en = new PathVirtualEnum(path);

    while (en.hasMoreElements()) {
      final PathVirtual p = en.nextElement();

      try {
        final FileReference ref = this.lookupDirect(p);
        if (ref.type == Type.TYPE_DIRECTORY) {
          this.directories.add(p);
        } else {
          throw FilesystemError.notDirectory(p.toString());
        }
      } catch (final FilesystemError e) {
        if (e.code == Code.FS_ERROR_NONEXISTENT) {
          this.directories.add(p);
        } else {
          throw e;
        }
      }
    }
  }

  @Override public long fileSize(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    return this.fileSizeInternal(Constraints.constrainNotNull(path, "path"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.io7m.jvvfs.FilesystemAPI#isFile(com.io7m.jvvfs.PathVirtual)
   */

  @Override public long fileSize(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError
  {
    return this.fileSize(new PathVirtual(path));
  }

  private long fileSizeInternal(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final FileReference file = this.lookup(path);
    if (file.type == Type.TYPE_DIRECTORY) {
      throw FilesystemError.isDirectory(path.toString());
    }

    return file.archive.fileSize(path);
  }

  private ArchiveHandler handlerFor(
    final @Nonnull PathReal path)
    throws FilesystemError
  {
    for (final ArchiveHandler handler : this.handlers) {
      if (handler.canHandle(path)) {
        return handler;
      }
    }
    throw FilesystemError.unhandledType(path.value);
  }

  @Override public boolean isDirectory(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("is-directory " + path.toString());
    return this.isDirectoryInternal(Constraints
      .constrainNotNull(path, "path"));
  }

  @Override public boolean isDirectory(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError
  {
    return this.isDirectory(new PathVirtual(path));
  }

  private boolean isDirectoryInternal(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    try {
      final FileReference ref = this.lookup(path);
      return ref.type == Type.TYPE_DIRECTORY;
    } catch (final FilesystemError e) {
      if (e.code != Code.FS_ERROR_NONEXISTENT) {
        throw e;
      }
      return false;
    }
  }

  @Override public boolean isFile(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("is-file " + path.toString());
    return this.isFileInternal(Constraints.constrainNotNull(path, "path"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.io7m.jvvfs.FilesystemAPI#mount(java.lang.String,
   * com.io7m.jvvfs.PathVirtual)
   */

  @Override public boolean isFile(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError
  {
    return this.isFile(new PathVirtual(path));
  }

  private boolean isFileInternal(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    try {
      final FileReference ref = this.lookup(path);
      return ref.type == Type.TYPE_FILE;
    } catch (final FilesystemError e) {
      if (e.code != Code.FS_ERROR_NONEXISTENT) {
        throw e;
      }
      return false;
    }
  }

  private boolean isMounted(
    final @Nonnull PathVirtual mount)
  {
    return this.mounts.containsKey(mount);
  }

  private boolean isMountedArchive(
    final @Nonnull PathVirtual mount,
    final @Nonnull PathReal archive_real)
  {
    if (this.mounts.containsKey(mount)) {
      final Stack<Archive> archives = this.mounts.get(mount);
      assert archives != null;

      for (final Archive a : archives) {
        if (a.getRealPath().equals(archive_real)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override public void listDirectory(
    final @Nonnull PathVirtual path,
    final @Nonnull TreeSet<String> items)
    throws ConstraintError,
      FilesystemError
  {
    this.listDirectoryInternal(
      Constraints.constrainNotNull(path, "path"),
      Constraints.constrainNotNull(items, "items"));
  }

  @Override public void listDirectory(
    final @Nonnull String path,
    final @Nonnull TreeSet<String> items)
    throws ConstraintError,
      FilesystemError
  {
    this.listDirectory(new PathVirtual(path), items);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.io7m.jvvfs.FilesystemAPI#unmount(com.io7m.jvvfs.PathVirtual)
   */

  private void listDirectoryInternal(
    final @Nonnull PathVirtual path,
    final @Nonnull TreeSet<String> items)
    throws ConstraintError,
      FilesystemError
  {
    final FileReference ref = this.lookup(path);
    if (ref.type != Type.TYPE_DIRECTORY) {
      throw FilesystemError.notDirectory(path.toString());
    }

    for (final Entry<PathVirtual, Stack<Archive>> ent : this.mounts
      .entrySet()) {
      final PathVirtual mount = ent.getKey();
      if (mount.isParentOf(path)) {
        final Stack<Archive> stack = ent.getValue();
        final int size = stack.size();
        assert size > 0;
        for (int index = size - 1; index >= 0; --index) {
          final Archive a = stack.get(index);
          try {
            a.listDirectory(path.subtract(mount), items);
          } catch (final FilesystemError e) {
            if (e.code != Code.FS_ERROR_NONEXISTENT) {
              throw e;
            }
          }
        }
      }
    }

    for (final PathVirtual dir : this.directories) {
      if (dir.isRoot() == false) {
        if (dir.parent().equals(path)) {
          items.add(dir.baseName());
        }
      }
    }
  }

  private Archive loadArchive(
    final @Nonnull PathReal archive_real,
    final @Nonnull PathVirtual mount,
    final @CheckForNull Archive parent)
    throws ConstraintError,
      FilesystemError
  {
    final ArchiveHandler handler = this.handlerFor(archive_real);
    if (parent != null) {
      return handler.loadWithParent(parent, archive_real, mount);
    }
    return handler.load(archive_real, mount);
  }

  private @Nonnull FileReference lookup(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    final PathVirtualEnum e = new PathVirtualEnum(path);

    while (e.hasMoreElements()) {
      final PathVirtual p = e.nextElement();
      if (p.equals(path)) {
        break;
      }

      final FileReference ref = this.lookupDirect(p);
      if (ref.type != Type.TYPE_DIRECTORY) {
        throw FilesystemError.notDirectory(p.toString());
      }
    }

    return this.lookupDirect(path);
  }

  private @Nonnull FileReference lookupDirect(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    for (final Entry<PathVirtual, Stack<Archive>> entry : this.mounts
      .entrySet()) {
      final PathVirtual mount = entry.getKey();
      if (mount.isParentOf(path)) {
        final Stack<Archive> archives = entry.getValue();
        assert archives != null;
        assert archives.size() > 0;

        final int size = archives.size();
        for (int index = size - 1; index >= 0; --index) {
          final Archive a = archives.get(index);
          try {
            return a.lookup(path.subtract(mount));
          } catch (final FilesystemError e) {
            if (e.code != Code.FS_ERROR_NONEXISTENT) {
              throw e;
            }
          }
        }
      }
    }

    for (final PathVirtual dir : this.directories) {
      if (dir.equals(path)) {
        return new FileReference(null, path, Type.TYPE_DIRECTORY);
      }
    }

    throw FilesystemError.fileNotFound(path.toString());
  }

  @Override public long modificationTime(
    final @Nonnull PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    return this.modificationTimeInternal(Constraints.constrainNotNull(
      path,
      "path"));
  }

  @Override public long modificationTime(
    final @Nonnull String path)
    throws ConstraintError,
      FilesystemError
  {
    return this.modificationTime(new PathVirtual(path));
  }

  private long modificationTimeInternal(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    final FileReference ref = this.lookup(path);
    if (ref.type == Type.TYPE_FILE) {
      return ref.archive.modificationTime(path);
    }
    throw FilesystemError.isDirectory(path.toString());
  }

  @Override public void mount(
    final @Nonnull String archive,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("mount " + archive + " " + mount.toString());
    this.mountInternal(
      Constraints.constrainNotNull(archive, "archive"),
      MountCheck.MOUNT_ARCHIVE_FILE_SAFELY_FROM_DIRECTORY,
      Constraints.constrainNotNull(mount, "mount point"));
  }

  @Override public void mount(
    final @Nonnull String archive,
    final @Nonnull String mount)
    throws ConstraintError,
      FilesystemError
  {
    this.mount(archive, new PathVirtual(mount));
  }

  private void mountArchive(
    final @Nonnull Archive archive)
  {
    final PathVirtual path = archive.getMountPath();
    final Stack<Archive> archives =
      this.mounts.containsKey(path)
        ? this.mounts.get(path)
        : new Stack<Archive>();
    assert archives != null;

    archives.push(archive);
    this.mounts.put(path, archives);
  }

  private void mountInternal(
    final @Nonnull String archive_name,
    final @Nonnull MountCheck check,
    final @Nonnull PathVirtual mount)
    throws FilesystemError,
      ConstraintError
  {
    PathReal archive_real = null;

    if (check == MountCheck.MOUNT_ARCHIVE_FILE_SAFELY_FROM_DIRECTORY) {
      if (Filesystem.archiveNameUnsafe(archive_name)) {
        throw FilesystemError.fileNotFound(archive_name);
      }
      archive_real = this.archiveReal(archive_name);
    } else if (check == MountCheck.MOUNT_ARCHIVE_DANGEROUSLY_AND_DIRECTLY) {
      archive_real = new PathReal(archive_name);
    }

    assert archive_real != null;

    if (Filesystem.archiveExists(archive_real) == false) {
      throw FilesystemError.fileNotFound(archive_real.toString());
    }
    if (this.isMountedArchive(mount, archive_real)) {
      throw FilesystemError.duplicateMount(archive_name, mount.toString());
    }
    if (this.isDirectory(mount) == false) {
      throw FilesystemError.notDirectory(mount.toString());
    }

    if (this.mountNotManuallyCreated(mount)) {
      final Archive parent = this.archiveCurrentForMount(mount);
      final Archive archive = this.loadArchive(archive_real, mount, parent);
      this.mountArchive(archive);
    } else {
      final Archive archive = this.loadArchive(archive_real, mount, null);
      this.mountArchive(archive);
    }
  }

  private boolean mountIsBusy(
    final @Nonnull PathVirtual mount)
    throws ConstraintError
  {
    final Archive current = this.archiveCurrentForMount(mount);

    for (final Entry<PathVirtual, Stack<Archive>> en : this.mounts.entrySet()) {
      final PathVirtual p = en.getKey();
      if (mount.isParentOf(p) || mount.equals(p)) {
        final Stack<Archive> archives = en.getValue();
        for (final Archive a : archives) {
          final Archive parent = a.getParent();
          if (parent != null) {
            if (current == parent) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  private boolean mountNotManuallyCreated(
    final @Nonnull PathVirtual mount)
  {
    return this.directories.contains(mount) == false;
  }

  /**
   * Mount the archive file that contains class <code>c</code>. This is a
   * slightly dangerous function that should be used with extreme caution: it
   * may result in surprises when a class is not where the developer expects
   * it to be.
   * 
   * @param c
   *          The class.
   * @param mount
   *          The path at which to mount the resulting archive.
   * @throws ConstraintError
   *           Iff any of the following conditions hold:
   *           <ul>
   *           <li><code>c == null</code></li>
   *           <li><code>mount == null</code></li>
   *           </ul>
   * @throws FilesystemError
   *           Iff a filesystem error occurs. This error may occur if the
   *           given class is within a container that <code>jvvfs</code> does
   *           not know how to load.
   */

  public void mountUnsafeClasspathItem(
    final @Nonnull Class<?> c,
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    Constraints.constrainNotNull(c, "Class");
    final String cname = c.getCanonicalName();
    Constraints.constrainNotNull(cname, "Class canonical name");

    final String cname_s = cname.replace('.', '/');
    final String cname_k = cname_s + ".class";

    final ClassLoader loader = c.getClassLoader();
    final URL url = loader.getResource(cname_k);

    if (url.getProtocol().equals("file")) {
      final String real_path = url.getPath();
      final String mount_path =
        real_path.substring(0, real_path.length() - cname_k.length());

      this.log.info("mount-classpath-file : " + mount_path);

      this.mountInternal(
        mount_path,
        MountCheck.MOUNT_ARCHIVE_DANGEROUSLY_AND_DIRECTLY,
        mount);
    } else if (url.getProtocol().equals("jar")) {
      final String real_path = url.getPath();

      /*
       * Path is of the form "file:/x/y/z.jar!/path/to/file.class"
       */

      final String file_path =
        real_path.substring(0, real_path.length() - (cname_k.length() + 2));
      final String mount_path = file_path.replaceFirst("^file:", "");

      this.log.info("mount-classpath-jar : " + mount_path);

      this.mountInternal(
        mount_path,
        MountCheck.MOUNT_ARCHIVE_DANGEROUSLY_AND_DIRECTLY,
        mount);
    } else {
      throw new FilesystemError(
        Code.FS_ERROR_UNHANDLED_TYPE,
        "Cannot mount whatever is holding this classpath item");
    }
  }

  @Override public @Nonnull InputStream openFile(
    final PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("open-file " + path);
    return this.openFileInternal(Constraints.constrainNotNull(path, "path"));
  }

  @Override public InputStream openFile(
    final @Nonnull String file)
    throws ConstraintError,
      FilesystemError
  {
    return this.openFile(new PathVirtual(file));
  }

  private @Nonnull InputStream openFileInternal(
    final PathVirtual path)
    throws ConstraintError,
      FilesystemError
  {
    final FileReference ref = this.lookup(path);
    if (ref.type == Type.TYPE_FILE) {
      return ref.archive.openFile(path);
    }
    throw FilesystemError.isDirectory(path.toString());
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("Filesystem [\n");

    final Set<PathVirtual> dirs = new TreeSet<PathVirtual>();
    for (final PathVirtual p : this.directories) {
      dirs.add(p);
    }
    for (final PathVirtual p : this.mounts.keySet()) {
      dirs.add(p);
    }

    for (final PathVirtual p : dirs) {
      if (this.mounts.containsKey(p)) {
        final Stack<Archive> archives = this.mounts.get(p);
        for (final Archive a : archives) {
          builder.append("  ");
          builder.append(p.toString());
          builder.append(" ");
          builder.append(a.toString());
          builder.append("\n");
        }
      } else {
        builder.append("  ");
        builder.append(p.toString());
        builder.append("\n");
      }
    }
    builder.append("]");
    return builder.toString();
  }

  @Override public void unmount(
    final @Nonnull PathVirtual mount)
    throws ConstraintError,
      FilesystemError
  {
    this.log.info("unmount " + mount.toString());
    this.unmountInternal(Constraints.constrainNotNull(mount, "mount point"));
  }

  @Override public void unmount(
    final @Nonnull String mount)
    throws ConstraintError,
      FilesystemError
  {
    this.unmount(new PathVirtual(mount));
  }

  private void unmountArchive(
    final @Nonnull Archive archive)
    throws FilesystemError
  {
    final PathVirtual path = archive.getMountPath();
    final Stack<Archive> archives = this.mounts.get(path);
    assert archives != null;
    assert archives.size() > 0;

    final Archive a = archives.pop();
    assert a == archive;

    if (archives.size() == 0) {
      this.mounts.remove(path);
    }

    a.close();
  }

  private void unmountInternal(
    final @Nonnull PathVirtual mount)
    throws FilesystemError,
      ConstraintError
  {
    if (this.isMounted(mount) == false) {
      throw FilesystemError.notMounted(mount.toString());
    }
    if (this.mountIsBusy(mount)) {
      throw FilesystemError.busy(mount.toString());
    }

    final Archive archive = this.archiveCurrentForMount(mount);
    this.unmountArchive(archive);
  }
}
