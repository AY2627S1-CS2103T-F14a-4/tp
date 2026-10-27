package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a person's priority.
 * Guarantees: immutable; is valid as declared in {@link #isValidPriority(String)}.
 */
public class Priority {

    public static final String MESSAGE_BLANK = "Priority cannot be empty.";
    public static final String MESSAGE_LENGTH = "Priority must not exceed 200 characters.";
    public static final String MESSAGE_INVALID_CHARACTERS = "Priority contains invalid characters.";

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
        value = FinancialInformationValidator.trimSpaces(priority);
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
        return FinancialInformationValidator.getValidationError(priority,
                MESSAGE_BLANK, MESSAGE_LENGTH, MESSAGE_INVALID_CHARACTERS);
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
