package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.time.LocalDate;
import java.time.MonthDay;
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
import seedu.address.testutil.PersonBuilder;

public class RemindCommandTest {
    private static final RecurringReminder REMINDER = new RecurringReminder("Birthday", MonthDay.of(10, 26));
    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());

    @Test
    public void execute_addAndReplaceReminder_preservesAllOtherFields() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().getFirst()).withTags("friend")
                .withFinancialNeed("Retirement").withPriority("High").withAreaOfInterest("Insurance")
                .withPreferredContactChannel("Email").withPreferredContactTiming("Morning")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2026, 1, 1), "Discussed policy"))
                .withNextAction(new NextAction("Call", LocalDate.of(2026, 1, 2), true)).build();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        RemindCommand command = new RemindCommand(INDEX_FIRST_PERSON, REMINDER);
        assertEquals(String.format(RemindCommand.MESSAGE_SUCCESS, original.getName().fullName),
                command.execute(model, null).getFeedbackToUser());
        assertEquals(new PersonBuilder(original).withRecurringReminders(REMINDER).build(),
                model.getFilteredPersonList().getFirst());
        RecurringReminder changed = new RecurringReminder("Birthday", MonthDay.of(10, 27));
        new RemindCommand(INDEX_FIRST_PERSON, changed).execute(model, null);
        assertEquals(List.of(changed), model.getFilteredPersonList().getFirst().getRecurringReminders());
    }

    @Test
    public void execute_displayedIndexAndInvalidIndex_respectsFilterWithoutMutationOnFailure() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new RemindCommand(INDEX_FIRST_PERSON, REMINDER).execute(model, null);
        assertEquals(first, model.getClientBook().getPersonList().getFirst());
        assertEquals(second.withRecurringReminder(REMINDER), model.getFilteredPersonList().getFirst());
        assertCommandFailure(new RemindCommand(INDEX_SECOND_PERSON, REMINDER), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new RemindCommand(Index.fromOneBased(1), REMINDER), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_existingPersonMutations_preserveReminderAndOptionalFields() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().getFirst())
                .withFinancialNeed("Retirement").withPriority("High").withAreaOfInterest("Insurance")
                .withPreferredContactChannel("Email").withPreferredContactTiming("Morning")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2026, 1, 1), "Original"))
                .withNextAction(new NextAction("Call", LocalDate.of(2026, 1, 2), false))
                .withRecurringReminders(REMINDER).build();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        ClientBookParser parser = new ClientBookParser();
        for (String command : List.of("edit 1 p/81234567", "info 1 /fn Retirement", "note 1 d/2026-01-01 n/New",
                "noteedit 1 1 n/Changed", "notedelete 1 1", "tag 1 priority", "contact 1 ch/Email tm/Morning",
                "next 1 a/Call d/2026-01-02", "done 1")) {
            parser.parseCommand(command).execute(model, null);
            Person updated = model.getFilteredPersonList().getFirst();
            assertEquals(List.of(REMINDER), updated.getRecurringReminders(), command);
            assertEquals(original.getFinancialNeed(), updated.getFinancialNeed(), command);
            assertEquals(original.getPriority(), updated.getPriority(), command);
            assertEquals(original.getAreaOfInterest(), updated.getAreaOfInterest(), command);
            assertEquals(original.getPreferredContactChannel(), updated.getPreferredContactChannel(), command);
            assertEquals(original.getPreferredContactTiming(), updated.getPreferredContactTiming(), command);
        }
    }

    @Test
    public void equalsAndHashCode_compareIndexAndReminder() {
        RemindCommand command = new RemindCommand(INDEX_FIRST_PERSON, REMINDER);
        RemindCommand same = new RemindCommand(INDEX_FIRST_PERSON, REMINDER);
        assertEquals(command, same);
        assertEquals(command.hashCode(), same.hashCode());
        assertNotEquals(command, new RemindCommand(INDEX_SECOND_PERSON, REMINDER));
        assertNotEquals(command, new RemindCommand(INDEX_FIRST_PERSON,
                new RecurringReminder("Renewal", REMINDER.getDate())));
        assertNotEquals(command, null);
    }
}
