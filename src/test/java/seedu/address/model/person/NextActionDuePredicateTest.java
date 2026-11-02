package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NextActionDuePredicateTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);
    private final NextActionDuePredicate predicate = new NextActionDuePredicate(TODAY);

    @Test
    public void test_incompleteActionDueYesterday_returnsTrue() {
        assertTrue(predicate.test(personWithAction(TODAY.minusDays(1), false)));
    }

    @Test
    public void test_incompleteActionDueToday_returnsTrue() {
        assertTrue(predicate.test(personWithAction(TODAY, false)));
    }

    @Test
    public void test_incompleteActionDueTomorrow_returnsFalse() {
        assertFalse(predicate.test(personWithAction(TODAY.plusDays(1), false)));
    }

    @Test
    public void test_sevenDayLookahead_includesActionCutoffOnly() {
        NextActionDuePredicate sevenDays = new NextActionDuePredicate(TODAY, 7);
        assertTrue(sevenDays.test(personWithAction(TODAY.minusDays(1), false)));
        assertTrue(sevenDays.test(personWithAction(TODAY.plusDays(7), false)));
        assertFalse(sevenDays.test(personWithAction(TODAY.plusDays(8), false)));
        assertFalse(sevenDays.test(personWithAction(TODAY.plusDays(7), true)));
    }

    @Test
    public void test_reminderLookahead_usesAtLeastThirtyDays() {
        Person atThirty = new PersonBuilder().withRecurringReminders(
                new RecurringReminder("Renewal", MonthDay.from(TODAY.plusDays(30)))).build();
        Person atThirtyOne = new PersonBuilder().withRecurringReminders(
                new RecurringReminder("Renewal", MonthDay.from(TODAY.plusDays(31)))).build();
        Person atThirtyTwo = new PersonBuilder().withRecurringReminders(
                new RecurringReminder("Renewal", MonthDay.from(TODAY.plusDays(32)))).build();
        for (int actionDays : new int[] {0, 7, 30, 31, 365}) {
            NextActionDuePredicate lookahead = new NextActionDuePredicate(TODAY, actionDays);
            assertTrue(lookahead.test(atThirty));
            assertEquals(actionDays >= 31, lookahead.test(atThirtyOne));
            assertEquals(actionDays >= 32, lookahead.test(atThirtyTwo));
        }
    }

    @Test
    public void test_completedAction_returnsFalse() {
        assertFalse(predicate.test(personWithAction(TODAY.minusDays(1), true)));
        assertFalse(predicate.test(personWithAction(TODAY, true)));
    }

    @Test
    public void test_noNextAction_returnsFalse() {
        assertFalse(predicate.test(new PersonBuilder().build()));
    }

    @Test
    public void earliestQualifyingDate_mixedEvents_ignoresCompletedAndFutureActions() {
        Person person = new PersonBuilder().withRecurringReminders(
                new RecurringReminder("Later", MonthDay.from(TODAY.plusDays(20))),
                new RecurringReminder("Earlier", MonthDay.from(TODAY.plusDays(5)))).build();
        assertEquals(Optional.of(TODAY.plusDays(5)), predicate.earliestQualifyingDate(person));
        assertEquals(Optional.of(TODAY.plusDays(5)), predicate.earliestQualifyingDate(
                person.withNextAction(new NextAction("Call", TODAY.minusDays(1), true))));
        assertEquals(Optional.of(TODAY.plusDays(5)), predicate.earliestQualifyingDate(
                person.withNextAction(new NextAction("Call", TODAY.plusDays(1), false))));
        assertEquals(Optional.of(TODAY.minusDays(1)), predicate.earliestQualifyingDate(
                person.withNextAction(new NextAction("Call", TODAY.minusDays(1), false))));
    }

    @Test
    public void earliestQualifyingDate_remindersAcrossYearBoundary_includesOnlyNextThirtyDays() {
        LocalDate today = LocalDate.of(2026, 12, 20);
        NextActionDuePredicate yearEnd = new NextActionDuePredicate(today);
        for (int offset : new int[] {0, 30, 31}) {
            Person person = new PersonBuilder().withRecurringReminders(
                    new RecurringReminder("Renewal", MonthDay.from(today.plusDays(offset)))).build();
            Optional<LocalDate> expected = offset <= 30 ? Optional.of(today.plusDays(offset)) : Optional.empty();
            assertEquals(expected, yearEnd.earliestQualifyingDate(person));
            assertEquals(expected.isPresent(), yearEnd.test(person));
        }
    }

    @Test
    public void equals() {
        assertEquals(predicate, predicate);
        assertEquals(predicate, new NextActionDuePredicate(TODAY));
        assertEquals(predicate, new NextActionDuePredicate(TODAY, 0));
        assertEquals(predicate.hashCode(), new NextActionDuePredicate(TODAY).hashCode());
        assertEquals(new NextActionDuePredicate(TODAY, 7), new NextActionDuePredicate(TODAY, 7));
        assertEquals(new NextActionDuePredicate(TODAY, 7).hashCode(),
                new NextActionDuePredicate(TODAY, 7).hashCode());
        assertNotEquals(predicate, new NextActionDuePredicate(TODAY, 7));
        assertNotEquals(predicate, new NextActionDuePredicate(TODAY.plusDays(1)));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, TODAY);
    }

    @Test
    public void toStringMethod() {
        String expected = NextActionDuePredicate.class.getCanonicalName() + "{today=" + TODAY + ", actionDays=0}";
        assertEquals(expected, predicate.toString());
        assertEquals(NextActionDuePredicate.class.getCanonicalName() + "{today=" + TODAY + ", actionDays=7}",
                new NextActionDuePredicate(TODAY, 7).toString());
    }

    private Person personWithAction(LocalDate dueDate, boolean completed) {
        return new PersonBuilder().withNextAction(new NextAction("Call", dueDate, completed)).build();
    }
}
