package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecurringReminder;
import seedu.address.testutil.ClientBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class DueCommandTest {
    // The instant is still October 19 in UTC, but already October 20 in Singapore.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T16:30:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);
    private static final Person DUE_TODAY = new PersonBuilder().withName("Today")
            .withPhone("90000001").withEmail("today@example.com")
            .withNextAction(new NextAction("Call", TODAY, false)).build();
    private static final Person DUE_YESTERDAY = new PersonBuilder().withName("Yesterday")
            .withPhone("90000002").withEmail("yesterday@example.com")
            .withNextAction(new NextAction("Email", TODAY.minusDays(1), false)).build();
    private static final Person DUE_TOMORROW = new PersonBuilder().withName("Tomorrow")
            .withPhone("90000003").withEmail("tomorrow@example.com")
            .withNextAction(new NextAction("Meet", TODAY.plusDays(1), false)).build();
    private static final Person COMPLETED = new PersonBuilder().withName("Completed")
            .withPhone("90000004").withEmail("completed@example.com")
            .withNextAction(new NextAction("Call", TODAY.minusDays(1), true)).build();
    private static final Person NO_ACTION = new PersonBuilder().withName("No Action")
            .withPhone("90000005").withEmail("noaction@example.com").build();

    private final DueCommand command = new DueCommand(CLOCK);
    private final Model model = new ModelManager(new ClientBookBuilder()
            .withPerson(DUE_TODAY).withPerson(DUE_TOMORROW).withPerson(COMPLETED)
            .withPerson(DUE_YESTERDAY).withPerson(NO_ACTION).build(), new UserPrefs());

    @Test
    public void execute_mixedActions_sortsDuePeopleWithoutReorderingStoredContacts() throws Exception {
        List<Person> originalPeople = List.copyOf(model.getClientBook().getPersonList());

        CommandResult result = command.execute(model, null);

        assertEquals(List.of(DUE_YESTERDAY, DUE_TODAY), model.getFilteredPersonList());
        assertEquals(String.format(DueCommand.MESSAGE_SUCCESS, 2), result.getFeedbackToUser());
        assertEquals(originalPeople, model.getClientBook().getPersonList());
    }

    @Test
    public void execute_noDueActions_returnsEmptyResultMessage() throws Exception {
        Model noDueModel = new ModelManager(new ClientBookBuilder().withPerson(DUE_TOMORROW)
                .withPerson(COMPLETED).withPerson(NO_ACTION).build(), new UserPrefs());

        assertEquals(DueCommand.MESSAGE_NO_FOLLOW_UPS, command.execute(noDueModel, null).getFeedbackToUser());
        assertTrue(noDueModel.getFilteredPersonList().isEmpty());
        assertEquals(DueCommand.MESSAGE_NO_FOLLOW_UPS, command.execute(new ModelManager(), null).getFeedbackToUser());
    }

    @Test
    public void execute_singleDueAction_returnsCountMessage() throws Exception {
        Model singleDueModel = new ModelManager(new ClientBookBuilder().withPerson(DUE_TODAY).build(),
                new UserPrefs());

        assertEquals(String.format(DueCommand.MESSAGE_SUCCESS, 1),
                command.execute(singleDueModel, null).getFeedbackToUser());
    }

    @Test
    public void execute_previousFindFilter_replacesFilter() throws Exception {
        new FindCommand(new NameContainsKeywordsPredicate(List.of("Tomorrow"))).execute(model, null);
        assertEquals(List.of(DUE_TOMORROW), model.getFilteredPersonList());

        command.execute(model, null);

        assertEquals(List.of(DUE_YESTERDAY, DUE_TODAY), model.getFilteredPersonList());
    }

    @Test
    public void execute_doneAfterDue_removesCompletedPersonFromDisplayedList() throws Exception {
        command.execute(model, null);

        new DoneCommand(Index.fromOneBased(4)).execute(model, null);

        assertEquals(List.of(DUE_TODAY), model.getFilteredPersonList());
        Person completedPerson = model.getClientBook().getPersonList().get(3);
        assertEquals(DUE_YESTERDAY.withNextAction(DUE_YESTERDAY.getNextAction().orElseThrow().markCompleted()),
                completedPerson);
        assertEquals(5, model.getClientBook().getPersonList().size());
    }

    @Test
    public void execute_differentClockDate_usesClockAtExecution() throws Exception {
        DueCommand laterCommand = new DueCommand(Clock.fixed(Instant.parse("2026-10-20T16:30:00Z"),
                CLOCK.getZone()));

        assertEquals(String.format(DueCommand.MESSAGE_SUCCESS, 3),
                laterCommand.execute(model, null).getFeedbackToUser());
        assertEquals(List.of(DUE_YESTERDAY, DUE_TODAY, DUE_TOMORROW), model.getFilteredPersonList());
    }

    @Test
    public void execute_equalDates_sortsNamesIgnoringCase() throws Exception {
        Person zulu = new PersonBuilder(DUE_TODAY).withName("Zulu").build();
        Person alice = new PersonBuilder(DUE_YESTERDAY).withName("alice")
                .withNextAction(new NextAction("Call", TODAY, false)).build();
        Model tied = new ModelManager(new ClientBookBuilder().withPerson(zulu).withPerson(alice).build(),
                new UserPrefs());
        command.execute(tied, null);
        assertEquals(List.of(alice, zulu), tied.getFilteredPersonList());
        assertEquals(List.of(zulu, alice), tied.getClientBook().getPersonList());
    }

    @Test
    public void execute_equalNamesIgnoringCase_usesExactSpelling() throws Exception {
        Person lower = new PersonBuilder(DUE_TODAY).withName("alice").build();
        Person upper = new PersonBuilder(DUE_YESTERDAY).withName("Alice")
                .withNextAction(new NextAction("Call", TODAY, false)).build();
        Model tied = new ModelManager(new ClientBookBuilder().withPerson(lower).withPerson(upper).build(),
                new UserPrefs());
        command.execute(tied, null);
        assertEquals(List.of(upper, lower), tied.getFilteredPersonList());
    }

    @Test
    public void execute_recurringAndOneOffEvents_includesPersonOnlyOnceAndSortsByDate() throws Exception {
        Person upcoming = NO_ACTION.withRecurringReminder(new RecurringReminder("Birthday", MonthDay.of(11, 19)));
        Person outside = COMPLETED.withRecurringReminder(new RecurringReminder("Renewal", MonthDay.of(11, 20)));
        Person both = DUE_TODAY.withRecurringReminder(new RecurringReminder("Birthday", MonthDay.of(10, 20)));
        Model mixed = new ModelManager(new ClientBookBuilder().withPerson(upcoming).withPerson(outside)
                .withPerson(both).withPerson(DUE_YESTERDAY).build(), new UserPrefs());
        command.execute(mixed, null);
        assertEquals(List.of(DUE_YESTERDAY, both, upcoming), mixed.getFilteredPersonList());
        new DoneCommand(Index.fromOneBased(3)).execute(mixed, null);
        assertTrue(mixed.getClientBook().getPersonList().get(2).getNextAction().orElseThrow().isCompleted());
        assertEquals(3, mixed.getFilteredPersonList().size(), "Upcoming reminders keep a completed person visible");
    }

    @Test
    public void execute_reminderDates_ignoreCompletedAndFutureActionDatesWhenSorting() throws Exception {
        Person early = new PersonBuilder(NO_ACTION).withRecurringReminders(
                new RecurringReminder("Birthday", MonthDay.of(10, 25))).build();
        Person middle = new PersonBuilder(COMPLETED).withRecurringReminders(
                new RecurringReminder("Renewal", MonthDay.of(11, 4))).build();
        Person late = new PersonBuilder(DUE_TOMORROW).withRecurringReminders(
                new RecurringReminder("Later", MonthDay.of(11, 15)),
                new RecurringReminder("Earlier", MonthDay.of(11, 9))).build();
        Model reminders = new ModelManager(new ClientBookBuilder().withPerson(late).withPerson(middle)
                .withPerson(early).build(), new UserPrefs());

        command.execute(reminders, null);

        assertEquals(List.of(early, middle, late), reminders.getFilteredPersonList());
        assertEquals(List.of(late, middle, early), reminders.getClientBook().getPersonList());
        new RemindCommand(Index.fromOneBased(1),
                new RecurringReminder("Earlier", MonthDay.of(10, 21))).execute(reminders, null);
        Person updated = reminders.getClientBook().getPersonList().getFirst();
        assertEquals(List.of(updated, early, middle), reminders.getFilteredPersonList());
        new UnremindCommand(Index.fromOneBased(1), "Earlier").execute(reminders, null);
        updated = reminders.getClientBook().getPersonList().getFirst();
        assertEquals(List.of(early, middle, updated), reminders.getFilteredPersonList());
    }

    @Test
    public void execute_actionAndReminderOnSameDate_sortsByName() throws Exception {
        Person reminder = new PersonBuilder(NO_ACTION).withName("Alpha").withRecurringReminders(
                new RecurringReminder("Birthday", MonthDay.of(10, 20))).build();
        Person action = new PersonBuilder(DUE_TODAY).withName("Zulu").build();
        Model tied = new ModelManager(new ClientBookBuilder().withPerson(action).withPerson(reminder).build(),
                new UserPrefs());

        command.execute(tied, null);

        assertEquals(List.of(reminder, action), tied.getFilteredPersonList());
    }

    @Test
    public void equals() {
        assertEquals(command, command);
        assertEquals(command, new DueCommand(Clock.fixed(CLOCK.instant(), CLOCK.getZone())));
        assertEquals(command.hashCode(), new DueCommand(Clock.fixed(CLOCK.instant(), CLOCK.getZone())).hashCode());
        assertNotEquals(command, new DueCommand(Clock.fixed(CLOCK.instant().plusSeconds(1), CLOCK.getZone())));
        assertNotEquals(command, new DueCommand(CLOCK.withZone(ZoneId.of("UTC"))));
        assertNotEquals(command, null);
        assertNotEquals(command, new ListCommand());
    }

    @Test
    public void toStringMethod() {
        String expected = DueCommand.class.getCanonicalName() + "{clock=" + CLOCK + "}";
        assertEquals(expected, command.toString());
    }
}
