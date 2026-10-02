package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names must contain 1 to 100 characters after trimming. They may contain letters, spaces, hyphens, "
                    + "apostrophes, and the relationship markers s/o, d/o, or w/o.";

    private static final int MAX_NAME_LENGTH = 100;
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile(
            "[\\p{L}]+(?:(?: +(?:s/o|d/o|w/o) +[\\p{L}]+)|(?: +|[-'])[\\p{L}]+)*",
            Pattern.CASE_INSENSITIVE);

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);
        String trimmedName = test.trim();
        return !trimmedName.isEmpty()
                && trimmedName.length() <= MAX_NAME_LENGTH
                && VALID_NAME_PATTERN.matcher(trimmedName).matches();
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
