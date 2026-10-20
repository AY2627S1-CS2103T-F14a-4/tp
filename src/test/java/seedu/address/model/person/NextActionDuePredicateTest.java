package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

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
    public void equals() {
        assertEquals(predicate, predicate);
        assertEquals(predicate, new NextActionDuePredicate(TODAY));
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
