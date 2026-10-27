package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;

/** Shared validation and ordinary-space trimming for financial information value objects. */
final class FinancialInformationValidator {
    private static final int MAX_LENGTH = 200;
    private static final Pattern VALID_PATTERN = Pattern.compile("[\\p{L}\\p{Nd} .,'()&-]+");

    private FinancialInformationValidator() {}

    /** Returns the first validation error using the supplied field-specific messages, or null when valid. */
    static String getValidationError(String value, String blankMessage, String lengthMessage,
            String invalidCharactersMessage) {
        requireNonNull(value);
        String trimmedValue = trimSpaces(value);
        if (trimmedValue.isEmpty()) {
            return blankMessage;
        }
        if (trimmedValue.length() > MAX_LENGTH) {
            return lengthMessage;
        }
        if (!VALID_PATTERN.matcher(trimmedValue).matches()) {
            return invalidCharactersMessage;
        }
        return null;
    }

    /** Removes only leading and trailing ordinary spaces, preserving all other characters for validation. */
    static String trimSpaces(String input) {
        requireNonNull(input);
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
}
