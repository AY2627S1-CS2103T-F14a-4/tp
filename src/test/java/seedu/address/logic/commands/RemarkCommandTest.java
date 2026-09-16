package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.storage.JsonAddressBookStorage;

public class RemarkCommandTest {
    @TempDir
    public Path tempDir;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_addReplaceAndRemoveRemark_success() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        for (String text : new String[] {"Likes to swim.", "Likes baseball", ""}) {
            CommandResult result = parser.parseCommand("remark 1 r/" + text).execute(model);
            Person edited = model.getFilteredPersonList().get(0);
            assertEquals(new Remark(text), edited.getRemark());
            assertEquals(original.getName(), edited.getName());
            assertEquals(original.getPhone(), edited.getPhone());
            assertEquals(original.getEmail(), edited.getEmail());
            assertEquals(original.getAddress(), edited.getAddress());
            assertEquals(original.getTags(), edited.getTags());
            String message = text.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                    : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;
            assertEquals(String.format(message, Messages.format(edited)), result.getFeedbackToUser());
        }
    }

    @Test
    public void execute_filteredList_updatesDisplayedPersonAndShowsAll() throws Exception {
        Person target = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        parser.parseCommand("remark 1 r/Filtered remark").execute(model);
        assertEquals(getTypicalAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
        assertEquals(target.getName(), model.getFilteredPersonList().get(1).getName());
        assertEquals(new Remark("Filtered remark"), model.getFilteredPersonList().get(1).getRemark());
        assertEquals(new Remark(""), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(outOfBounds, new Remark("remark")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("remark")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_remarkSurvivesEditAndStorageRoundTrip() throws Exception {
        parser.parseCommand("remark 1 r/Likes to swim.").execute(model);
        parser.parseCommand("edit 1 p/91234567").execute(model);
        assertEquals(new Remark("Likes to swim."), model.getFilteredPersonList().get(0).getRemark());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(new Remark("Likes to swim."),
                storage.readAddressBook().orElseThrow().getPersonList().get(0).getRemark());
    }
}
