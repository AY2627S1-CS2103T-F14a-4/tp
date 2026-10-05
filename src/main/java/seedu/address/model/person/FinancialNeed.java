package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a person's financial need.
 * Guarantees: immutable; is valid as declared in {@link #isValidFinancialNeed(String)}.
 */
public class FinancialNeed {

    public static final String MESSAGE_BLANK = "Financial need cannot be empty.";
    public static final String MESSAGE_LENGTH = "Financial need must not exceed 200 characters.";
    public static final String MESSAGE_INVALID_CHARACTERS = "Financial need contains invalid characters";

    private static final int MAX_LENGTH = 200;
    private static final Pattern VALID_PATTERN = Pattern.compile("[\\p{L}\\p{Nd} .,'()&-]+");

    public final String value;

    /**
     * Constructs a {@code FinancialNeed}.
     *
     * @param financialNeed A valid financial need.
     */
    public FinancialNeed(String financialNeed) {
        requireNonNull(financialNeed);
        String validationError = getValidationError(financialNeed);
        checkArgument(validationError == null, validationError);
        value = trimSpaces(financialNeed);
    }

    /**
     * Returns true if a given string is a valid financial need.
     */
    public static boolean isValidFinancialNeed(String test) {
        return getValidationError(test) == null;
    }

    /**
     * Returns the reason a financial need is invalid, or {@code null} if it is valid.
     *
     * @param financialNeed The financial need to validate.
     * @return The validation error, or {@code null} when valid.
     */
    public static String getValidationError(String financialNeed) {
        requireNonNull(financialNeed);
        String trimmedValue = trimSpaces(financialNeed);
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
        if (!(other instanceof FinancialNeed otherValue)) {
            return false;
        }

        return value.equals(otherValue.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
