package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.ClientBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.PreferredContactChannel;
import seedu.address.model.person.PreferredContactTiming;
import seedu.address.model.person.Relationship;
import seedu.address.testutil.PersonBuilder;

public class ContactCommandTest {
    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
    private final ClientBookParser parser = new ClientBookParser();

    private Person populatedPerson() {
        return new PersonBuilder(model.getFilteredPersonList().getFirst()).withRelationship(Relationship.CLIENT)
                .withTags("friend").withFinancialNeed("Retirement").withPriority("High")
                .withAreaOfInterest("Insurance").withInteractionNotes(new InteractionNote(LocalDate.of(2000, 1, 1),
                        "Discussed plans")).withNextAction(new NextAction("Call", LocalDate.of(2000, 1, 1), false))
                .withPreferredContactChannel("Email").withPreferredContactTiming("Weekends").build();
    }

    @Test
    public void execute_replaceBothOrOne_preservesUnspecifiedFields() throws Exception {
        Person original = populatedPerson();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        assertEquals(String.format(ContactCommand.MESSAGE_SUCCESS, original.getName().fullName),
                parser.parseCommand("contact 1 tm/After 6 pm ch/Phone").execute(model, null).getFeedbackToUser());
        Person expected = new PersonBuilder(original).withPreferredContactChannel("Phone")
                .withPreferredContactTiming("After 6 pm").build();
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        parser.parseCommand("contact 1 ch/WhatsApp").execute(model, null);
        expected = new PersonBuilder(expected).withPreferredContactChannel("WhatsApp").build();
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        parser.parseCommand("contact 1 tm/Lunch break").execute(model, null);
        assertEquals(new PersonBuilder(expected).withPreferredContactTiming("Lunch break").build(),
                model.getFilteredPersonList().getFirst());
        assertEquals("Email", original.getPreferredContactChannel().orElseThrow().value);
    }

    @Test
    public void execute_existingReplacementPaths_preserveEveryUnrelatedField() throws Exception {
        Person original = populatedPerson();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        Person expected = new PersonBuilder(original).withPhone("81234567").build();
        parser.parseCommand("edit 1 p/81234567").execute(model, null);
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        expected = new PersonBuilder(expected).withFinancialNeed("Protection").build();
        parser.parseCommand("info 1 /fn Protection").execute(model, null);
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        expected = new PersonBuilder(expected).withInteractionNotes(original.getInteractionNotes().getFirst(),
                new InteractionNote(LocalDate.of(2000, 1, 2), "Followed up")).build();
        parser.parseCommand("note 1 d/2000-01-02 n/Followed up").execute(model, null);
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        NextAction action = new NextAction("Email", LocalDate.of(2000, 1, 3), false);
        expected = new PersonBuilder(expected).withNextAction(action).build();
        parser.parseCommand("next 1 a/Email d/2000-01-03").execute(model, null);
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        parser.parseCommand("done 1").execute(model, null);
        assertEquals(new PersonBuilder(expected).withNextAction(action.markCompleted()).build(),
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndKeepsFilter() throws Exception {
        Person second = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        parser.parseCommand("contact 1 ch/Email").execute(model, null);
        assertEquals(List.of(new PersonBuilder(second).withPreferredContactChannel("Email").build()),
                model.getFilteredPersonList());
        assertCommandFailure(new ContactCommand(INDEX_SECOND_PERSON, new PreferredContactChannel("Phone"), null),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_outOfRangeOrEmptyList_noMutation() {
        assertCommandFailure(new ContactCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1),
                new PreferredContactChannel("Email"), null), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(new ContactCommand(INDEX_FIRST_PERSON, new PreferredContactChannel("Email"), null),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_lastDisplayedIndexAndRepeatedValue_success() throws Exception {
        int size = model.getFilteredPersonList().size();
        Person last = model.getFilteredPersonList().getLast();
        parser.parseCommand("contact " + size + " tm/Weekends").execute(model, null);
        Person expected = new PersonBuilder(last).withPreferredContactTiming("Weekends").build();
        assertEquals(expected, model.getFilteredPersonList().getLast());
        parser.parseCommand("contact " + size + " tm/Weekends").execute(model, null);
        assertEquals(expected, model.getFilteredPersonList().getLast());
    }

    @Test
    public void constructorAndExecute_programmerErrors_throw() {
        assertThrows(NullPointerException.class, () ->
                new ContactCommand(null, new PreferredContactChannel("Email"), null));
        assertThrows(IllegalArgumentException.class, () ->
                new ContactCommand(INDEX_FIRST_PERSON, null, null));
        assertThrows(NullPointerException.class, () ->
                new ContactCommand(INDEX_FIRST_PERSON, new PreferredContactChannel("Email"), null).execute(null, null));
    }

    @Test
    public void equals_comparesIndexAndBothValues() {
        ContactCommand command = new ContactCommand(INDEX_FIRST_PERSON, new PreferredContactChannel("Email"), null);
        assertEquals(command, command);
        assertEquals(command, new ContactCommand(INDEX_FIRST_PERSON, new PreferredContactChannel("Email"), null));
        assertNotEquals(command, new ContactCommand(INDEX_SECOND_PERSON, new PreferredContactChannel("Email"), null));
        assertNotEquals(command, new ContactCommand(INDEX_FIRST_PERSON, new PreferredContactChannel("Phone"), null));
        assertNotEquals(command, new ContactCommand(INDEX_FIRST_PERSON, null, new PreferredContactTiming("Weekends")));
        assertNotEquals(command, "contact");
        assertNotEquals(null, command);
    }
}
