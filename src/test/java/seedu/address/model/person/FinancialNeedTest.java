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
            "\u4fdd\u969c", "\u0661\uff12", "\ud801\udc00".repeat(100), ".,'-()&", "A".repeat(199),
            "A".repeat(200), "  " + "A".repeat(200) + "  "}) {
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
        for (String input : new String[] {"", " ", "   ", " ".repeat(201)}) {
            assertInvalid(input, "Financial need cannot be empty.");
        }
    }

    @Test
    public void overlongValues_rejectedWithExactMessage() {
        assertInvalid("A".repeat(201), "Financial need must not exceed 200 characters.");
        assertInvalid("  " + "A".repeat(201) + "  ", "Financial need must not exceed 200 characters.");
        assertInvalid("\ud801\udc00".repeat(100) + "A", "Financial need must not exceed 200 characters.");
    }

    @Test
    public void invalidCharacters_rejectedWithExactMessage() {
        for (String input : new String[] {"/", "Needs/Wants", "r/note", "Plan!", "Plan?", "A_B", "A:B", "A;B",
            "A@B", "A#B", "A$B", "A%B", "A+B", "[A]", "{A}", "A\\B", "A\"B", "A\tB", "A\nB",
            "A\rB", "A\u00a0B", "A\uD83D\uDE00B", "e\u0301", "A\u2013B"}) {
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
    public void unicodeWhitespace_rejectedWithoutTrimming() {
        for (String whitespace : new String[] {"\u0085", "\u00a0", "\u2003", "\u2007", "\u202f"}) {
            assertInvalid("  " + whitespace + "  ", FinancialNeed.MESSAGE_INVALID_CHARACTERS);
            assertInvalid(whitespace + "Retirement", FinancialNeed.MESSAGE_INVALID_CHARACTERS);
            assertInvalid("Retirement" + whitespace, FinancialNeed.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void validationOrder_lengthBeforeInvalidCharacters() {
        assertInvalid("A".repeat(199) + "!", FinancialNeed.MESSAGE_INVALID_CHARACTERS);
        for (String input : new String[] {"A".repeat(200) + "!", "A".repeat(200) + "\t",
            "  " + "A".repeat(200) + "!  ", "\u2003".repeat(201)}) {
            assertInvalid(input, FinancialNeed.MESSAGE_LENGTH);
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
