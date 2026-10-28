package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Represents a Person's name in the client book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_BLANK = "Name cannot be blank.";
    public static final String MESSAGE_INVALID_CHARACTERS = "The name contains invalid characters. "
            + "A name may contain only letters, spaces, hyphens (-), apostrophes ('), "
            + "and the relationship markers s/o, d/o, or w/o.";
    public static final String MESSAGE_LENGTH = "The name must not exceed 100 characters.";

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
        String validationError = getValidationError(name);
        checkArgument(validationError == null, validationError);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return getValidationError(test) == null;
    }

    /**
     * Returns the reason a name is invalid, or {@code null} if it is valid.
     *
     * @param name The name to validate.
     * @return The validation error, or {@code null} when valid.
     */
    public static String getValidationError(String name) {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            return MESSAGE_LENGTH;
        }
        if (trimmedName.isEmpty()) {
            return MESSAGE_BLANK;
        }
        if (!VALID_NAME_PATTERN.matcher(trimmedName).matches()) {
            return MESSAGE_INVALID_CHARACTERS;
        }
        return null;
    }

    /**
     * Returns whether this name and {@code otherName} are equal after ignoring letter case and redundant spaces.
     */
    public boolean hasSameName(Name otherName) {
        requireNonNull(otherName);
        return getNormalizedName().equals(otherName.getNormalizedName());
    }

    private String getNormalizedName() {
        return fullName.trim().replaceAll(" +", " ").toLowerCase(Locale.ROOT);
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
