package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * API of the Storage component
 */
public interface Storage {

    /**
     * Returns the file path of the UserPrefs data file.
     */
    Path getUserPrefsFilePath();

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    Optional<UserPrefs> readUserPrefs() throws DataLoadingException;

    /**
     * Saves the given {@link seedu.address.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException;

    /**
     * Returns the file path of the ClientBook data file.
     */
    Path getClientBookFilePath();

    /**
     * Returns ClientBook data as a {@link ReadOnlyClientBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    Optional<ReadOnlyClientBook> readClientBook() throws DataLoadingException;

    /**
     * Returns ClientBook data from the specified file path.
     * Returns {@code Optional.empty()} if the file is not found.
     *
     * @param filePath location of the data file.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    Optional<ReadOnlyClientBook> readClientBook(Path filePath) throws DataLoadingException;

    /**
     * Saves the given {@link ReadOnlyClientBook} to the storage.
     * @param clientBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveClientBook(ReadOnlyClientBook clientBook) throws IOException;

    /**
     * Saves the given {@link ReadOnlyClientBook} to the specified file path.
     *
     * @param clientBook client book to save. Cannot be null.
     * @param filePath destination for the client book. Cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveClientBook(ReadOnlyClientBook clientBook, Path filePath) throws IOException;

}
