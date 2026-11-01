package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.model.ClientBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class ClearCommandTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_emptyClientBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyClientBook_success() {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalClientBook(), new UserPrefs());
        expectedModel.setClientBook(new ClientBook());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_throughLogic_requiresConfirmationBeforeClearingAndSaving() throws Exception {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        JsonClientBookStorage storage = new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        assertEquals(ClearCommand.MESSAGE_CONFIRMATION, logic.execute("clear").getFeedbackToUser());
        assertEquals(getTypicalClientBook(), model.getClientBook());
        assertFalse(storage.readClientBook().isPresent());

        assertEquals(ClearCommand.MESSAGE_CANCELLED, logic.execute("no").getFeedbackToUser());
        assertEquals(getTypicalClientBook(), model.getClientBook());

        logic.execute("clear");
        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("yes").getFeedbackToUser());
        assertEquals(new ClientBook(), model.getClientBook());
        assertEquals(new ClientBook(), storage.readClientBook().orElseThrow());
    }

}
