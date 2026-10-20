package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/** An immutable, optional preferred contact timing, with surrounding whitespace removed. */
public final class PreferredContactTiming {
    public static final String MESSAGE_CONSTRAINTS = "Preferred contact timing must contain 1 to 200 characters.";

    public final String value;

    /** Creates a preference containing 1 to 200 characters after trimming. */
    public PreferredContactTiming(String value) {
        requireNonNull(value);
        checkArgument(isValidPreferredContactTiming(value), MESSAGE_CONSTRAINTS);
        this.value = value.strip();
    }

    /** Free text follows the same character policy as next-action descriptions. */
    public static boolean isValidPreferredContactTiming(String value) {
        requireNonNull(value);
        String trimmed = value.strip();
        return !trimmed.isEmpty() && trimmed.length() <= 200;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PreferredContactTiming preference && value.equals(preference.value);
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
