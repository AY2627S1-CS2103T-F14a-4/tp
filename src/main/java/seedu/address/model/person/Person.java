package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the client book.
 * Guarantees: required details are not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;
    private final Relationship relationship;

    // Data fields
    private final FinancialNeed financialNeed;
    private final Priority priority;
    private final AreaOfInterest areaOfInterest;
    private final List<InteractionNote> interactionNotes;
    private final NextAction nextAction;
    private final PreferredContactChannel preferredContactChannel;
    private final PreferredContactTiming preferredContactTiming;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Set<Tag> tags) {
        this(name, phone, email, tags, Relationship.PROSPECT);
    }

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Set<Tag> tags, Relationship relationship) {
        this(name, phone, email, tags, relationship, null, null, null);
    }

    /**
     * Creates a person with optional financial information.
     * Required fields must not be null; a null financial need, priority, or area of interest represents absence.
     */
    public Person(Name name, Phone phone, Email email, Set<Tag> tags, Relationship relationship,
            FinancialNeed financialNeed, Priority priority, AreaOfInterest areaOfInterest) {
        this(name, phone, email, tags, relationship, financialNeed, priority, areaOfInterest, List.of());
    }

    /** Creates a person with a defensive copy of their interaction history. */
    public Person(Name name, Phone phone, Email email, Set<Tag> tags, Relationship relationship,
            FinancialNeed financialNeed, Priority priority, AreaOfInterest areaOfInterest,
            List<InteractionNote> interactionNotes) {
        this(name, phone, email, tags, relationship, financialNeed, priority, areaOfInterest, interactionNotes, null);
    }

    /** Creates a person with optional follow-up information; null means no follow-up. */
    public Person(Name name, Phone phone, Email email, Set<Tag> tags, Relationship relationship,
            FinancialNeed financialNeed, Priority priority, AreaOfInterest areaOfInterest,
            List<InteractionNote> interactionNotes, NextAction nextAction) {
        this(name, phone, email, tags, relationship, financialNeed, priority, areaOfInterest,
                interactionNotes, nextAction, null, null);
    }

    /** Creates a person with optional contact preferences; null represents absence. */
    public Person(Name name, Phone phone, Email email, Set<Tag> tags, Relationship relationship,
            FinancialNeed financialNeed, Priority priority, AreaOfInterest areaOfInterest,
            List<InteractionNote> interactionNotes, NextAction nextAction,
            PreferredContactChannel preferredContactChannel, PreferredContactTiming preferredContactTiming) {
        requireAllNonNull(name, phone, email, tags, relationship, interactionNotes);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.tags.addAll(tags);
        this.relationship = relationship;
        this.financialNeed = financialNeed;
        this.priority = priority;
        this.areaOfInterest = areaOfInterest;
        this.interactionNotes = List.copyOf(interactionNotes).stream()
                .sorted(Comparator.comparing(InteractionNote::getDate)).toList();
        this.nextAction = nextAction;
        this.preferredContactChannel = preferredContactChannel;
        this.preferredContactTiming = preferredContactTiming;
    }

    /**
     * Creates a contact without tags, as required by the add command.
     */
    public Person(Name name, Phone phone, Email email, Relationship relationship) {
        this(name, phone, email, Collections.emptySet(), relationship);
    }

    /** Returns immutable notes in ascending date order, preserving input order for notes with the same date. */
    public List<InteractionNote> getInteractionNotes() {
        return interactionNotes;
    }

    public Optional<NextAction> getNextAction() {
        return Optional.ofNullable(nextAction);
    }

    /** Returns a copy with the supplied follow-up and all other fields preserved. */
    public Person withNextAction(NextAction nextAction) {
        return new Person(name, phone, email, tags, relationship, financialNeed, priority, areaOfInterest,
                interactionNotes, nextAction, preferredContactChannel, preferredContactTiming);
    }

    public Optional<PreferredContactChannel> getPreferredContactChannel() {
        return Optional.ofNullable(preferredContactChannel);
    }

    public Optional<PreferredContactTiming> getPreferredContactTiming() {
        return Optional.ofNullable(preferredContactTiming);
    }

    /** Returns a copy replacing supplied preferences; null means keep the existing value. */
    public Person withContactPreferences(PreferredContactChannel channel, PreferredContactTiming timing) {
        return new Person(name, phone, email, tags, relationship, financialNeed, priority, areaOfInterest,
                interactionNotes, nextAction, channel == null ? preferredContactChannel : channel,
                timing == null ? preferredContactTiming : timing);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public Optional<FinancialNeed> getFinancialNeed() {
        return Optional.ofNullable(financialNeed);
    }

    public Optional<Priority> getPriority() {
        return Optional.ofNullable(priority);
    }

    public Optional<AreaOfInterest> getAreaOfInterest() {
        return Optional.ofNullable(areaOfInterest);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /** Returns a copy with the supplied tags and all other fields preserved. */
    public Person withTags(Set<Tag> tags) {
        return new Person(name, phone, email, tags, relationship, financialNeed, priority, areaOfInterest,
                interactionNotes, nextAction, preferredContactChannel, preferredContactTiming);
    }

    /**
     * Returns true if both persons have the same email address or phone number.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && (otherPerson.getEmail().equals(getEmail())
                || otherPerson.getPhone().hasSamePhoneNumber(getPhone()));
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && tags.equals(otherPerson.tags)
                && relationship.equals(otherPerson.relationship)
                && Objects.equals(financialNeed, otherPerson.financialNeed)
                && Objects.equals(priority, otherPerson.priority)
                && Objects.equals(areaOfInterest, otherPerson.areaOfInterest)
                && interactionNotes.equals(otherPerson.interactionNotes)
                && Objects.equals(nextAction, otherPerson.nextAction)
                && Objects.equals(preferredContactChannel, otherPerson.preferredContactChannel)
                && Objects.equals(preferredContactTiming, otherPerson.preferredContactTiming);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, tags, relationship, financialNeed, priority, areaOfInterest,
                interactionNotes, nextAction, preferredContactChannel, preferredContactTiming);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("relationship", relationship)
                .add("tags", tags)
                .add("financialNeed", financialNeed)
                .add("priority", priority)
                .add("areaOfInterest", areaOfInterest)
                .add("interactionNotes", interactionNotes)
                .add("nextAction", nextAction)
                .add("preferredContactChannel", preferredContactChannel)
                .add("preferredContactTiming", preferredContactTiming)
                .toString();
    }

}
