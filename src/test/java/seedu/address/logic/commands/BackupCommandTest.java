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
import seedu.address.model.ClientBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonClientBookStorage;

public class BackupCommandTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_savesCompleteClientBook() throws Exception {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Path backupFile = temporaryFolder.resolve("records.json");

        CommandResult result = new BackupCommand(backupFile).execute(model);

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

        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));

        assertTrue(exception.getMessage().startsWith(String.format(BackupCommand.MESSAGE_FAILURE, "")));
        assertEquals(getTypicalClientBook(), model.getClientBook());
    }
}
