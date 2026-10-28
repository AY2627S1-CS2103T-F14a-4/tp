package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AreaOfInterestTest {

    @Test
    public void nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AreaOfInterest(null));
        assertThrows(NullPointerException.class, () -> AreaOfInterest.isValidAreaOfInterest(null));
        assertThrows(NullPointerException.class, () -> AreaOfInterest.getValidationError(null));
    }

    @Test
    public void validValues_accepted() {
        for (String input : new String[] {"A", "1", "Retirement 2030", "Family's long-term needs (A & B), etc.",
            "\u4fdd\u969c", "\u0661\uff12", "\ud801\udc00".repeat(100), ".,'-()&", "A".repeat(199),
            "A".repeat(200), "  " + "A".repeat(200) + "  "}) {
            assertTrue(AreaOfInterest.isValidAreaOfInterest(input));
            assertNull(AreaOfInterest.getValidationError(input));
            assertEquals(input.trim(), new AreaOfInterest(input).value);
        }
    }

    @Test
    public void financialNotation_preservesValuesAndBoundaries() {
        for (String input : new String[] {"Retire on $3000 monthly", "Target 4% return", "CPF + SRS",
            "Life/Health insurance", "Target 6% return", "CPF/SRS planning", "Needs/Wants", "r/note"}) {
            AreaOfInterest value = new AreaOfInterest("  " + input + "  ");
            assertTrue(AreaOfInterest.isValidAreaOfInterest(input));
            assertNull(AreaOfInterest.getValidationError(input));
            assertEquals(input, value.value);
            assertEquals(new AreaOfInterest(input), value);
            assertEquals(new AreaOfInterest(input).hashCode(), value.hashCode());
        }
        for (String symbol : new String[] {"$", "%", "+", "/"}) {
            assertEquals(symbol, new AreaOfInterest(symbol).value);
            String boundary = symbol.repeat(200);
            assertEquals(boundary, new AreaOfInterest("  " + boundary + "  ").value);
            assertInvalid(boundary + symbol, AreaOfInterest.MESSAGE_LENGTH);
            assertInvalid(symbol + "\t", AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void unicodeCombiningMarks_preservesTextWithoutNormalization() {
        for (String input : new String[] {"\u00e9pargne", "e\u0301pargne",
            "\u0b93\u0baf\u0bcd\u0bb5\u0bc2\u0ba4\u0bbf\u0baf\u0bae\u0bcd",
            "\u0915\u093e", "\u05e9\u05b8\u05c1", "1\u20dd", "e\u0301",
            "e\u0301pargne $3000 + CPF/SRS 4%"}) {
            AreaOfInterest value = new AreaOfInterest("  " + input + "  ");
            assertTrue(AreaOfInterest.isValidAreaOfInterest(input));
            assertNull(AreaOfInterest.getValidationError(input));
            assertEquals(input, value.value);
            assertEquals(input, value.toString());
            assertEquals(new AreaOfInterest(input), value);
            assertEquals(new AreaOfInterest(input).hashCode(), value.hashCode());
        }
        assertFalse(new AreaOfInterest("\u00e9pargne").equals(new AreaOfInterest("e\u0301pargne")));
    }

    @Test
    public void unicodeCombiningMarks_preservesLengthAndControlRestrictions() {
        String boundary = "e\u0301".repeat(100);
        assertEquals(boundary, new AreaOfInterest("  " + boundary + "  ").value);
        assertInvalid(boundary + "\u0301", AreaOfInterest.MESSAGE_LENGTH);
        assertInvalid(boundary + "\t", AreaOfInterest.MESSAGE_LENGTH);
        for (String control : new String[] {"\t", "\n", "\r", "\f", "\u0000", "\u007f"}) {
            assertInvalid(control + "e\u0301pargne", AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
            assertInvalid("e\u0301pargne" + control, AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
            assertInvalid("e\u0301" + control + "pargne", AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void constructor_trimsEdgesAndPreservesInternalSpaces() {
        AreaOfInterest result = new AreaOfInterest("  Long  term   plans  ");
        assertEquals("Long  term   plans", result.value);
        assertEquals(result.value, result.toString());
    }

    @Test
    public void emptyValues_rejectedWithExactMessage() {
        for (String input : new String[] {"", " ", "   ", " ".repeat(201)}) {
            assertInvalid(input, "Area of interest cannot be empty.");
        }
    }

    @Test
    public void overlongValues_rejectedWithExactMessage() {
        assertInvalid("A".repeat(201), "Area of interest must not exceed 200 characters.");
        assertInvalid("  " + "A".repeat(201) + "  ", "Area of interest must not exceed 200 characters.");
        assertInvalid("\ud801\udc00".repeat(100) + "A", "Area of interest must not exceed 200 characters.");
    }

    @Test
    public void invalidCharacters_rejectedWithExactMessage() {
        for (String input : new String[] {"Plan!", "Plan?", "A_B", "A:B", "A;B",
            "A@B", "A#B", "[A]", "{A}", "A\\B", "A\"B", "A\tB", "A\nB",
            "A\rB", "A\u00a0B", "A\uD83D\uDE00B", "A\u2013B"}) {
            assertInvalid(input, "Area of interest contains invalid characters");
        }
    }

    @Test
    public void edgeControlCharacters_rejectedWithExactMessage() {
        for (String control : new String[] {"\t", "\n", "\r", "\f", "\u0000"}) {
            assertInvalid(control + "Retirement", "Area of interest contains invalid characters");
            assertInvalid("Retirement" + control, "Area of interest contains invalid characters");
            assertInvalid("  " + control + "Retirement  ", "Area of interest contains invalid characters");
            assertInvalid("  Retirement" + control + "  ", "Area of interest contains invalid characters");
            assertInvalid("  " + control + "  ", "Area of interest contains invalid characters");
        }
    }

    @Test
    public void unicodeWhitespace_rejectedWithoutTrimming() {
        for (String whitespace : new String[] {"\u0085", "\u00a0", "\u2003", "\u2007", "\u202f"}) {
            assertInvalid("  " + whitespace + "  ", AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
            assertInvalid(whitespace + "Retirement", AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
            assertInvalid("Retirement" + whitespace, AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void validationOrder_lengthBeforeInvalidCharacters() {
        assertInvalid("A".repeat(199) + "!", AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        for (String input : new String[] {"A".repeat(200) + "!", "A".repeat(200) + "\t",
            "  " + "A".repeat(200) + "!  ", "\u2003".repeat(201)}) {
            assertInvalid(input, AreaOfInterest.MESSAGE_LENGTH);
        }
    }

    @Test
    public void equalsAndHashCode_useTrimmedValue() {
        AreaOfInterest value = new AreaOfInterest("Long term");
        AreaOfInterest sameValue = new AreaOfInterest("  Long term  ");
        assertTrue(value.equals(value));
        assertTrue(value.equals(sameValue));
        assertEquals(value.hashCode(), sameValue.hashCode());
        assertFalse(value.equals(null));
        assertFalse(value.equals("Long term"));
        assertFalse(value.equals(new AreaOfInterest("Other plan")));
        assertFalse(value.equals(new AreaOfInterest("Long  term")));
    }

    private void assertInvalid(String input, String message) {
        assertFalse(AreaOfInterest.isValidAreaOfInterest(input));
        assertEquals(message, AreaOfInterest.getValidationError(input));
        assertThrows(IllegalArgumentException.class, message, () -> new AreaOfInterest(input));
    }
}
