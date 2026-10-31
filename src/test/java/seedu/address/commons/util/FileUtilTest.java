package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
