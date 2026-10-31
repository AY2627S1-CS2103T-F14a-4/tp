package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void writeToFile_existingFile_replacesContentAndCleansTemporaryFile() throws IOException {
        Path dataFile = temporaryFolder.resolve("clientbook.json");
        Files.writeString(dataFile, "old content");

        FileUtil.writeToFile(dataFile, "new content");

        assertEquals("new content", Files.readString(dataFile));
        try (Stream<Path> files = Files.list(temporaryFolder)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    public void writeToFile_missingParents_createsFileWithContent() throws IOException {
        Path dataFile = temporaryFolder.resolve("data").resolve("clientbook.json");

        FileUtil.writeToFile(dataFile, "new content");

        assertEquals("new content", Files.readString(dataFile));
    }

    @Test
    public void replaceFile_atomicMoveUnsupported_usesNonAtomicReplacement() throws IOException {
        Path temporaryFile = Files.createTempFile(temporaryFolder, "new", ".tmp");
        Path targetFile = temporaryFolder.resolve("clientbook.json");
        List<CopyOption[]> moveOptions = new ArrayList<>();
        Files.writeString(temporaryFile, "new content");
        Files.writeString(targetFile, "old content");

        FileUtil.replaceFile(temporaryFile, targetFile, (source, target, options) -> {
            moveOptions.add(options);
            if (options[0] == StandardCopyOption.ATOMIC_MOVE) {
                throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "not supported");
            }
            Files.move(source, target, options);
        });

        assertEquals("new content", Files.readString(targetFile));
        assertFalse(Files.exists(temporaryFile));
        assertEquals(2, moveOptions.size());
        assertEquals(StandardCopyOption.ATOMIC_MOVE, moveOptions.get(0)[0]);
        assertEquals(StandardCopyOption.REPLACE_EXISTING, moveOptions.get(0)[1]);
        assertEquals(1, moveOptions.get(1).length);
        assertEquals(StandardCopyOption.REPLACE_EXISTING, moveOptions.get(1)[0]);
    }
}
