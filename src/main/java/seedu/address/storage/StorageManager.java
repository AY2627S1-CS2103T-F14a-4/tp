package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * Manages storage of ClientBook data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonClientBookStorage clientBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;

    /**
     * Creates a {@code StorageManager} with the given client book and user prefs storage.
     */
    public StorageManager(JsonClientBookStorage clientBookStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.clientBookStorage = clientBookStorage;
        this.userPrefsStorage = userPrefsStorage;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ ClientBook methods ==============================

    @Override
    public Path getClientBookFilePath() {
        return clientBookStorage.getClientBookFilePath();
    }

    @Override
    public Optional<ReadOnlyClientBook> readClientBook() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + clientBookStorage.getClientBookFilePath());
        return clientBookStorage.readClientBook();
    }

    @Override
    public void saveClientBook(ReadOnlyClientBook clientBook) throws IOException {
        logger.fine("Attempting to write to data file: " + clientBookStorage.getClientBookFilePath());
        clientBookStorage.saveClientBook(clientBook);
    }

}
