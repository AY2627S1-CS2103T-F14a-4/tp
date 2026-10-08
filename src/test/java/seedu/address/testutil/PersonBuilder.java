package seedu.address.testutil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.model.person.AreaOfInterest;
import seedu.address.model.person.Email;
import seedu.address.model.person.FinancialNeed;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Priority;
import seedu.address.model.person.Relationship;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";

    private Name name;
    private Phone phone;
    private Email email;
    private Set<Tag> tags;
    private Relationship relationship = Relationship.PROSPECT;
    private FinancialNeed financialNeed;
    private Priority priority;
    private AreaOfInterest areaOfInterest;
    private List<InteractionNote> interactionNotes = List.of();

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        tags = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        tags = new HashSet<>(personToCopy.getTags());
        relationship = personToCopy.getRelationship();
        financialNeed = personToCopy.getFinancialNeed().orElse(null);
        priority = personToCopy.getPriority().orElse(null);
        areaOfInterest = personToCopy.getAreaOfInterest().orElse(null);
        interactionNotes = personToCopy.getInteractionNotes();
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the {@code FinancialNeed} of the {@code Person} that we are building.
     */
    public PersonBuilder withFinancialNeed(String financialNeed) {
        this.financialNeed = new FinancialNeed(financialNeed);
        return this;
    }

    /**
     * Sets the {@code Priority} of the {@code Person} that we are building.
     */
    public PersonBuilder withPriority(String priority) {
        this.priority = new Priority(priority);
        return this;
    }

    /**
     * Sets the {@code AreaOfInterest} of the {@code Person} that we are building.
     */
    public PersonBuilder withAreaOfInterest(String areaOfInterest) {
        this.areaOfInterest = new AreaOfInterest(areaOfInterest);
        return this;
    }

    /** Builds the person with all supplied information. */
    public Person build() {
        return new Person(name, phone, email, tags, relationship, financialNeed, priority, areaOfInterest,
                interactionNotes);
    }

    /** Sets the interaction history. */
    public PersonBuilder withInteractionNotes(InteractionNote... notes) {
        interactionNotes = List.of(notes);
        return this;
    }

}
