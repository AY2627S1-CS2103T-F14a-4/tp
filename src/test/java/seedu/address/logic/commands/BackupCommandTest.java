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
import seedu.address.model.ClientBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

public class BackupCommandTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_savesCompleteClientBook() throws Exception {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Path backupFile = temporaryFolder.resolve("records.json");

        CommandResult result = new BackupCommand(backupFile).execute(model, createStorage());

        assertEquals(new CommandResult(String.format(BackupCommand.MESSAGE_SUCCESS, backupFile)), result);
        assertEquals(getTypicalClientBook(), new ClientBook(
                new JsonClientBookStorage(backupFile).readClientBook().orElseThrow()));
    }

    @Test
    public void execute_invalidDestination_preservesRecords() throws Exception {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Path directory = temporaryFolder.resolve("folder");
        Files.createDirectory(directory);
        BackupCommand command = new BackupCommand(directory);

        CommandException exception = assertThrows(CommandException.class, () -> command.execute(
                model, createStorage()));

        assertTrue(exception.getMessage().startsWith(String.format(BackupCommand.MESSAGE_FAILURE, "")));
        assertEquals(getTypicalClientBook(), model.getClientBook());
    }

    @Test
    public void execute_writeFailure_preservesExistingBackup() throws Exception {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Path backupFile = temporaryFolder.resolve("records.json");
        ClientBook existingBackup = new ClientBook();
        new JsonClientBookStorage(backupFile).saveClientBook(existingBackup);
        Storage storage = new StorageManager(
                new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
            @Override
            public void saveClientBook(ReadOnlyClientBook clientBook, Path filePath) throws IOException {
                throw new IOException("simulated write failure");
            }
        };

        assertThrows(CommandException.class, () -> new BackupCommand(backupFile).execute(model, storage));

        assertEquals(existingBackup, new JsonClientBookStorage(backupFile).readClientBook().orElseThrow());
    }

    @Test
    public void execute_defaultPath_usesConfiguredStorageDirectory() throws Exception {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Storage storage = createStorage();
        Path expectedBackupFile = storage.getClientBookFilePath().resolveSibling(BackupCommand.BACKUP_FILE_NAME);

        new BackupCommand().execute(model, storage);

        assertEquals(getTypicalClientBook(),
                new JsonClientBookStorage(expectedBackupFile).readClientBook().orElseThrow());
    }

    private Storage createStorage() {
        return new StorageManager(
                new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
}
