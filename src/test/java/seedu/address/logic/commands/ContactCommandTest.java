package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import seedu.address.logic.commands.ContactCommand.ContactDescriptor;
import seedu.address.logic.parser.ClientBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
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
    public void execute_clearPreferences_preservesOtherFields() throws Exception {
        Person original = populatedPerson();
        for (int mask = 1; mask < 4; mask++) {
            model.setPerson(model.getFilteredPersonList().getFirst(), original);
            boolean clearChannel = (mask & 1) != 0;
            boolean clearTiming = (mask & 2) != 0;
            String command = "contact 1" + (clearChannel ? " ch/ - " : "") + (clearTiming ? " tm/ - " : "");
            Person expected = new Person(original.getName(), original.getPhone(), original.getEmail(),
                    original.getTags(), original.getRelationship(), original.getFinancialNeed().orElseThrow(),
                    original.getPriority().orElseThrow(), original.getAreaOfInterest().orElseThrow(),
                    original.getInteractionNotes(), original.getNextAction().orElseThrow(),
                    clearChannel ? null : original.getPreferredContactChannel().orElseThrow(),
                    clearTiming ? null : original.getPreferredContactTiming().orElseThrow());
            for (int repeat = 0; repeat < 2; repeat++) {
                assertEquals(String.format(ContactCommand.MESSAGE_SUCCESS, original.getName().fullName),
                        parser.parseCommand(command).execute(model, null).getFeedbackToUser());
                assertEquals(expected, model.getFilteredPersonList().getFirst());
            }
        }
    }

    @Test
    public void parse_whitespaceOnlyPreferences_noMutation() {
        Person original = populatedPerson();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        List<Person> expectedPersons = List.copyOf(model.getClientBook().getPersonList());
        for (String value : List.of(" ", "\t", "\u2003", "\u00a0", "\u2007", "\u202f",
                " \t\u2003\u00a0\u2007\u202f ")) {
            for (String fields : List.of("ch/" + value, "ch/" + value + " tm/After 6 pm",
                    "tm/After 6 pm ch/" + value)) {
                assertThrows(ParseException.class, PreferredContactChannel.MESSAGE_CONSTRAINTS, () ->
                        parser.parseCommand("contact 1 " + fields).execute(model, null));
                assertEquals(expectedPersons, model.getClientBook().getPersonList());
            }
            for (String fields : List.of("tm/" + value, "ch/Phone tm/" + value,
                    "tm/" + value + " ch/Phone")) {
                assertThrows(ParseException.class, PreferredContactTiming.MESSAGE_CONSTRAINTS, () ->
                        parser.parseCommand("contact 1 " + fields).execute(model, null));
                assertEquals(expectedPersons, model.getClientBook().getPersonList());
            }
        }
    }

    @Test
    public void execute_mixedSetAndClear_preservesOtherFields() throws Exception {
        Person original = populatedPerson();
        String[] commands = {"ch/WhatsApp tm/-", "tm/- ch/WhatsApp", "ch/- tm/Weekends", "tm/Weekends ch/-"};
        for (int i = 0; i < commands.length; i++) {
            model.setPerson(model.getFilteredPersonList().getFirst(), original);
            parser.parseCommand("contact 1 " + commands[i]).execute(model, null);
            Person expected = new Person(original.getName(), original.getPhone(), original.getEmail(),
                    original.getTags(), original.getRelationship(), original.getFinancialNeed().orElseThrow(),
                    original.getPriority().orElseThrow(), original.getAreaOfInterest().orElseThrow(),
                    original.getInteractionNotes(), original.getNextAction().orElseThrow(),
                    i < 2 ? new PreferredContactChannel("WhatsApp") : null,
                    i < 2 ? null : new PreferredContactTiming("Weekends"));
            assertEquals(expected, model.getFilteredPersonList().getFirst());
        }
    }

    @Test
    public void execute_clearAbsentPreferences_noChange() throws Exception {
        Person original = model.getFilteredPersonList().getFirst();
        assertTrue(original.getPreferredContactChannel().isEmpty());
        assertTrue(original.getPreferredContactTiming().isEmpty());
        for (String fields : List.of("ch/-", "tm/-", "ch/- tm/-", "tm/- ch/-")) {
            parser.parseCommand("contact 1 " + fields).execute(model, null);
            assertEquals(original, model.getFilteredPersonList().getFirst());
        }
    }

    @Test
    public void execute_clearInFilteredList_updatesDisplayedPerson() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1);
        Person target = new PersonBuilder(second).withPreferredContactChannel("Phone")
                .withPreferredContactTiming("Evenings").build();
        model.setPerson(second, target);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        parser.parseCommand("contact 1 tm/- ch/-").execute(model, null);
        assertEquals(List.of(second), model.getFilteredPersonList());
        assertEquals(first, model.getClientBook().getPersonList().getFirst());
        assertCommandFailure(parser.parseCommand("contact 2 ch/-"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(parser.parseCommand("contact 1 tm/-"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_invalidInputWithClear_noMutation() {
        Person original = populatedPerson();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        List<Person> expected = List.copyOf(model.getClientBook().getPersonList());
        for (String fields : List.of("", "ch/- tm/", "tm/- ch/", "ch/- tm/\t", "tm/- ch/\u2003",
                "ch/- tm/\u00a0", "tm/- ch/\u2007", "ch/- tm/\u202f", "tm/- ch/\u0085",
                "ch/- tm/" + "a".repeat(201), "tm/- ch/" + "a".repeat(201),
                "ch/- ch/Phone", "tm/Weekends tm/-")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("contact 1 " + fields).execute(model, null));
            assertEquals(expected, model.getClientBook().getPersonList());
        }
        for (String index : List.of("0", "-1", "abc", "2147483648")) {
            assertThrows(ParseException.class, () ->
                    parser.parseCommand("contact " + index + " ch/- tm/-").execute(model, null));
            assertEquals(expected, model.getClientBook().getPersonList());
        }
        assertCommandFailure(new ContactCommand(Index.fromOneBased(expected.size() + 1), clearBoth()), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void descriptor_statesAndCopies_distinguishChanges() throws Exception {
        ContactDescriptor omitted = new ContactDescriptor();
        ContactDescriptor clear = clearBoth();
        assertFalse(omitted.isChannelSpecified());
        assertFalse(omitted.isTimingSpecified());
        assertTrue(clear.isChannelSpecified());
        assertTrue(clear.isTimingSpecified());
        assertTrue(clear.getChannel().isEmpty());
        assertTrue(clear.getTiming().isEmpty());
        assertNotEquals(omitted, clear);
        ContactDescriptor copy = new ContactDescriptor(clear);
        assertEquals(clear, copy);
        assertEquals(clear.hashCode(), copy.hashCode());
        ContactCommand command = new ContactCommand(INDEX_FIRST_PERSON, clear);
        clear.setChannel(new PreferredContactChannel("Phone"));
        clear.setTiming(new PreferredContactTiming("Evenings"));
        assertEquals(command, parser.parseCommand("contact 1 ch/- tm/-"));
        assertEquals(copy, clearBoth());
        assertNotEquals(copy, clear);
        assertNotEquals(copy, null);
        assertNotEquals(copy, "clear");
        assertNotEquals(command, parser.parseCommand("contact 1 ch/-"));
        assertNotEquals(command, parser.parseCommand("contact 1 tm/-"));
        assertNotEquals(command, parser.parseCommand("contact 1 ch/Phone tm/Evenings"));
        assertThrows(NullPointerException.class, () -> new ContactDescriptor(null));
        assertThrows(NullPointerException.class, () -> new ContactCommand(INDEX_FIRST_PERSON, null));
        assertThrows(IllegalArgumentException.class, () -> new ContactCommand(INDEX_FIRST_PERSON, omitted));
    }

    private ContactDescriptor clearBoth() {
        ContactDescriptor descriptor = new ContactDescriptor();
        descriptor.setChannel(null);
        descriptor.setTiming(null);
        return descriptor;
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
    public void hashCode_separateEquivalentIndices_equalHashes() {
        ContactDescriptor descriptor = clearBoth();
        ContactCommand first = new ContactCommand(Index.fromOneBased(1), descriptor);
        ContactCommand second = new ContactCommand(Index.fromOneBased(1), descriptor);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void hashCode_equivalentDescriptors_equalHashes() {
        ContactDescriptor first = new ContactDescriptor();
        first.setChannel(new PreferredContactChannel("Phone"));
        first.setTiming(null);
        ContactDescriptor second = new ContactDescriptor();
        second.setTiming(null);
        second.setChannel(new PreferredContactChannel("Phone"));
        ContactCommand firstCommand = new ContactCommand(Index.fromOneBased(1), first);
        ContactCommand secondCommand = new ContactCommand(Index.fromOneBased(1), second);
        assertEquals(firstCommand, secondCommand);
        assertEquals(firstCommand.hashCode(), secondCommand.hashCode());
    }

    @Test
    public void hashCode_originalDescriptorModified_remainsStable() {
        ContactDescriptor descriptor = new ContactDescriptor();
        descriptor.setChannel(new PreferredContactChannel("Phone"));
        ContactCommand command = new ContactCommand(Index.fromOneBased(1), descriptor);
        ContactCommand equivalent = new ContactCommand(Index.fromOneBased(1), new ContactDescriptor(descriptor));
        int originalHashCode = command.hashCode();

        descriptor.setChannel(null);
        descriptor.setTiming(new PreferredContactTiming("Evenings"));

        assertEquals(equivalent, command);
        assertEquals(originalHashCode, command.hashCode());
        assertEquals(equivalent.hashCode(), command.hashCode());
        assertNotEquals(new ContactCommand(Index.fromOneBased(1), descriptor), command);
    }

    @Test
    public void equals_differentIndicesOrDescriptorStates_notEqual() throws Exception {
        ContactCommand command = new ContactCommand(INDEX_FIRST_PERSON, new PreferredContactChannel("Phone"), null);
        for (String args : List.of("2 ch/Phone", "1 ch/Email", "1 tm/Phone", "1 ch/-", "1 ch/Phone tm/-")) {
            assertNotEquals(command, parser.parseCommand("contact " + args));
        }
        assertNotEquals(parser.parseCommand("contact 1 tm/Weekends"),
                parser.parseCommand("contact 1 ch/- tm/Weekends"));
        assertNotEquals(parser.parseCommand("contact 1 ch/-"), parser.parseCommand("contact 1 ch/- tm/-"));
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
