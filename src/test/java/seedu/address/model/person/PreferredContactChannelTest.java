package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PreferredContactChannelTest {
    @Test
    public void constructor_validPartitionsAndBoundaries_preservesTrimmedText() {
        for (String text : List.of("a", "a".repeat(199), "a".repeat(200), "WhatsApp + phone",
                "Weekdays 6:30-8 pm", "Email  or  phone", "https://example.com", "\u5ba2\u6237")) {
            assertTrue(PreferredContactChannel.isValidPreferredContactChannel(text));
            assertEquals(text, new PreferredContactChannel(" \t" + text + "\t ").value);
        }
    }

    @Test
    public void constructor_invalidPartitions_throws() {
        assertThrows(NullPointerException.class, () -> new PreferredContactChannel(null));
        assertThrows(NullPointerException.class, () -> PreferredContactChannel.isValidPreferredContactChannel(null));
        for (String text : List.of("", " ", "\t\n", "\u2003", "a".repeat(201))) {
            assertFalse(PreferredContactChannel.isValidPreferredContactChannel(text));
            assertThrows(IllegalArgumentException.class, PreferredContactChannel.MESSAGE_CONSTRAINTS, () ->
                    new PreferredContactChannel(text));
        }
    }

    @Test
    public void equalsAndHashCode_valueSemantics() {
        PreferredContactChannel value = new PreferredContactChannel("Phone");
        assertEquals(value, value);
        assertEquals(value, new PreferredContactChannel(" Phone "));
        assertEquals(value.hashCode(), new PreferredContactChannel("Phone").hashCode());
        assertNotEquals(value, new PreferredContactChannel("Email"));
        assertNotEquals(value, "Phone");
        assertNotEquals(null, value);
        assertEquals("Phone", value.toString());
    }
}
