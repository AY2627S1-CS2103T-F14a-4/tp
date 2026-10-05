package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a person's priority.
 * Guarantees: immutable; is valid as declared in {@link #isValidPriority(String)}.
 */
public class Priority {

    public static final String MESSAGE_BLANK = "Priority cannot be empty.";
    public static final String MESSAGE_LENGTH = "Priority must not exceed 200 characters.";
    public static final String MESSAGE_INVALID_CHARACTERS = "Priority contains invalid characters.";

    private static final int MAX_LENGTH = 200;
    private static final Pattern VALID_PATTERN = Pattern.compile("[\\p{L}\\p{Nd} .,'()&-]+");

    public final String value;

    /**
     * Constructs a {@code Priority}.
     *
     * @param priority A valid priority.
     */
    public Priority(String priority) {
        requireNonNull(priority);
        String validationError = getValidationError(priority);
        checkArgument(validationError == null, validationError);
        value = trimSpaces(priority);
    }

    /**
     * Returns true if a given string is a valid priority.
     */
    public static boolean isValidPriority(String test) {
        return getValidationError(test) == null;
    }

    /**
     * Returns the reason a priority is invalid, or {@code null} if it is valid.
     *
     * @param priority The priority to validate.
     * @return The validation error, or {@code null} when valid.
     */
    public static String getValidationError(String priority) {
        requireNonNull(priority);
        String trimmedValue = trimSpaces(priority);
        if (trimmedValue.isEmpty()) {
            return MESSAGE_BLANK;
        }
        if (trimmedValue.length() > MAX_LENGTH) {
            return MESSAGE_LENGTH;
        }
        if (!VALID_PATTERN.matcher(trimmedValue).matches()) {
            return MESSAGE_INVALID_CHARACTERS;
        }
        return null;
    }

    /**
     * Removes only leading and trailing ordinary spaces, preserving all other characters for validation.
     */
    private static String trimSpaces(String input) {
        int start = 0;
        int end = input.length();
        while (start < end && input.charAt(start) == ' ') {
            start++;
        }
        while (end > start && input.charAt(end - 1) == ' ') {
            end--;
        }
        return input.substring(start, end);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Priority otherValue)) {
            return false;
        }

        return value.equals(otherValue.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
