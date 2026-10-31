package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.InfoCommand.InfoDescriptor;
import seedu.address.logic.parser.InfoCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AreaOfInterest;
import seedu.address.model.person.FinancialNeed;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.Priority;
import seedu.address.model.person.Relationship;
import seedu.address.testutil.PersonBuilder;

public class InfoCommandTest {
    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
    private final InfoCommandParser parser = new InfoCommandParser();

    private Person populatedPerson() {
        return new PersonBuilder(model.getFilteredPersonList().getFirst()).withRelationship(Relationship.CLIENT)
                .withTags("friend").withFinancialNeed("Education").withPriority("Low").withAreaOfInterest("Savings")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2000, 1, 1), "Discussed plans"))
                .withNextAction(new NextAction("Call", LocalDate.of(2000, 1, 2), true)).build();
    }

    @Test
    public void execute_invalidFinancialFieldsWithUpdatesOrClears_noMutation() {
        Person original = populatedPerson();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        List<Person> expectedPersons = List.copyOf(model.getClientBook().getPersonList());
        for (String args : new String[] {"1 fn/Retirement pr/ ai/", "1 fn/- pr/ ai/",
            "1 fn/ pr/High ai/-", "1 fn/ pr/- ai/Insurance",
            "1 fn/Retire on $3000 monthly pr/Bad! ai/-", "1 fn/- pr/Target 6% return ai/Bad!",
            "1 fn/Bad! pr/- ai/CPF/SRS", "1 fn/CPF/SRS /fnn value",
            "1 fn/Life / Health pr/-", "1 fn/- /pr High", "1 fn/- fnn/Value",
            "1 fn/- pr/High pr/Low", "1 fn/- ai/Insurance\t"}) {
            assertThrows(ParseException.class, () -> parser.parse(args).execute(model, null));
            assertEquals(expectedPersons, model.getClientBook().getPersonList());
        }
    }

    @Test
    public void execute_financialNotation_preservesOtherFields() throws Exception {
        Person original = new PersonBuilder(populatedPerson()).withPreferredContactChannel("Email")
                .withPreferredContactTiming("Weekends").build();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        parser.parse("1 ai/Planning Life/Health insurance fn/Save $3000/month + CPF/SRS pr/Target 6% return")
                .execute(model, null);
        Person expected = new PersonBuilder(original).withFinancialNeed("Save $3000/month + CPF/SRS")
                .withPriority("Target 6% return").withAreaOfInterest("Planning Life/Health insurance").build();
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_clearEachCombination_preservesOmittedAndUnrelatedFields() throws Exception {
        Person original = populatedPerson();
        for (int mask = 1; mask < 8; mask++) {
            model.setPerson(model.getFilteredPersonList().getFirst(), original);
            boolean clearNeed = (mask & 1) != 0;
            boolean clearPriority = (mask & 2) != 0;
            boolean clearInterest = (mask & 4) != 0;
            String args = "1" + (clearNeed ? " fn/-" : "") + (clearPriority ? " pr/-" : "")
                    + (clearInterest ? " ai/-" : "");
            Person expected = new Person(original.getName(), original.getPhone(), original.getEmail(),
                    original.getTags(), original.getRelationship(),
                    clearNeed ? null : original.getFinancialNeed().orElseThrow(),
                    clearPriority ? null : original.getPriority().orElseThrow(),
                    clearInterest ? null : original.getAreaOfInterest().orElseThrow(),
                    original.getInteractionNotes(), original.getNextAction().orElseThrow());
            CommandResult result = parser.parse(args).execute(model, null);
            assertEquals(expected, model.getFilteredPersonList().getFirst());
            assertEquals(String.format(InfoCommand.MESSAGE_SUCCESS, original.getName().fullName),
                    result.getFeedbackToUser());
            // Repeating the same clears must also be safe when those fields are already absent.
            parser.parse(args).execute(model, null);
            assertEquals(expected, model.getFilteredPersonList().getFirst());
        }
    }

    @Test
    public void execute_mixedSetAndClear_preservesOmittedAndUnrelatedFields() throws Exception {
        Person original = populatedPerson();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        parser.parse("1 pr/High ai/-").execute(model, null);
        Person expected = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(), original.getFinancialNeed().orElseThrow(), new Priority("High"), null,
                original.getInteractionNotes(), original.getNextAction().orElseThrow());
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        assertEquals("Savings", original.getAreaOfInterest().orElseThrow().value);
    }

    @Test
    public void execute_clearInFilteredList_preservesFilterAndOtherPeople() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1);
        Person target = new PersonBuilder(second).withPriority("High").build();
        model.setPerson(second, target);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        parser.parse("2 pr/-").execute(model, null);
        assertEquals(List.of(second), model.getFilteredPersonList());
        assertEquals(first, model.getClientBook().getPersonList().getFirst());
        assertCommandFailure(parser.parse("1 pr/-"), model,
                "No visible client or prospect exists at index 1. Use list to view all contacts.");
    }

    @Test
    public void execute_clearOutOfRange_noMutation() throws Exception {
        int invalid = model.getFilteredPersonList().size() + 1;
        assertCommandFailure(parser.parse(invalid + " fn/-"), model,
                String.format(InfoCommand.MESSAGE_INVALID_INDEX, invalid));
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(parser.parse("1 fn/-"), model,
                String.format(InfoCommand.MESSAGE_INVALID_INDEX, 1));
    }

    @Test
    public void descriptor_statesAndCopies_distinguishUnchangedSetAndClear() throws Exception {
        InfoDescriptor unspecified = new InfoDescriptor();
        InfoDescriptor clear = new InfoDescriptor();
        clear.setFinancialNeed(null);
        clear.setPriority(null);
        clear.setAreaOfInterest(null);
        assertFalse(unspecified.isFinancialNeedSpecified());
        assertFalse(unspecified.isPrioritySpecified());
        assertFalse(unspecified.isAreaOfInterestSpecified());
        assertTrue(clear.isFinancialNeedSpecified());
        assertTrue(clear.isPrioritySpecified());
        assertTrue(clear.isAreaOfInterestSpecified());
        assertTrue(clear.getFinancialNeed().isEmpty());
        assertTrue(clear.getPriority().isEmpty());
        assertTrue(clear.getAreaOfInterest().isEmpty());
        assertNotEquals(unspecified, clear);
        assertEquals(clear, new InfoDescriptor(clear));
        assertEquals(clear.hashCode(), new InfoDescriptor(clear).hashCode());
        InfoCommand command = new InfoCommand(INDEX_FIRST_PERSON, clear);
        clear.setFinancialNeed(new FinancialNeed("Retirement"));
        clear.setPriority(new Priority("High"));
        clear.setAreaOfInterest(new AreaOfInterest("Insurance"));
        assertEquals(command, parser.parse("1 fn/- pr/- ai/-"));
        assertNotEquals(clear, new InfoDescriptor());
        assertNotEquals(command, new InfoCommand(INDEX_FIRST_PERSON, clear));
        assertNotEquals(parser.parse("1 fn/-"), new InfoCommand(INDEX_FIRST_PERSON, unspecified));
        assertNotEquals(parser.parse("1 pr/-"), new InfoCommand(INDEX_FIRST_PERSON, unspecified));
        assertNotEquals(parser.parse("1 ai/-"), new InfoCommand(INDEX_FIRST_PERSON, unspecified));
    }

    @Test
    public void execute_suppliedFieldsReplaceOthersPreserved() throws Exception {
        Person base = model.getFilteredPersonList().get(0);
        Person original = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getTags(),
                Relationship.CLIENT, new FinancialNeed("Education"),
                new Priority("Low"),
                new AreaOfInterest("Savings"));
        model.setPerson(base, original);
        String[] args = {"1 fn/Retirement", "1 pr/High", "1 ai/Insurance",
            "1 fn/Retirement pr/High ai/Insurance"};
        Person[] expected = {
            new PersonBuilder(original).withFinancialNeed("Retirement").build(),
            new PersonBuilder(original).withPriority("High").build(),
            new PersonBuilder(original).withAreaOfInterest("Insurance").build(),
            new PersonBuilder(original).withFinancialNeed("Retirement").withPriority("High")
                    .withAreaOfInterest("Insurance").build()
        };
        for (int i = 0; i < args.length; i++) {
            model.setPerson(model.getFilteredPersonList().get(0), original);
            CommandResult result = parser.parse(args[i]).execute(model, null);
            assertEquals(expected[i], model.getFilteredPersonList().get(0));
            assertEquals("Client information for " + original.getName().fullName
                    + " has been updated successfully.", result.getFeedbackToUser());
        }
    }

    @Test
    public void execute_filteredList_updatesDisplayedPersonOnly() throws Exception {
        Person first = model.getFilteredPersonList().get(0);
        Person target = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        parser.parse("2 fn/Retirement").execute(model, null);
        assertEquals(new PersonBuilder(target).withFinancialNeed("Retirement").build(),
                model.getFilteredPersonList().get(0));
        assertEquals(first, model.getClientBook().getPersonList().get(0));
        assertTrue(model.getFilteredPersonList().get(0).getPriority().isEmpty());
        assertTrue(model.getFilteredPersonList().get(0).getAreaOfInterest().isEmpty());
    }

    @Test
    public void execute_nonexistentIndex_failure() throws Exception {
        int invalid = model.getFilteredPersonList().size() + 1;
        assertCommandFailure(parser.parse(invalid + " pr/High"), model,
                "No visible client or prospect exists at index " + invalid + ". Use list to view all contacts.");
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(parser.parse("2 pr/High"), model,
                "No visible client or prospect exists at index 2. Use list to view all contacts.");
    }

    @Test
    public void hashCode_separatelyCreatedEqualIndices_equalHashes() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Retirement"));
        InfoCommand first = new InfoCommand(Index.fromOneBased(1), descriptor);
        InfoCommand second = new InfoCommand(Index.fromOneBased(1), new InfoDescriptor(descriptor));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void hashCode_differentIndexValues_distinguishesIndices() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Retirement"));
        InfoCommand first = new InfoCommand(Index.fromOneBased(1), descriptor);
        InfoCommand second = new InfoCommand(Index.fromOneBased(2), descriptor);
        assertNotEquals(first, second);
    }

    @Test
    public void hashCode_equivalentDescriptors_equalCommandsHaveEqualHashes() {
        InfoDescriptor first = new InfoDescriptor();
        first.setFinancialNeed(new FinancialNeed("Retirement"));
        first.setPriority(null);
        InfoDescriptor second = new InfoDescriptor();
        second.setPriority(null);
        second.setFinancialNeed(new FinancialNeed("Retirement"));
        InfoCommand firstCommand = new InfoCommand(INDEX_FIRST_PERSON, first);
        InfoCommand secondCommand = new InfoCommand(INDEX_FIRST_PERSON, second);
        assertEquals(firstCommand, secondCommand);
        assertEquals(firstCommand.hashCode(), secondCommand.hashCode());
    }

    @Test
    public void hashCode_originalDescriptorMutated_commandRemainsUnchanged() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Retirement"));
        descriptor.setPriority(null);
        InfoCommand command = new InfoCommand(INDEX_FIRST_PERSON, descriptor);
        InfoCommand equivalent = new InfoCommand(INDEX_FIRST_PERSON, new InfoDescriptor(descriptor));
        int originalHashCode = command.hashCode();

        descriptor.setFinancialNeed(new FinancialNeed("Education"));
        descriptor.setPriority(new Priority("High"));
        descriptor.setAreaOfInterest(new AreaOfInterest("Insurance"));

        assertEquals(equivalent, command);
        assertEquals(originalHashCode, command.hashCode());
        assertEquals(equivalent.hashCode(), command.hashCode());
        assertNotEquals(new InfoCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void equals_differentValuesOrSpecifiedStates_notEqual() throws Exception {
        InfoCommand command = parser.parse("1 fn/Retirement pr/High ai/Insurance");
        for (String args : List.of("1 fn/Education pr/High ai/Insurance",
                "1 fn/Retirement pr/Low ai/Insurance", "1 fn/Retirement pr/High ai/Savings",
                "1 fn/- pr/High ai/Insurance", "1 fn/Retirement pr/- ai/Insurance",
                "1 fn/Retirement pr/High ai/-", "1 pr/High ai/Insurance",
                "1 fn/Retirement ai/Insurance", "1 fn/Retirement pr/High")) {
            assertNotEquals(command, parser.parse(args));
        }
        InfoCommand omitted = new InfoCommand(INDEX_FIRST_PERSON, new InfoDescriptor());
        for (String args : List.of("1 fn/-", "1 pr/-", "1 ai/-")) {
            assertNotEquals(omitted, parser.parse(args));
        }
    }

    @Test
    public void equalsAndDescriptorCopy() throws Exception {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Retirement"));
        InfoCommand command = new InfoCommand(INDEX_FIRST_PERSON, descriptor);
        assertTrue(command.equals(command));
        assertEquals(command, parser.parse("1 fn/Retirement"));
        assertFalse(command.equals(null));
        assertFalse(command.equals("info"));
        assertFalse(command.equals(new InfoCommand(Index.fromOneBased(2), descriptor)));
        assertFalse(command.equals(parser.parse("1 fn/Education")));
        assertFalse(command.equals(parser.parse("1 pr/High")));
        assertFalse(command.equals(parser.parse("1 ai/Insurance")));
        descriptor.setFinancialNeed(new FinancialNeed("Changed"));
        assertEquals(command, parser.parse("1 fn/Retirement"));
    }
}
