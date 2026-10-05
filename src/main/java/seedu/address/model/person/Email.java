package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_BLANK = "Email cannot be blank.";
    public static final String MESSAGE_LENGTH = "Email must contain between 3 and 254 characters.";
    public static final String MESSAGE_CONTROL_CHARACTERS =
            "Email cannot contain line breaks or other control characters.";
    public static final String MESSAGE_WHITESPACE = "Email cannot contain spaces or other whitespace characters.";
    public static final String MESSAGE_AT_SIGN =
            "Email must contain exactly one @ symbol, with text before and after it.";
    public static final String MESSAGE_LOCAL_PART_DOTS =
            "The part before @ cannot start or end with a dot, or contain consecutive dots.";
    public static final String MESSAGE_LOCAL_PART_CHARACTERS =
            "The part before @ contains unsupported characters.";
    public static final String MESSAGE_DOMAIN_EMPTY_LABEL =
            "The domain cannot start, end, or contain consecutive dots.";
    public static final String MESSAGE_DOMAIN_LABEL =
            "Each domain label must use letters, digits, or hyphens, and cannot start or end with a hyphen.";

    private static final int MIN_EMAIL_LENGTH = 3;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern LOCAL_PART_PATTERN =
            Pattern.compile("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+");
    private static final Pattern DOMAIN_LABEL_PATTERN =
            Pattern.compile("[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?");

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        String validationError = getValidationError(email);
        checkArgument(validationError == null, validationError);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        return getValidationError(test) == null;
    }

    /**
     * Returns the reason an email is invalid, or {@code null} if it is valid.
     *
     * @param email The email to validate.
     * @return The validation error, or {@code null} when valid.
     */
    public static String getValidationError(String email) {
        requireNonNull(email);

        if (email.isBlank()) {
            return MESSAGE_BLANK;
        }
        if (email.length() < MIN_EMAIL_LENGTH || email.length() > MAX_EMAIL_LENGTH) {
            return MESSAGE_LENGTH;
        }
        if (email.chars().anyMatch(Character::isISOControl)) {
            return MESSAGE_CONTROL_CHARACTERS;
        }
        if (email.chars().anyMatch(Character::isWhitespace)) {
            return MESSAGE_WHITESPACE;
        }

        int atSignIndex = email.indexOf('@');
        if (atSignIndex <= 0 || atSignIndex != email.lastIndexOf('@') || atSignIndex == email.length() - 1) {
            return MESSAGE_AT_SIGN;
        }

        String localPart = email.substring(0, atSignIndex);
        String domain = email.substring(atSignIndex + 1);
        if (localPart.startsWith(".") || localPart.endsWith(".") || localPart.contains("..")) {
            return MESSAGE_LOCAL_PART_DOTS;
        }
        if (!LOCAL_PART_PATTERN.matcher(localPart).matches()) {
            return MESSAGE_LOCAL_PART_CHARACTERS;
        }

        String[] domainLabels = domain.split("\\.", -1);
        for (String domainLabel : domainLabels) {
            if (domainLabel.isEmpty()) {
                return MESSAGE_DOMAIN_EMPTY_LABEL;
            }
            if (!DOMAIN_LABEL_PATTERN.matcher(domainLabel).matches()) {
                return MESSAGE_DOMAIN_LABEL;
            }
        }
        return null;
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
