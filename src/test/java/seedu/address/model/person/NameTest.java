package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only invalid characters
        assertFalse(Name.isValidName("peter*")); // contains an invalid character
        assertFalse(Name.isValidName("Peter123")); // contains digits
        assertFalse(Name.isValidName("John/Smith")); // arbitrary slash usage
        assertFalse(Name.isValidName("Ravi s/o")); // incomplete relationship marker
        assertFalse(Name.isValidName("A".repeat(101))); // more than 100 characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // letters and spaces
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("Nur-Aisyah")); // hyphenated name
        assertTrue(Name.isValidName("O'Connor")); // apostrophe in name
        assertTrue(Name.isValidName("Ravi s/o Kumar")); // son of marker
        assertTrue(Name.isValidName("Priya D/O Nair")); // case-insensitive daughter of marker
        assertTrue(Name.isValidName("Asha w/o Raj")); // wife of marker
        assertTrue(Name.isValidName("  Ravi s/o Kumar  ")); // surrounding whitespace
    }

    @Test
    public void getValidationError() {
        assertEquals(Name.MESSAGE_CONSTRAINTS, Name.getValidationError("Ravi/ Kumar"));
        assertEquals(Name.MESSAGE_LENGTH, Name.getValidationError("A".repeat(101)));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
