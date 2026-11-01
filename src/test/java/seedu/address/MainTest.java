package seedu.address;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MainTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void tryAcquireLock_preventsOverlapAndAllowsAcquisitionAfterRelease() throws Exception {
        Path lockFile = temporaryFolder.resolve("clientbook.lock");
        try (FileChannel firstChannel = FileChannel.open(lockFile, CREATE, WRITE);
                FileChannel secondChannel = FileChannel.open(lockFile, CREATE, WRITE)) {
            try (FileLock firstLock = Main.tryAcquireLock(firstChannel)) {
                assertNotNull(firstLock);
                assertNull(Main.tryAcquireLock(secondChannel));
            }
            try (FileLock secondLock = Main.tryAcquireLock(secondChannel)) {
                assertNotNull(secondLock);
            }
        }
    }
}
