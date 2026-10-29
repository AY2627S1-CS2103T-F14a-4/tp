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
        assertEquals(predicate.hashCode(), new NextActionDuePredicate(TODAY).hashCode());
        assertNotEquals(predicate, new NextActionDuePredicate(TODAY.plusDays(1)));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, TODAY);
    }

    @Test
    public void toStringMethod() {
        String expected = NextActionDuePredicate.class.getCanonicalName() + "{today=" + TODAY + "}";
        assertEquals(expected, predicate.toString());
    }

    private Person personWithAction(LocalDate dueDate, boolean completed) {
        return new PersonBuilder().withNextAction(new NextAction("Call", dueDate, completed)).build();
    }
}
