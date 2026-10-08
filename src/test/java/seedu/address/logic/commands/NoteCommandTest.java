package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class NoteCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_firstAndRepeatedNotes_appendAndPreserveOtherFields() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        InteractionNote note = new InteractionNote(LocalDate.of(2000, 1, 1), "Discussed retirement");
        Command command = parser.parseCommand("note 1 d/2000-01-01 n/Discussed retirement");
        assertEquals("Added interaction note for " + original.getName().fullName + ".",
                command.execute(model).getFeedbackToUser());
        assertEquals(new PersonBuilder(original).withInteractionNotes(note).build(),
                model.getFilteredPersonList().get(0));
        command.execute(model);
        assertEquals(List.of(note, note), model.getFilteredPersonList().get(0).getInteractionNotes());
    }

    @Test
    public void execute_filteredIndex_updatesDisplayedPersonOnly() throws Exception {
        Person first = model.getFilteredPersonList().get(0);
        Person target = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        parser.parseCommand("note 1 d/2000-01-01 n/Called").execute(model);
        assertEquals(first, model.getAddressBook().getPersonList().get(0));
        assertEquals(target.getName(), model.getFilteredPersonList().get(0).getName());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(1, model.getFilteredPersonList().get(0).getInteractionNotes().size());
        assertCommandFailure(new NoteCommand(INDEX_SECOND_PERSON,
                new InteractionNote(LocalDate.of(2000, 1, 1), "Called")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_invalidIndex() {
        assertCommandFailure(new NoteCommand(INDEX_FIRST_PERSON,
                new InteractionNote(LocalDate.of(2000, 1, 1), "Called")), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_editAndInfo_preserveNotes() throws Exception {
        parser.parseCommand("note 1 d/2000-01-01 n/Called").execute(model);
        List<InteractionNote> notes = model.getFilteredPersonList().get(0).getInteractionNotes();
        parser.parseCommand("edit 1 p/81234567").execute(model);
        assertEquals(notes, model.getFilteredPersonList().get(0).getInteractionNotes());
        assertEquals("81234567", model.getFilteredPersonList().get(0).getPhone().value);
        parser.parseCommand("info 1 /fn Retirement").execute(model);
        assertEquals(notes, model.getFilteredPersonList().get(0).getInteractionNotes());
        assertEquals("Retirement", model.getFilteredPersonList().get(0).getFinancialNeed().orElseThrow().value);
    }
}
