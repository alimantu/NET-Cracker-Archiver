package com.netcracker.edu.salinskii.archiver;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class should be used for *.zip files manipulations.
 * With help of it you could create new archives, add some information to the existing or just uncompress some of them.
 * Created by Alimantu on 22/11/15.
 *
 * @author Alimantu
 */
public class Archiver {
    /**
     * This field is sets the size of the used buffer for files manipulation operations.
     */
    private final int BUFF_SIZE = 1024;
    /**
     * This constant will be used for setting the default comment during creation of the archives if user
     * didn't like to specify them himself.
     */
    private final String DEFAULT_COMMENT = "";

    /**
     * Simple method for getting recursively all the files that contains the dir's list.
     *
     * @param dirs list of the directories and files.
     * @return list with all files from dirs and Files found in the directories from dirs.
     */
    private ArrayList<File> getFileList(final File... dirs) {
        ArrayList<File> files = new ArrayList<>();
        for (final File dir : dirs) {
            if (dir.isDirectory()) {
                for (final File file : dir.listFiles()) {
                    if (file.isDirectory()) {
                        files.addAll(getFileList(file));
                    } else if (!checkSystemFiles(dir.getName())) {
                        files.add(file);
                    }
                }
            } else {
                if (!checkSystemFiles(dir.getName())) {
                    files.add(dir);
                }
            }
        }
        return files;
    }

    /**
     * Simple checker for system files. (Currently specified only for OS X)
     *
     * @param fileName name of the checked file.
     * @return <tt>true</tt> if checked file is system file <tt>false</tt> otherwise.
     */
    private boolean checkSystemFiles(String fileName) {
        return fileName.contains("__MACOSX")
                || fileName.contains(".DS_Store");
    }

    /**
     * Adding the files to the specified <tt>ZipOutputStream</tt>.
     *
     * @param zipOutputStream stream for adding files.
     * @param names           list of the files, that are currently in <tt>ZipOutputStream</tt>.
     * @param files           vararg of the files needed to be added to the <tt>ZipOutputStream</tt>.
     */
    private void addFilesToStream(ZipOutputStream zipOutputStream, Set<String> names, File... files) {
        try {
            for (File file : getFileList(files)) {
                FileInputStream fileInputStream = new FileInputStream(file);
                ZipEntry zipEntry = new ZipEntry(getNewName(file.getPath(), names));

                putEntry(zipOutputStream, zipEntry, fileInputStream);

                fileInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates new unique name for the file.
     *
     * @param name  default name.
     * @param names <tt>Set</tt> for searching for name collisions.
     * @return new unique name of the file.
     */
    private String getNewName(String name, Set<String> names) {
        String tmpName = name;
        String tmpPath = "";
        String result = name;
        if (name.contains("/")) {
            tmpName = name.substring(name.lastIndexOf("/") + 1);
            tmpPath = name.substring(0, name.lastIndexOf("/") + 1);
        }
        while (names.contains(result)) {
            tmpName = "New_" + tmpName;
            result = tmpPath + tmpName;
        }
        names.add(result);
        return result;
    }

    /**
     * Simplification of the method {@link Archiver#addFilesToArchive(String, String, String...)} uses
     * {@link Archiver#DEFAULT_COMMENT} as comment.
     *
     * @param archivePath path of the output archive file.
     * @param paths       varargs of the paths of the files should be added to the archive.
     * @throws IllegalAccessException if any of the parameters equals to null.
     */
    public void simpleAddFilesToArchive(String archivePath, String... paths)
            throws IllegalAccessException {
        addFilesToArchive(archivePath, DEFAULT_COMMENT, paths);
    }

    /**
     * Add to the existing, or creates new archive and add new files to it.
     *
     * @param archivePath path of the output archive file.
     * @param comment     comment, that should be added to the resulting zip file.
     * @param paths       varargs of the paths of the files should be added to the archive.
     * @throws IllegalAccessException if any of the parameters equals to null.
     */
    public void addFilesToArchive(String archivePath, String comment, String... paths)
            throws IllegalAccessException {
        checkInput(comment, "String comment");
        checkInput(archivePath, "String archive");
        checkInput(paths, "String... paths");
        File archive = new File(archivePath);
        ArrayList<File> files = new ArrayList<>();
        for (String path : paths) {
            files.add(new File(path));
        }
        addFilesToArchive(archive, comment, files.toArray(new File[files.size()]));
    }

    /**
     * Simplification of the {@link Archiver#copyArchive(File, Set, String)} method uses
     * {@link Archiver#DEFAULT_COMMENT} as comment.
     *
     * @param from  input file should to be copied.
     * @param names <tt>Set</tt> with all previous used names, that would be added with names of the
     *              new added files during the work of this method.
     * @return <tt>ZipOutputStream</tt> with all files copied from the input <tt>File</tt>.
     * @throws IOException            if there are some troubles during usage of the I/O streams.
     * @throws IllegalAccessException if any of the arguments is equal to null.
     */
    private ZipOutputStream copyArchive(File from, Set<String> names)
            throws IOException, IllegalAccessException {
        return copyArchive(from, names, DEFAULT_COMMENT);
    }

    /**
     * Simplification of the {@link Archiver#copyArchive(File, Set, String)} for situations when we don't need
     * names of the files inside the archive.
     *
     * @param from    input file should to be copied.
     * @param comment comment, that should be added to the archive.
     * @return <tt>ZipOutputStream</tt> with all files copied from the input <tt>File</tt>.
     * @throws IOException            if there are some troubles during usage of the I/O streams.
     * @throws IllegalAccessException if any of the arguments is equal to null.
     */
    private ZipOutputStream copyArchive(File from, String comment)
            throws IOException, IllegalAccessException {
        return copyArchive(from, new HashSet<>(), comment);
    }

    /**
     * Copies <tt>File</tt> to the <tt>ZipOutputStream</tt>, adding the used names to the names <tt>Set</tt> and
     * sets the comment of the stream to the specified value.
     *
     * @param from    input file should to be copied.
     * @param names   <tt>Set</tt> with all previous used names, that would be added with names of the
     *                new added files during the work of this method.
     * @param comment comment, that should be added to the archive.
     * @return <tt>ZipOutputStream</tt> with all files copied from the input <tt>File</tt>.
     * @throws IOException            if there are some troubles during usage of the I/O streams.
     * @throws IllegalAccessException if any of the arguments is equal to null.
     */
    private ZipOutputStream copyArchive(File from, Set<String> names, String comment)
            throws IOException, IllegalAccessException {
        File tmpFile = File.createTempFile(from.getName(), null);
        ZipOutputStream result = new ZipOutputStream(new FileOutputStream(tmpFile));
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(from));
        result.setComment(comment);

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            names.add(entry.getName());
            putEntry(result, entry, zipInputStream);
        }
        zipInputStream.close();
        String name = from.getAbsolutePath();
        from.delete();
        if (!tmpFile.renameTo(new File(name))) {
            throw new IllegalAccessException("Can't get create the "
                    + from.getAbsolutePath() + " file");
        }
        return result;
    }

    /**
     * Add files to the existing archive, or creates new one with them.
     *
     * @param archive archive <tt>File</tt>, that should be used for adding new files.
     * @param comment comment, that should be added to the archive.
     * @param files   files, that should be compressed and added to the specified archive file.
     * @throws IllegalAccessException if any of the arguments is equal to null.
     */
    public void addFilesToArchive(File archive, String comment, File... files)
            throws IllegalAccessException {
        checkInput(archive, "File archive");
        checkInput(files, "File... files");
        try {
            File tmpFile = archive;
            Set<String> names = new HashSet<>();
            ZipOutputStream zipOutputStream = archive.exists()
                    ? copyArchive(archive, names, comment)
                    : new ZipOutputStream(new FileOutputStream(tmpFile));
            addFilesToStream(zipOutputStream, names, files);
            zipOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Puts new <tt>ZipEntry</tt> to the specified <tt>ZipOutputStream</tt>.
     *
     * @param zipOutputStream stream for adding new entry.
     * @param entry           added enrty.
     * @param zipInputStream  input stream with bytes of the specified entry.
     * @throws IOException if there are some troubles during work with I/O streams.
     */
    private void putEntry(ZipOutputStream zipOutputStream, ZipEntry entry, InputStream zipInputStream)
            throws IOException {
        zipOutputStream.putNextEntry(entry);
        putFile(zipOutputStream, zipInputStream);
        zipOutputStream.closeEntry();
    }

    /**
     * Adds the bytes of the file from <tt>InputStream</tt> to the <tt>OutputStream</tt>.
     *
     * @param outputStream <tt>OutputStream</tt> for adding bytes of the file.
     * @param inputStream  <tt>InputStream</tt> source for the bytes of the file.
     * @throws IOException if there are some troubles during work with I/O streams.
     */
    private void putFile(OutputStream outputStream, InputStream inputStream)
            throws IOException {
        byte[] buff = new byte[BUFF_SIZE];
        int len;
        while ((len = inputStream.read(buff)) > 0) {
            outputStream.write(buff, 0, len);
        }
    }

    /**
     * Simple checker of the specified <tt>Object</tt> to the equality to <tt>null</tt>.
     *
     * @param object    <tt>Object</tt> need to be checked.
     * @param objDeclar Some additional information for throwing <tt>IllegalArgumentException</tt>.
     */
    private void checkInput(Object object, String objDeclar) {
        if (object == null) {
            throw new IllegalArgumentException("Expected " + objDeclar + " value, but found null!");
        }
    }

    /**
     * Uncompressed the specified archive to the specified path.
     *
     * @param archivePath archive should be uncompressed.
     * @param path        output destination.
     */
    public void getFilesFromArchive(String archivePath, String path) {
        checkInput(archivePath, "String archivePath");
        getFilesFromArchive(new File(archivePath), path);
    }

    /**
     * Simplification of the {@link Archiver#getFilesFromArchive(String, String)} used input file name
     * shorted by the extension as output destination.
     *
     * @param archivePath archive should be uncompressed.
     */
    public void getFilesFromArchive(String archivePath) {
        checkInput(archivePath, "String archivePath");
        getFilesFromArchive(new File(archivePath));
    }

    /**
     * Simplification of the {@link Archiver#getFilesFromArchive(File, String)} used input file name
     * shorted by the extension as output destination.
     *
     * @param file archive should be uncompressed.
     */
    public void getFilesFromArchive(File file) {
        checkInput(file, "File file");
        getFilesFromArchive(file, file.getPath().substring(0,
                file.getPath().lastIndexOf(".")));
    }

    /**
     * Uncompresses the archive file to the specified path.
     *
     * @param file archive should be uncompressed.
     * @param path output destination.
     */
    public void getFilesFromArchive(File file, String path) {
        checkInput(file, "File file");
        checkInput(path, "String path");
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
            File folder = new File(path);
            if (!folder.exists()) {
                folder.mkdir();
            }

            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (checkSystemFiles(zipEntry.getName())) {
                    continue;
                }
                File newFile = new File(path + File.separator + zipEntry.getName());
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fileOutputStream = new FileOutputStream(newFile);

                putFile(fileOutputStream, zipInputStream);
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the comment of the existing archive specified by archiveName to the given comment.
     * Could have some troubles when used in the same place with other methods of the class with the same archive file.
     *
     * @param archiveName name of the destination archive.
     * @param comment     comment should be set.
     */
    public void setComment(String archiveName, String comment) {
        checkInput(archiveName, "String archiveName");
        setComment(new File(archiveName), comment);
    }

    /**
     * Sets the comment of the existing archive to the given comment.
     * Could have some troubles when used in the same place with other methods of the class with the same archive file.
     *
     * @param archive destination archive.
     * @param comment comment should be set.
     */
    public void setComment(File archive, String comment) {
        checkInput(archive, "File archive");
        checkInput(comment, "String comment");
        if (!archive.exists()) {
            throw new IllegalArgumentException("Can't find " + archive.getAbsolutePath() + " file");
        }
        try (ZipOutputStream zipOutputStream = copyArchive(archive, comment)) {
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple getter of the comment from the archive specified by archiveName.
     *
     * @param archiveName name of the archive for getting comment.
     * @return <tt>String</tt> value of the comment of the archive if it's exists.
     */
    public String getComment(String archiveName) {
        checkInput(archiveName, "String archiveName");
        return getComment(new File(archiveName));
    }

    /**
     * Simple getter of the comment from the specified archive.
     *
     * @param archive archive for getting comment.
     * @return <tt>String</tt> value of the comment of the archive if it's exists.
     */
    public String getComment(File archive) {
        checkInput(archive, "File archive");
        if (!archive.exists()) {
            throw new IllegalArgumentException("Can't find " + archive.getAbsolutePath() + " file");
        }
        String result = DEFAULT_COMMENT;
        try {
            result = new ZipFile(archive).getComment();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
