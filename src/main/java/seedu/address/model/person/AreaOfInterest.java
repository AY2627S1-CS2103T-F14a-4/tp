package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a person's area of interest.
 * Guarantees: immutable; is valid as declared in {@link #isValidAreaOfInterest(String)}.
 */
public class AreaOfInterest {

    public static final String MESSAGE_BLANK = "Area of interest cannot be empty.";
    public static final String MESSAGE_LENGTH = "Area of interest must not exceed 200 characters.";
    public static final String MESSAGE_INVALID_CHARACTERS = "Area of interest contains invalid characters";

    public final String value;

    /**
     * Constructs a {@code AreaOfInterest}.
     *
     * @param areaOfInterest A valid area of interest.
     */
    public AreaOfInterest(String areaOfInterest) {
        requireNonNull(areaOfInterest);
        String validationError = getValidationError(areaOfInterest);
        checkArgument(validationError == null, validationError);
        value = FinancialInformationValidator.trimSpaces(areaOfInterest);
    }

    /**
     * Returns true if a given string is a valid area of interest.
     */
    public static boolean isValidAreaOfInterest(String test) {
        return getValidationError(test) == null;
    }

    /**
     * Returns the reason a area of interest is invalid, or {@code null} if it is valid.
     *
     * @param areaOfInterest The area of interest to validate.
     * @return The validation error, or {@code null} when valid.
     */
    public static String getValidationError(String areaOfInterest) {
        return FinancialInformationValidator.getValidationError(areaOfInterest,
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
        if (!(other instanceof AreaOfInterest otherValue)) {
            return false;
        }

        return value.equals(otherValue.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
