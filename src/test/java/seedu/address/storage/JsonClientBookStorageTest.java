package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ClientBook;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.person.NextAction;

public class JsonClientBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonClientBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void saveAndReadFollowups_preservesPendingAndCompletedStates() throws Exception {
        Path file = testFolder.resolve("followups.json");
        ClientBook original = new ClientBook();
        original.addPerson(ALICE.withNextAction(new NextAction("Call", LocalDate.of(2000, 1, 1), false)));
        original.addPerson(HOON.withNextAction(new NextAction("Email", LocalDate.of(2026, 10, 20), true)));
        JsonClientBookStorage storage = new JsonClientBookStorage(file);
        storage.saveClientBook(original);
        assertEquals(original, new ClientBook(storage.readClientBook().orElseThrow()));
    }

    @Test
    public void readClientBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readClientBook(null));
    }

    private java.util.Optional<ReadOnlyClientBook> readClientBook(String filePath) throws Exception {
        return new JsonClientBookStorage(Paths.get(filePath)).readClientBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readClientBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readClientBook("notJsonFormatClientBook.json"));
    }

    @Test
    public void readClientBook_invalidPersonClientBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readClientBook("invalidPersonClientBook.json"));
    }

    @Test
    public void readClientBook_invalidAndValidPersonClientBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readClientBook("invalidAndValidPersonClientBook.json"));
    }

    @Test
    public void readAndSaveClientBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempClientBook.json");
        ClientBook original = getTypicalClientBook();
        JsonClientBookStorage jsonClientBookStorage = new JsonClientBookStorage(filePath);

        // Save in new file and read back
        jsonClientBookStorage.saveClientBook(original, filePath);
        ReadOnlyClientBook readBack = jsonClientBookStorage.readClientBook(filePath).get();
        assertEquals(original, new ClientBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonClientBookStorage.saveClientBook(original, filePath);
        readBack = jsonClientBookStorage.readClientBook(filePath).get();
        assertEquals(original, new ClientBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonClientBookStorage.saveClientBook(original); // file path not specified
        readBack = jsonClientBookStorage.readClientBook().get(); // file path not specified
        assertEquals(original, new ClientBook(readBack));

    }

    @Test
    public void saveClientBook_nullClientBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveClientBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code clientBook} at the specified {@code filePath}.
     */
    private void saveClientBook(ReadOnlyClientBook clientBook, String filePath) {
        try {
            new JsonClientBookStorage(Paths.get(filePath))
                    .saveClientBook(clientBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveClientBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveClientBook(new ClientBook(), null));
    }
}
