package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneOffset;
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
import seedu.address.model.person.RecurringReminder;
import seedu.address.testutil.ClientBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class UnremindCommandTest {
    private static final RecurringReminder BIRTHDAY = new RecurringReminder("Birthday", MonthDay.of(10, 26));
    private static final RecurringReminder RENEWAL = new RecurringReminder("Policy renewal", MonthDay.of(11, 1));
    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());

    @Test
    public void execute_mixedCaseLabel_removesOnlyMatchingReminderAndPreservesAllOtherFields() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().getFirst()).withTags("friend")
                .withFinancialNeed("Retirement").withPriority("High").withAreaOfInterest("Insurance")
                .withPreferredContactChannel("Email").withPreferredContactTiming("Morning")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2026, 1, 1), "Discussed policy"))
                .withNextAction(new NextAction("Call", LocalDate.of(2026, 1, 2), true))
                .withRecurringReminders(BIRTHDAY, RENEWAL).build();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        assertEquals(String.format(UnremindCommand.MESSAGE_SUCCESS, "Birthday", original.getName().fullName),
                new ClientBookParser().parseCommand("unremind 1 a/bIRTHDAY").execute(model, null).getFeedbackToUser());
        assertEquals(new PersonBuilder(original).withRecurringReminders(RENEWAL).build(),
                model.getFilteredPersonList().getFirst());
        new UnremindCommand(INDEX_FIRST_PERSON, "POLICY RENEWAL").execute(model, null);
        assertEquals(new PersonBuilder(original).withRecurringReminders().build(),
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_missingReminderOrNoReminders_reportsErrorWithoutMutation() {
        Person original = model.getFilteredPersonList().getFirst();
        UnremindCommand command = new UnremindCommand(INDEX_FIRST_PERSON, "Missing");
        String expected = String.format(UnremindCommand.MESSAGE_NOT_FOUND, original.getName().fullName, "Missing");
        assertCommandFailure(command, model, expected);
        model.setPerson(original, original.withRecurringReminder(BIRTHDAY));
        assertCommandFailure(command, model, expected);
    }

    @Test
    public void execute_displayedIndexAndInvalidIndex_respectsFilteredList() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1);
        model.setPerson(second, second.withRecurringReminder(BIRTHDAY));
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertCommandFailure(new UnremindCommand(INDEX_SECOND_PERSON, "Birthday"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        new UnremindCommand(INDEX_FIRST_PERSON, "birthday").execute(model, null);
        assertEquals(first, model.getClientBook().getPersonList().getFirst());
        assertEquals(second, model.getFilteredPersonList().getFirst());
        assertCommandFailure(new UnremindCommand(Index.fromOneBased(1), "Birthday"), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_removeLastQualifyingReminder_rechecksDueFilterAndPreservesDueAction() throws Exception {
        Person first = new PersonBuilder().withRecurringReminders(BIRTHDAY, RENEWAL).build();
        Model dueModel = new ModelManager(new ClientBookBuilder().withPerson(first).build(), new UserPrefs());
        Clock clock = Clock.fixed(Instant.parse("2026-10-26T00:00:00Z"), ZoneOffset.UTC);
        new DueCommand(clock).execute(dueModel, null);
        new UnremindCommand(INDEX_FIRST_PERSON, "birthday").execute(dueModel, null);
        assertEquals(1, dueModel.getFilteredPersonList().size(), "The other upcoming reminder still qualifies");
        new UnremindCommand(INDEX_FIRST_PERSON, "policy renewal").execute(dueModel, null);
        assertTrue(dueModel.getFilteredPersonList().isEmpty());
        Person stored = dueModel.getClientBook().getPersonList().getFirst();
        assertTrue(stored.getRecurringReminders().isEmpty());

        Person withAction = first.withNextAction(new NextAction("Call", LocalDate.of(2026, 10, 25), false));
        dueModel.setPerson(stored, withAction);
        new UnremindCommand(INDEX_FIRST_PERSON, "birthday").execute(dueModel, null);
        new UnremindCommand(INDEX_FIRST_PERSON, "policy renewal").execute(dueModel, null);
        assertEquals(List.of(withAction.withoutRecurringReminder("Birthday")
                .withoutRecurringReminder("Policy renewal")), dueModel.getFilteredPersonList());
    }

    @Test
    public void equalsAndHashCode_compareIndexAndSuppliedLabel() {
        UnremindCommand command = new UnremindCommand(INDEX_FIRST_PERSON, "Birthday");
        UnremindCommand same = new UnremindCommand(INDEX_FIRST_PERSON, "Birthday");
        assertEquals(command, same);
        assertEquals(command.hashCode(), same.hashCode());
        assertNotEquals(command, new UnremindCommand(INDEX_SECOND_PERSON, "Birthday"));
        assertNotEquals(command, new UnremindCommand(INDEX_FIRST_PERSON, "Renewal"));
        assertNotEquals(command, null);
    }
}
