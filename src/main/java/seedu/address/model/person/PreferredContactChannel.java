package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.nio.charset.StandardCharsets;

/** An immutable, optional preferred contact channel, with surrounding whitespace removed. */
public final class PreferredContactChannel {
    public static final String MESSAGE_CONSTRAINTS =
            "Preferred contact channel must contain 1 to 200 characters of valid Unicode text.";

    public final String value;

    /** Creates a preference containing 1 to 200 characters after trimming. */
    public PreferredContactChannel(String value) {
        requireNonNull(value);
        checkArgument(isValidPreferredContactChannel(value), MESSAGE_CONSTRAINTS);
        this.value = stripWhitespace(value);
    }

    /** Free text must contain 1 to 200 characters after surrounding whitespace is removed. */
    public static boolean isValidPreferredContactChannel(String value) {
        requireNonNull(value);
        String trimmed = stripWhitespace(value);
        return !trimmed.isEmpty() && trimmed.length() <= 200
                && StandardCharsets.UTF_8.newEncoder().canEncode(trimmed);
    }

    /** Removes surrounding Java whitespace and Unicode White_Space characters. */
    private static String stripWhitespace(String value) {
        return value.replaceAll(
                "^[\\p{javaWhitespace}\\p{IsWhite_Space}]+|[\\p{javaWhitespace}\\p{IsWhite_Space}]+$", "");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PreferredContactChannel preference && value.equals(preference.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
