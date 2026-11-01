package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    @FunctionalInterface
    interface FileMover {
        void move(Path source, Path target, CopyOption... options) throws IOException;
    }

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes given string to a file.
     * Creates missing parent directories and writes to a temporary file in the target directory before replacing
     * the target. This prevents a failed write from partially overwriting an existing file. If the file system does
     * not support atomic moves, the replacement falls back to a normal move.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Path absoluteFile = file.toAbsolutePath();
        Path parentDirectory = absoluteFile.getParent();
        createParentDirsOfFile(absoluteFile);

        String temporaryFilePrefix = absoluteFile.getFileName().toString();
        if (temporaryFilePrefix.length() < 3) {
            temporaryFilePrefix = "tmp";
        }
        Path temporaryFile = Files.createTempFile(parentDirectory, temporaryFilePrefix, ".tmp");
        try {
            Files.write(temporaryFile, content.getBytes(CHARSET));
            replaceFile(temporaryFile, absoluteFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static void replaceFile(Path temporaryFile, Path targetFile) throws IOException {
        replaceFile(temporaryFile, targetFile, Files::move);
    }

    static void replaceFile(Path temporaryFile, Path targetFile, FileMover fileMover) throws IOException {
        try {
            fileMover.move(temporaryFile, targetFile, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            fileMover.move(temporaryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
