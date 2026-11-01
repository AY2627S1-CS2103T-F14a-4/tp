package seedu.address;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javafx.application.Application;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.AppUtil;
import seedu.address.commons.util.StringUtil;

/**
 * The main entry point to the application.
 *
 * This is a workaround for the following error when MainApp is made the
 * entry point of the application:
 *
 *     Error: JavaFX runtime components are missing, and are required to run this application
 *
 * The reason is that MainApp extends Application. In that case, the
 * LauncherHelper will check for the javafx.graphics module to be present
 * as a named module. We don't use JavaFX via the module system so it can't
 * find the javafx.graphics module, and so the launch is aborted.
 *
 * By having a separate main class (Main) that doesn't extend Application
 * to be the entry point of the application, we avoid this issue.
 */
public class Main {
    static final String MESSAGE_ALREADY_RUNNING =
            "ClientBook is already running from this folder. Close the other instance and try again.";
    static final String MESSAGE_LOCK_FAILURE =
            "ClientBook could not secure exclusive access to its data. Check that its folder is writable.";
    private static final String LOCK_FILE_NAME = "clientbook.lock";
    private static Logger logger = LogsCenter.getLogger(Main.class);

    public static void main(String[] args) {

        logger.warning("The warnings about a 'restricted method in java.lang.System' "
            + "and 'enabling native access' appearing below (if any) can be ignored.");
        launchWithLock(AppUtil::getApplicationDirectory, () -> Application.launch(MainApp.class, args));
    }

    /** Resolves the home folder before launching the application with its exclusive lock. */
    static void launchWithLock(Callable<Path> directoryProvider, Runnable launcher) {
        Path applicationDirectory;
        try {
            applicationDirectory = directoryProvider.call();
        } catch (Exception exception) {
            logger.severe(MESSAGE_LOCK_FAILURE + " " + StringUtil.getDetails(exception));
            return;
        }
        launchWithLock(applicationDirectory, launcher);
    }

    /** Launches the application while holding the exclusive lock for its home folder. */
    static void launchWithLock(Path applicationDirectory, Runnable launcher) {
        try (FileChannel channel = FileChannel.open(
                applicationDirectory.resolve(LOCK_FILE_NAME), CREATE, WRITE);
                FileLock lock = tryAcquireLock(channel)) {
            if (lock == null) {
                logger.severe(MESSAGE_ALREADY_RUNNING);
                return;
            }
            launcher.run();
        } catch (IOException exception) {
            logger.severe(MESSAGE_LOCK_FAILURE + " " + StringUtil.getDetails(exception));
        }
    }

    /** Attempts to acquire the process lock, returning null when another local channel already holds it. */
    static FileLock tryAcquireLock(FileChannel channel) throws IOException {
        try {
            return channel.tryLock();
        } catch (OverlappingFileLockException exception) {
            return null;
        }
    }
}
