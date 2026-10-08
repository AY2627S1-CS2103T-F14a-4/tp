package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Relationship} matches the specified relationship.
 */
public class RelationshipMatchesPredicate implements Predicate<Person> {
    private final Relationship relationship;

    /**
     * Creates a predicate that matches persons with the specified {@code relationship}.
     */
    public RelationshipMatchesPredicate(Relationship relationship) {
        this.relationship = requireNonNull(relationship);
    }

    @Override
    public boolean test(Person person) {
        return person.getRelationship().equals(relationship);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof RelationshipMatchesPredicate otherRelationshipMatchesPredicate)) {
            return false;
        }

        return relationship.equals(otherRelationshipMatchesPredicate.relationship);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("relationship", relationship).toString();
    }
}
