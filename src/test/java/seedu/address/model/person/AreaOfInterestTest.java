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
        for (String input : new String[] {"/", "Needs/Wants", "r/note", "Plan!", "Plan?", "A_B", "A:B", "A;B",
            "A@B", "A#B", "A$B", "A%B", "A+B", "[A]", "{A}", "A\\B", "A\"B", "A\tB", "A\nB",
            "A\rB", "A\u00a0B", "A\uD83D\uDE00B", "e\u0301", "A\u2013B"}) {
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
