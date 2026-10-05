package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.AreaOfInterest;
import seedu.address.model.person.Email;
import seedu.address.model.person.FinancialNeed;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Priority;
import seedu.address.model.person.Relationship;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String relationship;
    private final String financialNeed;
    private final String priority;
    private final String areaOfInterest;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    JsonAdaptedPerson(String name, String phone, String email, List<JsonAdaptedTag> tags) {
        this(name, phone, email, tags, null);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    public JsonAdaptedPerson(String name, String phone, String email, List<JsonAdaptedTag> tags,
            String relationship) {
        this(name, phone, email, tags, relationship, null, null, null);
    }

    /**
     * Constructs an adapted person with optional financial information; null represents absence.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("relationship") String relationship,
            @JsonProperty("financialNeed") String financialNeed, @JsonProperty("priority") String priority,
            @JsonProperty("areaOfInterest") String areaOfInterest) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.relationship = relationship;
        this.financialNeed = financialNeed;
        this.priority = priority;
        this.areaOfInterest = areaOfInterest;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        relationship = source.getRelationship().toString();
        financialNeed = source.getFinancialNeed().map(value -> value.value).orElse(null);
        priority = source.getPriority().map(value -> value.value).orElse(null);
        areaOfInterest = source.getAreaOfInterest().map(value -> value.value).orElse(null);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        String nameValidationError = Name.getValidationError(name);
        if (nameValidationError != null) {
            throw new IllegalValueException(nameValidationError);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        String phoneValidationError = Phone.getValidationError(phone);
        if (phoneValidationError != null) {
            throw new IllegalValueException(phoneValidationError);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        String emailValidationError = Email.getValidationError(email);
        if (emailValidationError != null) {
            throw new IllegalValueException(emailValidationError);
        }
        final Email modelEmail = new Email(email);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        Relationship modelRelationship = relationship == null
                ? Relationship.PROSPECT : parseRelationship(relationship);
        FinancialNeed modelFinancialNeed = null;
        if (financialNeed != null) {
            String validationError = FinancialNeed.getValidationError(financialNeed);
            if (validationError != null) {
                throw new IllegalValueException(validationError);
            }
            modelFinancialNeed = new FinancialNeed(financialNeed);
        }

        Priority modelPriority = null;
        if (priority != null) {
            String validationError = Priority.getValidationError(priority);
            if (validationError != null) {
                throw new IllegalValueException(validationError);
            }
            modelPriority = new Priority(priority);
        }

        AreaOfInterest modelAreaOfInterest = null;
        if (areaOfInterest != null) {
            String validationError = AreaOfInterest.getValidationError(areaOfInterest);
            if (validationError != null) {
                throw new IllegalValueException(validationError);
            }
            modelAreaOfInterest = new AreaOfInterest(areaOfInterest);
        }

        return new Person(modelName, modelPhone, modelEmail, modelTags, modelRelationship,
                modelFinancialNeed, modelPriority, modelAreaOfInterest);
    }

    private static Relationship parseRelationship(String relationship) throws IllegalValueException {
        try {
            return Relationship.fromString(relationship);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(Relationship.MESSAGE_CONSTRAINTS);
        }
    }

}
