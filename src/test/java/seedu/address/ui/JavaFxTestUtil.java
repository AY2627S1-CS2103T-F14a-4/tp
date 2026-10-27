package seedu.address.ui;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/** Provides shared JavaFX toolkit setup for UI tests. */
final class JavaFxTestUtil {

    private static boolean isToolkitStarted;

    private JavaFxTestUtil() {
    }

    static synchronized void startToolkit() throws Exception {
        if (isToolkitStarted) {
            return;
        }

        CompletableFuture<Void> started = new CompletableFuture<>();
        try {
            Platform.startup(() -> started.complete(null));
            started.get(10, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            // The toolkit may already have been started by another test class.
        }
        Platform.setImplicitExit(false);
        isToolkitStarted = true;
    }
}
