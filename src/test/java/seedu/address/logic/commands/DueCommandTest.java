package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class DueCommandTest {
    // The instant is still October 19 in UTC, but already October 20 in Singapore.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T16:30:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);
    private static final Person DUE_TODAY = new PersonBuilder().withName("Today")
            .withNextAction(new NextAction("Call", TODAY, false)).build();
    private static final Person DUE_YESTERDAY = new PersonBuilder().withName("Yesterday")
            .withNextAction(new NextAction("Email", TODAY.minusDays(1), false)).build();
    private static final Person DUE_TOMORROW = new PersonBuilder().withName("Tomorrow")
            .withNextAction(new NextAction("Meet", TODAY.plusDays(1), false)).build();
    private static final Person COMPLETED = new PersonBuilder().withName("Completed")
            .withNextAction(new NextAction("Call", TODAY.minusDays(1), true)).build();
    private static final Person NO_ACTION = new PersonBuilder().withName("No Action").build();

    private final DueCommand command = new DueCommand(CLOCK);
    private final Model model = new ModelManager(new AddressBookBuilder()
            .withPerson(DUE_TODAY).withPerson(DUE_TOMORROW).withPerson(COMPLETED)
            .withPerson(DUE_YESTERDAY).withPerson(NO_ACTION).build(), new UserPrefs());

    @Test
    public void execute_mixedActions_showsOnlyDuePeopleInOriginalOrder() {
        List<Person> originalPeople = List.copyOf(model.getAddressBook().getPersonList());

        CommandResult result = command.execute(model);

        assertEquals(List.of(DUE_TODAY, DUE_YESTERDAY), model.getFilteredPersonList());
        assertEquals("2 follow-up(s) due today or earlier.", result.getFeedbackToUser());
        assertEquals(originalPeople, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_noDueActions_returnsEmptyResultMessage() {
        Model noDueModel = new ModelManager(new AddressBookBuilder().withPerson(DUE_TOMORROW)
                .withPerson(COMPLETED).withPerson(NO_ACTION).build(), new UserPrefs());

        assertEquals("No follow-ups are due.", command.execute(noDueModel).getFeedbackToUser());
        assertTrue(noDueModel.getFilteredPersonList().isEmpty());
        assertEquals("No follow-ups are due.", command.execute(new ModelManager()).getFeedbackToUser());
    }

    @Test
    public void execute_singleDueAction_returnsCountMessage() {
        Model singleDueModel = new ModelManager(new AddressBookBuilder().withPerson(DUE_TODAY).build(),
                new UserPrefs());

        assertEquals("1 follow-up(s) due today or earlier.", command.execute(singleDueModel).getFeedbackToUser());
    }

    @Test
    public void execute_previousFindFilter_replacesFilter() {
        new FindCommand(new NameContainsKeywordsPredicate(List.of("Tomorrow"))).execute(model);
        assertEquals(List.of(DUE_TOMORROW), model.getFilteredPersonList());

        command.execute(model);

        assertEquals(List.of(DUE_TODAY, DUE_YESTERDAY), model.getFilteredPersonList());
    }

    @Test
    public void execute_doneAfterDue_removesCompletedPersonFromDisplayedList() throws Exception {
        command.execute(model);

        new DoneCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(DUE_YESTERDAY), model.getFilteredPersonList());
        Person completedPerson = model.getAddressBook().getPersonList().getFirst();
        assertEquals(DUE_TODAY.withNextAction(DUE_TODAY.getNextAction().orElseThrow().markCompleted()),
                completedPerson);
        assertEquals(5, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_differentClockDate_usesClockAtExecution() {
        DueCommand laterCommand = new DueCommand(Clock.fixed(Instant.parse("2026-10-20T16:30:00Z"),
                CLOCK.getZone()));

        assertEquals("3 follow-up(s) due today or earlier.", laterCommand.execute(model).getFeedbackToUser());
        assertEquals(List.of(DUE_TODAY, DUE_TOMORROW, DUE_YESTERDAY), model.getFilteredPersonList());
    }

    @Test
    public void equals() {
        assertEquals(command, command);
        assertEquals(command, new DueCommand(Clock.fixed(CLOCK.instant(), CLOCK.getZone())));
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
