package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;

public class BackupCommandTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_savesCompleteAddressBook() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Path backupFile = temporaryFolder.resolve("records.json");

        CommandResult result = new BackupCommand(backupFile).execute(model);

        assertEquals(new CommandResult(String.format(BackupCommand.MESSAGE_SUCCESS, backupFile)), result);
        assertEquals(getTypicalAddressBook(), new AddressBook(
                new JsonAddressBookStorage(backupFile).readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_invalidDestination_preservesRecords() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Path directory = temporaryFolder.resolve("folder");
        Files.createDirectory(directory);

        CommandTestUtil.assertCommandFailure(new BackupCommand(directory), model,
                String.format(BackupCommand.MESSAGE_FAILURE, directory.toString()));
    }
}
