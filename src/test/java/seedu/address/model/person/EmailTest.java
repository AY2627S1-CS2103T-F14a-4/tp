package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertFalse(Email.isValidEmail("")); // empty string
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // invalid @ structure
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name
        assertFalse(Email.isValidEmail("peterjack@@example.com")); // double '@' symbol

        // invalid local part
        assertFalse(Email.isValidEmail("peter jack@example.com")); // spaces in local part
        assertFalse(Email.isValidEmail(".peterjack@example.com")); // local part starts with a period
        assertFalse(Email.isValidEmail("peterjack.@example.com")); // local part ends with a period
        assertFalse(Email.isValidEmail("peter..jack@example.com")); // consecutive periods
        assertFalse(Email.isValidEmail("peterjack,one@example.com")); // unsupported local-part character

        // invalid domain
        assertFalse(Email.isValidEmail("peterjack@exam_ple.com")); // underscore in domain name
        assertFalse(Email.isValidEmail("peterjack@exam ple.com")); // spaces in domain name
        assertFalse(Email.isValidEmail("peterjack@.example.com")); // domain name starts with a period
        assertFalse(Email.isValidEmail("peterjack@example.com.")); // domain name ends with a period
        assertFalse(Email.isValidEmail("peterjack@example..com")); // consecutive periods
        assertFalse(Email.isValidEmail("peterjack@-example.com")); // domain name starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.com-")); // domain name ends with a hyphen
        assertFalse(Email.isValidEmail("a@")); // fewer than 3 characters
        assertFalse(Email.isValidEmail("a".repeat(255) + "@b")); // more than 254 characters

        // valid email
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com")); // underscore in local part
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com")); // period in local part
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com")); // '+' symbol in local part
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com")); // hyphen in local part
        assertTrue(Email.isValidEmail("Peter!Jack@example.com")); // common local-part character
        assertTrue(Email.isValidEmail("a@bc")); // minimal
        assertTrue(Email.isValidEmail("test@localhost")); // alphabets only
        assertTrue(Email.isValidEmail("123@145")); // numeric local part and domain name
        assertTrue(Email.isValidEmail("a1+be.d@example1.com")); // mixture of alphanumeric and special characters
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com")); // long domain name
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com")); // long local part
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu")); // more than one period in domain
    }

    @Test
    public void getValidationError() {
        assertEquals(Email.MESSAGE_BLANK, Email.getValidationError(" "));
        assertEquals(Email.MESSAGE_CONTROL_CHARACTERS, Email.getValidationError("a@b\n"));
        assertEquals(Email.MESSAGE_WHITESPACE, Email.getValidationError("a b@example.com"));
        assertEquals(Email.MESSAGE_AT_SIGN, Email.getValidationError("abc.example.com"));
        assertEquals(Email.MESSAGE_LOCAL_PART_DOTS, Email.getValidationError("a..b@example.com"));
        assertEquals(Email.MESSAGE_LOCAL_PART_CHARACTERS, Email.getValidationError("a,b@example.com"));
        assertEquals(Email.MESSAGE_DOMAIN_EMPTY_LABEL, Email.getValidationError("a@example..com"));
        assertEquals(Email.MESSAGE_DOMAIN_LABEL, Email.getValidationError("a@-example.com"));
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email")));
    }
}
