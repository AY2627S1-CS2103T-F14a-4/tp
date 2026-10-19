package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;
import seedu.address.testutil.PersonBuilder;

public class NextCommandTest {
    private static final NextAction ACTION = new NextAction("Call", LocalDate.of(2000, 1, 1), false);
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_newAction_preservesAllOtherFields() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().getFirst())
                .withRelationship(Relationship.CLIENT).withFinancialNeed("Retirement")
                .withPriority("High").withAreaOfInterest("Insurance").withTags("friend")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2000, 1, 1), "Discussed plans")).build();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        assertEquals(String.format(NextCommand.MESSAGE_SUCCESS, original.getName().fullName),
                parser.parseCommand("next 1 a/Call d/2000-01-01").execute(model).getFeedbackToUser());
        assertEquals(new PersonBuilder(original).withNextAction(ACTION).build(),
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_replaceCompletedOrPendingAction_resetsCompletion() throws Exception {
        for (boolean completed : new boolean[] {false, true}) {
            Person original = model.getFilteredPersonList().getFirst();
            model.setPerson(original, original.withNextAction(new NextAction("Email", LocalDate.now(), completed)));
            new NextCommand(INDEX_FIRST_PERSON, ACTION).execute(model);
            assertEquals(ACTION, model.getFilteredPersonList().getFirst().getNextAction().orElseThrow());
            assertFalse(model.getFilteredPersonList().getFirst().getNextAction().orElseThrow().isCompleted());
        }
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new NextCommand(INDEX_FIRST_PERSON, ACTION).execute(model);
        assertEquals(first, model.getAddressBook().getPersonList().getFirst());
        assertEquals(second.withNextAction(ACTION), model.getFilteredPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
        assertCommandFailure(new NextCommand(INDEX_SECOND_PERSON, ACTION), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndex_doesNotChangeModel() {
        assertCommandFailure(new NextCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1), ACTION),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new NextCommand(INDEX_FIRST_PERSON, ACTION), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_editInfoAndNote_preserveFollowup() throws Exception {
        for (boolean completed : new boolean[] {false, true}) {
            NextAction action = new NextAction("Call", ACTION.getDueDate(), completed);
            Person original = model.getFilteredPersonList().getFirst();
            model.setPerson(original, original.withNextAction(action));
            for (String command : new String[] {"edit 1 p/81234567", "info 1 /fn Retirement",
                "note 1 d/2000-01-01 n/Discussed plans"}) {
                parser.parseCommand(command).execute(model);
                assertEquals(action, model.getFilteredPersonList().getFirst().getNextAction().orElseThrow());
            }
        }
    }

    @Test
    public void equals_comparesIndexAndAction() {
        NextCommand command = new NextCommand(INDEX_FIRST_PERSON, ACTION);
        assertEquals(command, new NextCommand(INDEX_FIRST_PERSON, ACTION));
        assertNotEquals(command, new NextCommand(INDEX_SECOND_PERSON, ACTION));
        assertNotEquals(command, new NextCommand(INDEX_FIRST_PERSON,
                new NextAction("Email", ACTION.getDueDate(), false)));
        assertNotEquals(command, null);
    }

    @Test
    public void toStringMethod() {
        NextCommand nextCommand = new NextCommand(INDEX_FIRST_PERSON, ACTION);
        String expected = NextCommand.class.getCanonicalName()
                + "{index=" + INDEX_FIRST_PERSON + ", nextAction=" + ACTION + "}";
        assertEquals(expected, nextCommand.toString());
    }

}
