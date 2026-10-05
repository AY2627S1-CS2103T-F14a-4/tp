package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.InfoCommand.InfoDescriptor;
import seedu.address.logic.parser.InfoCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AreaOfInterest;
import seedu.address.model.person.FinancialNeed;
import seedu.address.model.person.Person;
import seedu.address.model.person.Priority;
import seedu.address.model.person.Relationship;
import seedu.address.testutil.PersonBuilder;

public class InfoCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final InfoCommandParser parser = new InfoCommandParser();

    @Test
    public void execute_suppliedFieldsReplaceOthersPreserved() throws Exception {
        Person base = model.getFilteredPersonList().get(0);
        Person original = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getTags(),
                Relationship.CLIENT, new FinancialNeed("Education"),
                new Priority("Low"),
                new AreaOfInterest("Savings"));
        model.setPerson(base, original);
        String[] args = {"1 /fn Retirement", "1 /pr High", "1 /ai Insurance",
            "1 /fn Retirement /pr High /ai Insurance"};
        Person[] expected = {
            new PersonBuilder(original).withFinancialNeed("Retirement").build(),
            new PersonBuilder(original).withPriority("High").build(),
            new PersonBuilder(original).withAreaOfInterest("Insurance").build(),
            new PersonBuilder(original).withFinancialNeed("Retirement").withPriority("High")
                    .withAreaOfInterest("Insurance").build()
        };
        for (int i = 0; i < args.length; i++) {
            model.setPerson(model.getFilteredPersonList().get(0), original);
            CommandResult result = parser.parse(args[i]).execute(model);
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
        parser.parse("1 /fn Retirement").execute(model);
        assertEquals(new PersonBuilder(target).withFinancialNeed("Retirement").build(),
                model.getFilteredPersonList().get(0));
        assertEquals(first, model.getAddressBook().getPersonList().get(0));
        assertTrue(model.getFilteredPersonList().get(0).getPriority().isEmpty());
        assertTrue(model.getFilteredPersonList().get(0).getAreaOfInterest().isEmpty());
    }

    @Test
    public void execute_nonexistentIndex_failure() throws Exception {
        int invalid = model.getFilteredPersonList().size() + 1;
        assertCommandFailure(parser.parse(invalid + " /pr High"), model,
                "No client exists at index " + invalid + ".");
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(parser.parse("2 /pr High"), model, "No client exists at index 2.");
    }

    @Test
    public void equalsAndDescriptorCopy() throws Exception {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Retirement"));
        InfoCommand command = new InfoCommand(INDEX_FIRST_PERSON, descriptor);
        assertTrue(command.equals(command));
        assertEquals(command, parser.parse("1 /fn Retirement"));
        assertFalse(command.equals(null));
        assertFalse(command.equals("info"));
        assertFalse(command.equals(new InfoCommand(Index.fromOneBased(2), descriptor)));
        assertFalse(command.equals(parser.parse("1 /fn Education")));
        assertFalse(command.equals(parser.parse("1 /pr High")));
        assertFalse(command.equals(parser.parse("1 /ai Insurance")));
        descriptor.setFinancialNeed(new FinancialNeed("Changed"));
        assertEquals(command, parser.parse("1 /fn Retirement"));
    }
}
