package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

public class RestoreCommandTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_restoresCompleteClientBookAndResetsFilter() throws Exception {
        Path backupFile = temporaryFolder.resolve("records.json");
        new JsonClientBookStorage(backupFile).saveClientBook(getTypicalClientBook());
        Model model = new ModelManager();

        CommandResult result = new RestoreCommand(backupFile).execute(model, createStorage());

        assertEquals(new CommandResult(String.format(RestoreCommand.MESSAGE_SUCCESS, backupFile), false, false, true),
                result);
        assertEquals(getTypicalClientBook(), model.getClientBook());
        assertEquals(getTypicalClientBook().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_missingBackup_preservesRecords() {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Path missingFile = temporaryFolder.resolve("missing.json");

        CommandException exception = assertThrows(CommandException.class, () ->
                new RestoreCommand(missingFile).execute(model, createStorage()));

        assertEquals(String.format(RestoreCommand.MESSAGE_FILE_NOT_FOUND, missingFile), exception.getMessage());
        assertEquals(getTypicalClientBook(), model.getClientBook());
    }

    @Test
    public void execute_invalidBackup_preservesRecords() throws Exception {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Path invalidFile = temporaryFolder.resolve("invalid.json");
        Files.writeString(invalidFile, "not JSON");

        CommandException exception = assertThrows(CommandException.class, () ->
                new RestoreCommand(invalidFile).execute(model, createStorage()));

        assertTrue(exception.getMessage().startsWith("Could not restore backup:"));
        assertEquals(getTypicalClientBook(), model.getClientBook());
    }

    @Test
    public void execute_saveFailure_preservesRecords() throws Exception {
        Path backupFile = temporaryFolder.resolve("records.json");
        new JsonClientBookStorage(backupFile).saveClientBook(new ModelManager().getClientBook());
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Storage storage = new StorageManager(
                new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
            @Override
            public void saveClientBook(ReadOnlyClientBook clientBook) throws IOException {
                throw new IOException("simulated write failure");
            }
        };

        assertThrows(CommandException.class, () -> new RestoreCommand(backupFile).execute(model, storage));

        assertEquals(getTypicalClientBook(), model.getClientBook());
    }

    @Test
    public void execute_defaultPath_usesConfiguredStorageDirectory() throws Exception {
        Storage storage = createStorage();
        Path backupFile = storage.getClientBookFilePath().resolveSibling(BackupCommand.BACKUP_FILE_NAME);
        new JsonClientBookStorage(backupFile).saveClientBook(getTypicalClientBook());
        Model model = new ModelManager();

        new RestoreCommand().execute(model, storage);

        assertEquals(getTypicalClientBook(), model.getClientBook());
    }

    private Storage createStorage() {
        return new StorageManager(
                new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
}
