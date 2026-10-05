package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class FinancialNeedTest {

    @Test
    public void nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FinancialNeed(null));
        assertThrows(NullPointerException.class, () -> FinancialNeed.isValidFinancialNeed(null));
        assertThrows(NullPointerException.class, () -> FinancialNeed.getValidationError(null));
    }

    @Test
    public void validValues_accepted() {
        for (String input : new String[] {"A", "1", "Retirement 2030", "Family's long-term needs (A & B), etc.",
            "\u4fdd\u969c", ".,'-()&", "A".repeat(200), "  " + "A".repeat(200) + "  "}) {
            assertTrue(FinancialNeed.isValidFinancialNeed(input));
            assertNull(FinancialNeed.getValidationError(input));
            assertEquals(input.trim(), new FinancialNeed(input).value);
        }
    }

    @Test
    public void constructor_trimsEdgesAndPreservesInternalSpaces() {
        FinancialNeed result = new FinancialNeed("  Long  term   plans  ");
        assertEquals("Long  term   plans", result.value);
        assertEquals(result.value, result.toString());
    }

    @Test
    public void emptyValues_rejectedWithExactMessage() {
        for (String input : new String[] {"", " ", "   "}) {
            assertInvalid(input, "Financial need cannot be empty.");
        }
    }

    @Test
    public void overlongValues_rejectedWithExactMessage() {
        assertInvalid("A".repeat(201), "Financial need must not exceed 200 characters.");
        assertInvalid("  " + "A".repeat(201) + "  ", "Financial need must not exceed 200 characters.");
    }

    @Test
    public void invalidCharacters_rejectedWithExactMessage() {
        for (String input : new String[] {"/", "Needs/Wants", "r/note", "Plan!", "Plan?", "A_B", "A:B", "A;B",
            "A@B", "A#B", "A$B", "A%B", "A+B", "[A]", "{A}", "A\\B", "A\"B", "A\tB", "A\nB",
            "A\rB", "A\u00a0B", "A\uD83D\uDE00B"}) {
            assertInvalid(input, "Financial need contains invalid characters");
        }
    }

    @Test
    public void edgeControlCharacters_rejectedWithExactMessage() {
        for (String control : new String[] {"\t", "\n", "\r", "\f", "\u0000"}) {
            assertInvalid(control + "Retirement", "Financial need contains invalid characters");
            assertInvalid("Retirement" + control, "Financial need contains invalid characters");
            assertInvalid("  " + control + "Retirement  ", "Financial need contains invalid characters");
            assertInvalid("  Retirement" + control + "  ", "Financial need contains invalid characters");
            assertInvalid("  " + control + "  ", "Financial need contains invalid characters");
        }
    }

    @Test
    public void equalsAndHashCode_useTrimmedValue() {
        FinancialNeed value = new FinancialNeed("Long term");
        FinancialNeed sameValue = new FinancialNeed("  Long term  ");
        assertTrue(value.equals(value));
        assertTrue(value.equals(sameValue));
        assertEquals(value.hashCode(), sameValue.hashCode());
        assertFalse(value.equals(null));
        assertFalse(value.equals("Long term"));
        assertFalse(value.equals(new FinancialNeed("Other plan")));
        assertFalse(value.equals(new FinancialNeed("Long  term")));
    }

    private void assertInvalid(String input, String message) {
        assertFalse(FinancialNeed.isValidFinancialNeed(input));
        assertEquals(message, FinancialNeed.getValidationError(input));
        assertThrows(IllegalArgumentException.class, message, () -> new FinancialNeed(input));
    }
}
