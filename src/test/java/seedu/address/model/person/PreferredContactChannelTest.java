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
                "Weekdays 6:30\u20138 pm", "Email  or  phone", "https://example.com", "\u5ba2\u6237",
                "Phone/WhatsApp", "Email\u00a0or\u2007phone\u202f(\u665a\u4e0a)!", "Email\u0085or phone")) {
            assertTrue(PreferredContactChannel.isValidPreferredContactChannel(text));
            assertEquals(text, new PreferredContactChannel(" \t" + text + "\t ").value);
            String padded = " \u00a0\t\u2007\u0085\u001c" + text + "\u0085\u202f\u2003\n ";
            assertTrue(PreferredContactChannel.isValidPreferredContactChannel(padded));
            assertEquals(text, new PreferredContactChannel(padded).value);
        }
    }

    @Test
    public void constructor_invalidPartitions_throws() {
        assertThrows(NullPointerException.class, () -> new PreferredContactChannel(null));
        assertThrows(NullPointerException.class, () -> PreferredContactChannel.isValidPreferredContactChannel(null));
        for (String text : List.of("", " ", "\t\n", "\u2003", "\u001c", "a".repeat(201),
                "\u00a0" + "a".repeat(201) + "\u202f")) {
            assertFalse(PreferredContactChannel.isValidPreferredContactChannel(text));
            assertThrows(IllegalArgumentException.class, PreferredContactChannel.MESSAGE_CONSTRAINTS, () ->
                    new PreferredContactChannel(text));
        }
    }

    @Test
    public void constructor_nonBreakingWhitespaceOnly_throws() {
        for (String text : List.of("\u00a0", "\u2007", "\u202f", " \t\u2003\u00a0\u2007\u202f\n ")) {
            assertFalse(PreferredContactChannel.isValidPreferredContactChannel(text));
            assertThrows(IllegalArgumentException.class, PreferredContactChannel.MESSAGE_CONSTRAINTS, () ->
                    new PreferredContactChannel(text));
        }
    }

    @Test
    public void constructor_nextLineWhitespaceOnly_throws() {
        for (String text : List.of("\u0085", " \t\u00a0\u0085\u001c ")) {
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
