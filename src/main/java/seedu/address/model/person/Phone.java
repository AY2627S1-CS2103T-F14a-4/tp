package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_BLANK = "Phone number cannot be blank.";
    public static final String MESSAGE_CONTROL_CHARACTERS =
            "Phone number cannot contain line breaks or other control characters.";
    public static final String MESSAGE_NO_DIGITS = "Phone number must contain at least one digit.";
    public static final String MESSAGE_INCOMPLETE =
            "Phone number must contain at least one complete sequence of 8 digits.";
    private static final Pattern FULL_PHONE_NUMBER =
            Pattern.compile("(?:\\+?\\d)(?:[\\s().-]*\\d){7,}");
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        String validationError = getValidationError(phone);
        checkArgument(validationError == null, validationError);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return getValidationError(test) == null;
    }

    /**
     * Returns the reason a phone number is invalid, or {@code null} if it is valid.
     *
     * @param phone The phone number to validate.
     * @return The validation error, or {@code null} when valid.
     */
    public static String getValidationError(String phone) {
        requireNonNull(phone);

        if (phone.isBlank()) {
            return MESSAGE_BLANK;
        }
        if (phone.chars().anyMatch(Character::isISOControl)) {
            return MESSAGE_CONTROL_CHARACTERS;
        }
        if (phone.chars().noneMatch(Character::isDigit)) {
            return MESSAGE_NO_DIGITS;
        }
        if (!FULL_PHONE_NUMBER.matcher(phone).find()) {
            return MESSAGE_INCOMPLETE;
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
