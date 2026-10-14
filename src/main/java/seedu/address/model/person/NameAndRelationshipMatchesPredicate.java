package seedu.address.model.person;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s name matches any of the specified keywords and their relationship matches the
 * specified relationship.
 */
public class NameAndRelationshipMatchesPredicate implements Predicate<Person> {
    private final NameContainsKeywordsPredicate namePredicate;
    private final RelationshipMatchesPredicate relationshipPredicate;

    /**
     * Creates a predicate that matches persons with a name containing any of the specified {@code keywords} and the
     * specified {@code relationship}.
     */
    public NameAndRelationshipMatchesPredicate(List<String> keywords, Relationship relationship) {
        namePredicate = new NameContainsKeywordsPredicate(keywords);
        relationshipPredicate = new RelationshipMatchesPredicate(relationship);
    }

    @Override
    public boolean test(Person person) {
        return namePredicate.test(person) && relationshipPredicate.test(person);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameAndRelationshipMatchesPredicate otherPredicate)) {
            return false;
        }

        return namePredicate.equals(otherPredicate.namePredicate)
                && relationshipPredicate.equals(otherPredicate.relationshipPredicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("namePredicate", namePredicate)
                .add("relationshipPredicate", relationshipPredicate)
                .toString();
    }
}
