package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;
import seedu.address.testutil.PersonBuilder;

public class DoneCommandTest {
    private static final LocalDate DATE = LocalDate.of(2000, 1, 1);
    private static final NextAction ACTION = new NextAction("Call client", DATE, false);
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_pendingAction_completesAndPreservesAllOtherFields() {
        Person original = new PersonBuilder(model.getFilteredPersonList().getFirst())
                .withRelationship(Relationship.CLIENT).withFinancialNeed("Retirement")
                .withPriority("High").withAreaOfInterest("Insurance").withTags("friend")
                .withInteractionNotes(new InteractionNote(DATE, "Discussed plans"))
                .withNextAction(ACTION).build();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        Person expected = new PersonBuilder(original).withNextAction(
                new NextAction("Call client", DATE, true)).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, expected);

        assertCommandSuccess(new DoneCommand(INDEX_FIRST_PERSON), model,
                String.format(DoneCommand.MESSAGE_SUCCESS, original.getName().fullName), expectedModel);
        assertEquals(expected, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_filteredList_targetsDisplayedIndexAndPreservesFilter() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1).withNextAction(ACTION);
        model.setPerson(model.getFilteredPersonList().get(1), second);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new DoneCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(first, model.getAddressBook().getPersonList().getFirst());
        assertEquals(second.withNextAction(ACTION.markCompleted()), model.getFilteredPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
        assertCommandFailure(new DoneCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_outOfRangeIndex_rejectsBeforeCheckingNextAction() {
        assertCommandFailure(new DoneCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1)),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new DoneCommand(INDEX_FIRST_PERSON), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(new DoneCommand(INDEX_FIRST_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_noNextAction_rejectsWithoutChangingData() {
        Person original = model.getFilteredPersonList().getFirst();
        assertTrue(original.getNextAction().isEmpty());
        assertCommandFailure(new DoneCommand(INDEX_FIRST_PERSON), model,
                String.format(DoneCommand.MESSAGE_NO_NEXT_ACTION, original.getName().fullName));
    }

    @Test
    public void execute_alreadyCompleted_rejectsWithoutChangingData() throws Exception {
        Person original = model.getFilteredPersonList().getFirst();
        model.setPerson(original, original.withNextAction(ACTION));
        DoneCommand command = new DoneCommand(INDEX_FIRST_PERSON);
        command.execute(model);
        assertCommandFailure(command, model,
                String.format(DoneCommand.MESSAGE_ALREADY_COMPLETED, original.getName().fullName));
    }

    @Test
    public void equals_comparesDisplayedIndex() {
        DoneCommand command = new DoneCommand(INDEX_FIRST_PERSON);
        assertEquals(command, command);
        assertEquals(command, new DoneCommand(INDEX_FIRST_PERSON));
        assertNotEquals(command, new DoneCommand(INDEX_SECOND_PERSON));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
    }

    @Test
    public void toStringMethod() {
        DoneCommand command = new DoneCommand(INDEX_FIRST_PERSON);
        String expected = DoneCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON + "}";
        assertEquals(expected, command.toString());
    }
}
