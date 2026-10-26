package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Person's phone number in the client book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_BLANK = "Phone number cannot be blank.";
    public static final String MESSAGE_CONTROL_CHARACTERS =
            "Phone number cannot contain line breaks or other control characters.";
    public static final String MESSAGE_NO_DIGITS = "Phone number must contain at least one digit.";
    public static final String MESSAGE_INCOMPLETE =
            "Phone number must contain at least one complete sequence of 8 digits.";
    public static final String MESSAGE_TOO_LONG = "Each phone number can contain at most 15 digits.";
    private static final int MIN_PHONE_NUMBER_LENGTH = 8;
    private static final int MAX_PHONE_NUMBER_LENGTH = 15;
    private static final Pattern PHONE_NUMBER_SEQUENCE =
            Pattern.compile("\\+?\\d(?:[\\s().-]*\\d)*");
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
        Matcher matcher = PHONE_NUMBER_SEQUENCE.matcher(phone);
        boolean hasCompletePhoneNumber = false;
        while (matcher.find()) {
            int digitCount = countDigits(matcher.group());
            if (digitCount > MAX_PHONE_NUMBER_LENGTH) {
                return MESSAGE_TOO_LONG;
            }
            if (digitCount >= MIN_PHONE_NUMBER_LENGTH) {
                hasCompletePhoneNumber = true;
            }
        }
        if (!hasCompletePhoneNumber) {
            return MESSAGE_INCOMPLETE;
        }
        return null;
    }

    /**
     * Returns whether this phone number and {@code otherPhone} contain at least one identical complete number.
     */
    public boolean hasSamePhoneNumber(Phone otherPhone) {
        requireNonNull(otherPhone);
        Set<String> phoneNumbers = getCompletePhoneNumbers();
        return otherPhone.getCompletePhoneNumbers().stream().anyMatch(phoneNumbers::contains);
    }

    private Set<String> getCompletePhoneNumbers() {
        Set<String> phoneNumbers = new HashSet<>();
        Matcher matcher = PHONE_NUMBER_SEQUENCE.matcher(value);
        while (matcher.find()) {
            String phoneNumber = matcher.group().replaceAll("\\D", "");
            if (phoneNumber.length() >= MIN_PHONE_NUMBER_LENGTH) {
                phoneNumbers.add(phoneNumber);
            }
        }
        return phoneNumbers;
    }

    private static int countDigits(String phoneNumber) {
        return (int) phoneNumber.chars().filter(Character::isDigit).count();
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
