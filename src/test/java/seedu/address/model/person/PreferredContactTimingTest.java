package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PreferredContactTimingTest {
    @Test
    public void constructor_validPartitionsAndBoundaries_preservesTrimmedText() {
        for (String text : List.of("a", "a".repeat(199), "a".repeat(200), "WhatsApp + phone",
                "Weekdays 6:30-8 pm", "Email  or  phone", "https://example.com", "\u5ba2\u6237")) {
            assertTrue(PreferredContactTiming.isValidPreferredContactTiming(text));
            assertEquals(text, new PreferredContactTiming(" \t" + text + "\t ").value);
        }
    }

    @Test
    public void constructor_invalidPartitions_throws() {
        assertThrows(NullPointerException.class, () -> new PreferredContactTiming(null));
        assertThrows(NullPointerException.class, () -> PreferredContactTiming.isValidPreferredContactTiming(null));
        for (String text : List.of("", " ", "\t\n", "\u2003", "a".repeat(201))) {
            assertFalse(PreferredContactTiming.isValidPreferredContactTiming(text));
            assertThrows(IllegalArgumentException.class, PreferredContactTiming.MESSAGE_CONSTRAINTS, () ->
                    new PreferredContactTiming(text));
        }
    }

    @Test
    public void equalsAndHashCode_valueSemantics() {
        PreferredContactTiming value = new PreferredContactTiming("Phone");
        assertEquals(value, value);
        assertEquals(value, new PreferredContactTiming(" Phone "));
        assertEquals(value.hashCode(), new PreferredContactTiming("Phone").hashCode());
        assertNotEquals(value, new PreferredContactTiming("Email"));
        assertNotEquals(value, "Phone");
        assertNotEquals(null, value);
        assertEquals("Phone", value.toString());
    }
}
