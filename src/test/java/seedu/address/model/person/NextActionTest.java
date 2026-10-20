package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NextActionTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 19);

    @Test
    public void markCompleted_returnsCompletedCopyAndPreservesOriginal() {
        NextAction original = new NextAction("Call", DATE, false);
        NextAction completed = original.markCompleted();
        assertEquals(new NextAction("Call", DATE, true), completed);
        assertNotSame(original, completed);
        assertFalse(original.isCompleted());
        NextAction anotherCopy = completed.markCompleted();
        assertEquals(completed, anotherCopy);
        assertNotSame(completed, anotherCopy);
    }

    @Test
    public void constructor_stripsWhitespaceAndAcceptsMaximumLength() {
        assertEquals("Call", new NextAction("  Call  ", DATE, false).getAction());
        assertEquals(200, new NextAction(" " + "a".repeat(200) + " ", DATE, false).getAction().length());
        assertFalse(new NextAction("a", DATE, false).isCompleted());
    }

    @Test
    public void constructor_invalidDescriptionOrNull_throws() {
        for (String action : List.of("", "  ", "\t\n", "a".repeat(201))) {
            assertThrows(IllegalArgumentException.class, () -> new NextAction(action, DATE, false));
        }
        assertThrows(NullPointerException.class, () -> new NextAction(null, DATE, false));
        assertThrows(NullPointerException.class, () -> new NextAction("Call", null, false));
        assertThrows(IllegalArgumentException.class, () -> new NextAction("Call", DATE.withYear(10000), false));
    }

    @Test
    public void constructor_dateYearRange_acceptsBoundariesAndRejectsOutOfRange() {
        for (boolean completed : new boolean[] {false, true}) {
            for (LocalDate date : List.of(LocalDate.of(1, 1, 1), LocalDate.of(9999, 12, 31))) {
                assertEquals(date, new NextAction("Call", date, completed).getDueDate());
            }
            for (int year : List.of(-1, 0, 10000)) {
                assertThrows(IllegalArgumentException.class, () -> new NextAction("Call",
                        DATE.withYear(year), completed));
            }
        }
    }

    @Test
    public void parseDate_strictCalendarValidation() {
        assertEquals(LocalDate.of(2024, 2, 29), NextAction.parseDate("2024-02-29"));
        assertEquals(LocalDate.of(2000, 1, 1), NextAction.parseDate("2000-01-01"));
        assertEquals(LocalDate.of(1, 1, 1), NextAction.parseDate("0001-01-01"));
        assertEquals(LocalDate.of(9999, 12, 31), NextAction.parseDate("9999-12-31"));
        for (String date : List.of("2026-02-30", "2026-02-29", "2026-2-03", "26-02-03",
                "2026-13-01", "2026-00-01", "2026-01-00", "2026-04-31", "2026-01-01extra",
                "0000-01-01", "0000-02-29", "-0001-01-01", "10000-01-01")) {
            assertThrows(IllegalArgumentException.class, () -> NextAction.parseDate(date));
        }
        assertThrows(IllegalArgumentException.class, () -> NextAction.parseDate(null));
    }

    @Test
    public void yearZero_rejectedByParserAndConstructor() {
        IllegalArgumentException parseError =
                assertThrows(IllegalArgumentException.class, () -> NextAction.parseDate("0000-01-01"));
        assertEquals(NextAction.MESSAGE_INVALID_DATE, parseError.getMessage());
        LocalDate yearZero = LocalDate.of(0, 1, 1);
        IllegalArgumentException constructorError =
                assertThrows(IllegalArgumentException.class, () -> new NextAction("Call", yearZero, false));
        assertEquals(NextAction.MESSAGE_INVALID_DATE, constructorError.getMessage());
    }

    @Test
    public void equality_includesAllFollowupFieldsButDoesNotChangePersonIdentity() {
        NextAction action = new NextAction("Call", DATE, false);
        assertEquals(action, new NextAction("Call", DATE, false));
        assertEquals(action.hashCode(), new NextAction("Call", DATE, false).hashCode());
        assertNotEquals(action, new NextAction("Email", DATE, false));
        assertNotEquals(action, new NextAction("Call", DATE.plusDays(1), false));
        assertNotEquals(action, new NextAction("Call", DATE, true));
        assertNotEquals(action, null);
        Person original = new PersonBuilder().build();
        Person updated = original.withNextAction(action);
        assertNotEquals(original, updated);
        assertEquals(new PersonBuilder(original).withNextAction(action).build(), updated);
        assertTrue(original.isSamePerson(updated));
        assertEquals(original, updated.withNextAction(null));
    }

    @Test
    public void toStringMethod() {
        NextAction nextAction = new NextAction("Call", DATE, false);
        String expected = NextAction.class.getCanonicalName()
                + "{action=Call, dueDate=" + DATE + ", completed=false}";
        assertEquals(expected, nextAction.toString());
    }

}
