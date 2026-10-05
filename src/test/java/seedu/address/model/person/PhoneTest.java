package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("9123")); // fewer than 8 digits
        assertFalse(Phone.isValidPhone("phone")); // no digits
        assertFalse(Phone.isValidPhone("1234abc5678")); // no complete digit sequence
        assertFalse(Phone.isValidPhone("HP: 1234; Office: 5678")); // two incomplete numbers
        assertFalse(Phone.isValidPhone("91234567\n")); // control character

        // valid phone numbers
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
        assertTrue(Phone.isValidPhone("9123 4567")); // spaces within digits
        assertTrue(Phone.isValidPhone("+65 9123-4567")); // country code and hyphen
        assertTrue(Phone.isValidPhone("1234 5678 (HP) 1111-3333 (Office)")); // labels and two numbers
    }

    @Test
    public void getValidationError() {
        assertEquals(Phone.MESSAGE_BLANK, Phone.getValidationError(" "));
        assertEquals(Phone.MESSAGE_CONTROL_CHARACTERS, Phone.getValidationError("91234567\n"));
        assertEquals(Phone.MESSAGE_NO_DIGITS, Phone.getValidationError("Office"));
        assertEquals(Phone.MESSAGE_INCOMPLETE, Phone.getValidationError("9123"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("99999999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("99999999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("99599999")));
    }
}
