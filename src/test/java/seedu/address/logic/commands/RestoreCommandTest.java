package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
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

        assertEquals(new CommandResult(String.format(RestoreCommand.MESSAGE_SUCCESS, backupFile)), result);
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

    private Storage createStorage() {
        return new StorageManager(
                new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
}
