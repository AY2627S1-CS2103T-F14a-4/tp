package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PriorityTest {

    @Test
    public void nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Priority(null));
        assertThrows(NullPointerException.class, () -> Priority.isValidPriority(null));
        assertThrows(NullPointerException.class, () -> Priority.getValidationError(null));
    }

    @Test
    public void validValues_accepted() {
        for (String input : new String[] {"A", "1", "Retirement 2030", "Family's long-term needs (A & B), etc.",
            "\u4fdd\u969c", "\u0661\uff12", "\ud801\udc00".repeat(100), ".,'-()&", "A".repeat(199),
            "A".repeat(200), "  " + "A".repeat(200) + "  "}) {
            assertTrue(Priority.isValidPriority(input));
            assertNull(Priority.getValidationError(input));
            assertEquals(input.trim(), new Priority(input).value);
        }
    }

    @Test
    public void financialNotation_preservesValuesAndBoundaries() {
        for (String input : new String[] {"Retire on $3000 monthly", "Target 4% return", "CPF + SRS",
            "Life/Health insurance", "Target 6% return", "CPF/SRS planning", "Needs/Wants", "r/note"}) {
            Priority value = new Priority("  " + input + "  ");
            assertTrue(Priority.isValidPriority(input));
            assertNull(Priority.getValidationError(input));
            assertEquals(input, value.value);
            assertEquals(new Priority(input), value);
            assertEquals(new Priority(input).hashCode(), value.hashCode());
        }
        for (String symbol : new String[] {"$", "%", "+", "/"}) {
            assertEquals(symbol, new Priority(symbol).value);
            String boundary = symbol.repeat(200);
            assertEquals(boundary, new Priority("  " + boundary + "  ").value);
            assertInvalid(boundary + symbol, Priority.MESSAGE_LENGTH);
            assertInvalid(symbol + "\t", Priority.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void unicodeCombiningMarks_preservesTextWithoutNormalization() {
        for (String input : new String[] {"\u00e9pargne", "e\u0301pargne",
            "\u0b93\u0baf\u0bcd\u0bb5\u0bc2\u0ba4\u0bbf\u0baf\u0bae\u0bcd",
            "\u0915\u093e", "\u05e9\u05b8\u05c1", "1\u20dd", "e\u0301",
            "e\u0301pargne $3000 + CPF/SRS 4%"}) {
            Priority value = new Priority("  " + input + "  ");
            assertTrue(Priority.isValidPriority(input));
            assertNull(Priority.getValidationError(input));
            assertEquals(input, value.value);
            assertEquals(input, value.toString());
            assertEquals(new Priority(input), value);
            assertEquals(new Priority(input).hashCode(), value.hashCode());
        }
        assertFalse(new Priority("\u00e9pargne").equals(new Priority("e\u0301pargne")));
    }

    @Test
    public void unicodeCombiningMarks_preservesLengthAndControlRestrictions() {
        String boundary = "e\u0301".repeat(100);
        assertEquals(boundary, new Priority("  " + boundary + "  ").value);
        assertInvalid(boundary + "\u0301", Priority.MESSAGE_LENGTH);
        assertInvalid(boundary + "\t", Priority.MESSAGE_LENGTH);
        for (String control : new String[] {"\t", "\n", "\r", "\f", "\u0000", "\u007f"}) {
            assertInvalid(control + "e\u0301pargne", Priority.MESSAGE_INVALID_CHARACTERS);
            assertInvalid("e\u0301pargne" + control, Priority.MESSAGE_INVALID_CHARACTERS);
            assertInvalid("e\u0301" + control + "pargne", Priority.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void constructor_trimsEdgesAndPreservesInternalSpaces() {
        Priority result = new Priority("  Long  term   plans  ");
        assertEquals("Long  term   plans", result.value);
        assertEquals(result.value, result.toString());
    }

    @Test
    public void emptyValues_rejectedWithExactMessage() {
        for (String input : new String[] {"", " ", "   ", " ".repeat(201)}) {
            assertInvalid(input, "Priority cannot be empty.");
        }
    }

    @Test
    public void overlongValues_rejectedWithExactMessage() {
        assertInvalid("A".repeat(201), "Priority must not exceed 200 characters.");
        assertInvalid("  " + "A".repeat(201) + "  ", "Priority must not exceed 200 characters.");
        assertInvalid("\ud801\udc00".repeat(100) + "A", "Priority must not exceed 200 characters.");
    }

    @Test
    public void invalidCharacters_rejectedWithExactMessage() {
        for (String input : new String[] {"Plan!", "Plan?", "A_B", "A:B", "A;B",
            "A@B", "A#B", "[A]", "{A}", "A\\B", "A\"B", "A\tB", "A\nB",
            "A\rB", "A\u00a0B", "A\uD83D\uDE00B", "A\u2013B"}) {
            assertInvalid(input, "Priority contains invalid characters.");
        }
    }

    @Test
    public void edgeControlCharacters_rejectedWithExactMessage() {
        for (String control : new String[] {"\t", "\n", "\r", "\f", "\u0000"}) {
            assertInvalid(control + "Retirement", "Priority contains invalid characters.");
            assertInvalid("Retirement" + control, "Priority contains invalid characters.");
            assertInvalid("  " + control + "Retirement  ", "Priority contains invalid characters.");
            assertInvalid("  Retirement" + control + "  ", "Priority contains invalid characters.");
            assertInvalid("  " + control + "  ", "Priority contains invalid characters.");
        }
    }

    @Test
    public void unicodeWhitespace_rejectedWithoutTrimming() {
        for (String whitespace : new String[] {"\u0085", "\u00a0", "\u2003", "\u2007", "\u202f"}) {
            assertInvalid("  " + whitespace + "  ", Priority.MESSAGE_INVALID_CHARACTERS);
            assertInvalid(whitespace + "Retirement", Priority.MESSAGE_INVALID_CHARACTERS);
            assertInvalid("Retirement" + whitespace, Priority.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void validationOrder_lengthBeforeInvalidCharacters() {
        assertInvalid("A".repeat(199) + "!", Priority.MESSAGE_INVALID_CHARACTERS);
        for (String input : new String[] {"A".repeat(200) + "!", "A".repeat(200) + "\t",
            "  " + "A".repeat(200) + "!  ", "\u2003".repeat(201)}) {
            assertInvalid(input, Priority.MESSAGE_LENGTH);
        }
    }

    @Test
    public void equalsAndHashCode_useTrimmedValue() {
        Priority value = new Priority("Long term");
        Priority sameValue = new Priority("  Long term  ");
        assertTrue(value.equals(value));
        assertTrue(value.equals(sameValue));
        assertEquals(value.hashCode(), sameValue.hashCode());
        assertFalse(value.equals(null));
        assertFalse(value.equals("Long term"));
        assertFalse(value.equals(new Priority("Other plan")));
        assertFalse(value.equals(new Priority("Long  term")));
    }

    private void assertInvalid(String input, String message) {
        assertFalse(Priority.isValidPriority(input));
        assertEquals(message, Priority.getValidationError(input));
        assertThrows(IllegalArgumentException.class, message, () -> new Priority(input));
    }
}
