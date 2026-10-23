package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;

public class RestoreCommandTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_restoresCompleteAddressBookAndResetsFilter() throws Exception {
        Path backupFile = temporaryFolder.resolve("records.json");
        new JsonAddressBookStorage(backupFile).saveAddressBook(getTypicalAddressBook());
        Model model = new ModelManager();

        CommandResult result = new RestoreCommand(backupFile).execute(model);

        assertEquals(new CommandResult(String.format(RestoreCommand.MESSAGE_SUCCESS, backupFile)), result);
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertEquals(getTypicalAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_missingBackup_preservesRecords() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Path missingFile = temporaryFolder.resolve("missing.json");

        CommandTestUtil.assertCommandFailure(new RestoreCommand(missingFile), model,
                String.format(RestoreCommand.MESSAGE_FILE_NOT_FOUND, missingFile));
    }

    @Test
    public void execute_invalidBackup_preservesRecords() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Path invalidFile = temporaryFolder.resolve("invalid.json");
        Files.writeString(invalidFile, "not JSON");

        CommandException exception = assertThrows(CommandException.class, () ->
                new RestoreCommand(invalidFile).execute(model));

        assertTrue(exception.getMessage().startsWith("Could not restore backup:"));
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }
}
