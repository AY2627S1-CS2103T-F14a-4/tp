package seedu.address;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    public void launchWithLock_availableLock_launchesAndReleasesLock() throws Exception {
        AtomicBoolean wasLaunched = new AtomicBoolean();

        Main.launchWithLock(temporaryFolder, () -> wasLaunched.set(true));

        assertTrue(wasLaunched.get());
        try (FileChannel channel = FileChannel.open(temporaryFolder.resolve("clientbook.lock"), CREATE, WRITE);
                FileLock lock = Main.tryAcquireLock(channel)) {
            assertNotNull(lock);
        }
    }

    @Test
    public void launchWithLock_unavailableLock_doesNotLaunch() throws Exception {
        AtomicBoolean wasLaunched = new AtomicBoolean();
        try (FileChannel channel = FileChannel.open(temporaryFolder.resolve("clientbook.lock"), CREATE, WRITE);
                FileLock lock = Main.tryAcquireLock(channel)) {
            Main.launchWithLock(temporaryFolder, () -> wasLaunched.set(true));
        }

        assertFalse(wasLaunched.get());
    }

    @Test
    public void launchWithLock_inaccessibleLockFile_doesNotLaunch() {
        AtomicBoolean wasLaunched = new AtomicBoolean();

        Main.launchWithLock(temporaryFolder.resolve("missing"), () -> wasLaunched.set(true));

        assertFalse(wasLaunched.get());
    }

    @Test
    public void launchWithLock_directoryResolutionFails_doesNotLaunch() {
        AtomicBoolean wasLaunched = new AtomicBoolean();

        Main.launchWithLock(() -> {
            throw new IOException("directory failure");
        }, () -> wasLaunched.set(true));

        assertFalse(wasLaunched.get());
    }

    @Test
    public void launchWithLock_directoryResolutionSucceeds_launches() {
        AtomicBoolean wasLaunched = new AtomicBoolean();

        Main.launchWithLock(() -> temporaryFolder, () -> wasLaunched.set(true));

        assertTrue(wasLaunched.get());
    }
}
