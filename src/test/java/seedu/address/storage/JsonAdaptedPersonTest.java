package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.AreaOfInterest;
import seedu.address.model.person.Email;
import seedu.address.model.person.FinancialNeed;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Priority;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_INVALID_CHARACTERS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_INCOMPLETE;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_AT_SIGN;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void financialInformation_jsonRoundTrip_preservesValues() throws Exception {
        Person original = new PersonBuilder(BENSON).withFinancialNeed("Retirement").withPriority("High")
                .withAreaOfInterest("Insurance").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertEquals(original, restored);
    }

    @Test
    public void financialInformation_nullJsonFields_loadAsAbsent() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON));
        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertEquals(BENSON, restored);
        assertTrue(restored.getFinancialNeed().isEmpty());
        assertTrue(restored.getPriority().isEmpty());
        assertTrue(restored.getAreaOfInterest().isEmpty());
    }

    @Test
    public void financialInformation_missingJsonFields_loadAsAbsent() throws Exception {
        String json = "{\"name\":\"Benson Meier\",\"phone\":\"98765432\",\"email\":\"benson@example.com\"}";
        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertTrue(restored.getFinancialNeed().isEmpty());
        assertTrue(restored.getPriority().isEmpty());
        assertTrue(restored.getAreaOfInterest().isEmpty());
    }

    @Test
    public void financialInformation_independentlyOptional_roundTrips() throws Exception {
        Person[] persons = {
            new PersonBuilder(BENSON).withFinancialNeed("Retirement").build(),
            new PersonBuilder(BENSON).withPriority("High").build(),
            new PersonBuilder(BENSON).withAreaOfInterest("Insurance").build()
        };
        for (Person person : persons) {
            String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
            assertEquals(person, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
        }
    }

    @Test
    public void toModelType_invalidFinancialNeed_throwsExactValidationMessage() {
        String[] invalidValues = {"", "   ", "A".repeat(201), "Needs/Wants", "\tRetirement", "Retirement\n",
            "Retirement\r"};
        String[] expectedMessages = {FinancialNeed.MESSAGE_BLANK, FinancialNeed.MESSAGE_BLANK,
            FinancialNeed.MESSAGE_LENGTH,
            FinancialNeed.MESSAGE_INVALID_CHARACTERS, FinancialNeed.MESSAGE_INVALID_CHARACTERS,
            FinancialNeed.MESSAGE_INVALID_CHARACTERS, FinancialNeed.MESSAGE_INVALID_CHARACTERS};
        for (int i = 0; i < invalidValues.length; i++) {
            String invalid = invalidValues[i];
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TAGS,
                    null, invalid, null, null);
            assertThrows(IllegalValueException.class, expectedMessages[i], person::toModelType);
        }
    }

    @Test
    public void toModelType_invalidPriority_throwsExactValidationMessage() {
        String[] invalidValues = {"", "   ", "A".repeat(201), "Needs/Wants", "\tRetirement", "Retirement\n",
            "Retirement\r"};
        String[] expectedMessages = {Priority.MESSAGE_BLANK, Priority.MESSAGE_BLANK, Priority.MESSAGE_LENGTH,
            Priority.MESSAGE_INVALID_CHARACTERS, Priority.MESSAGE_INVALID_CHARACTERS,
            Priority.MESSAGE_INVALID_CHARACTERS, Priority.MESSAGE_INVALID_CHARACTERS};
        for (int i = 0; i < invalidValues.length; i++) {
            String invalid = invalidValues[i];
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TAGS,
                    null, null, invalid, null);
            assertThrows(IllegalValueException.class, expectedMessages[i], person::toModelType);
        }
    }

    @Test
    public void toModelType_invalidAreaOfInterest_throwsExactValidationMessage() {
        String[] invalidValues = {"", "   ", "A".repeat(201), "Needs/Wants", "\tRetirement", "Retirement\n",
            "Retirement\r"};
        String[] expectedMessages = {AreaOfInterest.MESSAGE_BLANK, AreaOfInterest.MESSAGE_BLANK,
            AreaOfInterest.MESSAGE_LENGTH,
            AreaOfInterest.MESSAGE_INVALID_CHARACTERS, AreaOfInterest.MESSAGE_INVALID_CHARACTERS,
            AreaOfInterest.MESSAGE_INVALID_CHARACTERS, AreaOfInterest.MESSAGE_INVALID_CHARACTERS};
        for (int i = 0; i < invalidValues.length; i++) {
            String invalid = invalidValues[i];
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TAGS,
                    null, null, null, invalid);
            assertThrows(IllegalValueException.class, expectedMessages[i], person::toModelType);
        }
    }

}
