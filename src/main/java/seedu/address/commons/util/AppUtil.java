package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.scene.image.Image;
import seedu.address.MainApp;

/**
 * A container for app-specific utility functions
 */
public class AppUtil {

    /**
     * Gets an {@code Image} from the specified path.
     */
    public static Image getImage(String imagePath) {
        requireNonNull(imagePath);
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    /**
     * Checks that {@code condition} is true. Used for validating arguments to methods.
     *
     * @throws IllegalArgumentException if {@code condition} is false.
     */
    public static void checkArgument(Boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks that {@code condition} is true. Used for validating arguments to methods.
     *
     * @throws IllegalArgumentException with {@code errorMessage} if {@code condition} is false.
     */
    public static void checkArgument(Boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Returns the directory containing the application JAR, or the classes directory when running from an IDE.
     */
    public static Path getApplicationDirectory() throws URISyntaxException {
        Path applicationLocation = Path.of(MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return getApplicationDirectory(applicationLocation);
    }

    static Path getApplicationDirectory(Path applicationLocation) {
        requireNonNull(applicationLocation);
        return Files.isRegularFile(applicationLocation) ? applicationLocation.getParent() : applicationLocation;
    }
}
