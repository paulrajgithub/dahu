package io.dahuapp.driver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * FileSystem driver.
 *
 * Note: Filesystem driver must not make use of User Interface.
 * Filesystem driver's methods must only concern low level
 * functionalities. User interactions must be implemented in Kernel modules.
 */
public class FileSystemDriver {

    /**
     * Indicates if a specified file or directory exists.
     *
     * @param pathname A pathname string.
     * @return {@code true} if the file or directory exists; {@code false} otherwise.
     */
    static public boolean exists(String pathname) {
        File fileOrDir = new File(pathname);
        return fileOrDir.exists();
    }

    /**
     * Indicates if a specified pathname is a directory.
     *
     * @param pathname A pathname string.
     * @return {@code true} if the name is a directory; {@code false} otherwise.
     */
    static public boolean isDirectory(String pathname) {
        File dir = new File(pathname);
        return dir.isDirectory();
    }

    /**
     * Creates a directory by its abstract pathname.
     *
     * @param pathname A pathname.
     * @return True only if the directory was created.
     */
    static public boolean mkdir(String pathname) {
        File dir = new File(pathname);
        return dir.mkdirs();
    }

    /**
     * Delete a file from its pathname.
     *
     * @param pathname A pathname string.
     * @return  {@code true} if and only if the file or directory is
     *          successfully deleted; {@code false} otherwise
     */
    static public boolean delete(String pathname) {
        File file = new File(pathname);
        return file.delete();
    }

    /**
     * Deletes the directory denoted by this abstract pathname.  Unless recursive
     * is set to {@code true} the directory must be empty in order to be deleted.
     *
     * @param pathname A pathname string.
     * @param recursive True if recursive, False otherwise
     * @return  {@code true} if and only if the file or directory is
     *          successfully deleted; {@code false} otherwise.
     */
    static public boolean rmdir(String pathname, boolean recursive) {
        File dir = new File(pathname);
        return rmdir(dir, recursive);
    }

    /**
     * Deletes a directory. Unless recursive is set to <code>true</code>
     * the directory must be empty in order to be deleted.
     *
     * @param dir A directory.
     * @param recursive True if recursive, False otherwise
     * @return  {@code true} if and only if the file or directory is
     *          successfully deleted; {@code false} otherwise.
     */
    static public boolean rmdir(File dir, boolean recursive) {
        if(recursive) {
            boolean result = true;
            for (File fileOrDir : dir.listFiles()) {
                if(fileOrDir.isDirectory()) {
                    result = result && rmdir(fileOrDir, recursive);
                } else {
                    result = result && fileOrDir.delete();
                }
            }
            return result && dir.delete();
        } else {
            return dir.delete();
        }
    }

    /**
     * Writes content to a file. If the file does not exist it is
     * created.
     *
     * @param filename The name of the file to write.
     * @param content The content to be written.
     * @return {@code true} if content was successfully written;
     *         {@code false} otherwise.
     */
    static public boolean writeToFile(String filename, String content) {
        try {
            FileUtils.writeStringToFile(new File(filename), content);
            return true;
        } catch (IOException ex) {
            LoggerDriver.error("Unable to write content to {}.", filename);
            return false;
        }
    }

    /**
     * Reads and returns the content of a file.
     *
     * @param filename
     * @return {@code null} if unable to read the file; the file
     *         content otherwise.
     */
    static public String readFromFile(String filename) {
        try {
            return FileUtils.readFileToString(new File(filename));
        } catch (IOException ex) {
            LoggerDriver.error("Unable to read content from {}.", filename);
            return null;
        }
    }

    /**
     *  Copies a file to a new location.
     *
     * @param source an existing filename to copy, must not be {@code null}
     * @param destination the new filename, must not be {@code null}
     * @return {@code true} if the copy was successful; {@code false} otherwise.
     */
    static public boolean copyFile(String source, String destination) {
        try {
            FileUtils.copyFile(new File(source), new File(destination));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     *  Copies a directory to a new location.
     *
     * @param source an existing directory name to copy, must not be {@code null}
     * @param destination the new directory name, must not be {@code null}
     * @return {@code true} if the copy was successful; {@code false} otherwise.
     */
    static public boolean copyDir(String source, String destination) {
        try {
            FileUtils.copyDirectory(new File(source), new File(destination));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Copy a resource from the classpath to a new location.
     *
     * @param jarFilePath Path to the jar which contain the resource to copy.
     * @param resource Resource name to copy
     * @param destination A destination path
     * @return {@code true} if the copy was successful: {@code false} otherwise.
     */
    static public boolean copyResourceDir(String jarFilePath, String resource, String destination) {
        try {
            // access Jar
            final JarFile jarFile = new JarFile(jarFilePath);

            // iterate over resources and copy only those
            // in the *resource* directory.
            jarFile.stream().parallel().
                    filter(entry -> Paths.get(entry.getName()).startsWith(resource)).
                    forEach(entry -> {
                                File dstFile = Paths.get(
                                        destination,
                                        Paths.get(entry.getName().substring(resource.length())).toString()).toFile();
                                if (!entry.isDirectory()) {
                                    try {
                                        InputStream is = jarFile.getInputStream(entry);
                                        FileUtils.copyInputStreamToFile(is, dstFile);
                                    } catch (Exception ex) {
                                        LoggerDriver.error("Error while writing resource {}.", dstFile.getName());
                                    }
                                }
                            }
                    );

            // we are done!
            return true;
        } catch (Exception ex) {
            LoggerDriver.error("Resource {} does not exist in {}.", resource, jarFilePath);
        }

        return false;
    }

    /**
     * Normalize a `filename` using Unix Separator.
     *
     * @param filename Filename to normalize.
     * @return Normalized filename.
     */
    static public String normalize(String filename) {
        return FilenameUtils.normalize(filename, /* unixSeparator */ true);
    }
}
