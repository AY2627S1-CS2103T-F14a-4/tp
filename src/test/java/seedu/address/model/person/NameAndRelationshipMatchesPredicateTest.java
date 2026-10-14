package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameAndRelationshipMatchesPredicateTest {

    @Test
    public void equals() {
        NameAndRelationshipMatchesPredicate predicate =
                new NameAndRelationshipMatchesPredicate(List.of("Alice"), Relationship.CLIENT);

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(
                new NameAndRelationshipMatchesPredicate(List.of("Alice"), Relationship.CLIENT)));
        assertFalse(predicate.equals(
                new NameAndRelationshipMatchesPredicate(List.of("Bob"), Relationship.CLIENT)));
        assertFalse(predicate.equals(
                new NameAndRelationshipMatchesPredicate(List.of("Alice"), Relationship.PROSPECT)));
        assertFalse(predicate.equals(1));
        assertFalse(predicate.equals(null));
    }

    @Test
    public void test_nameAndRelationshipMatch_returnsTrue() {
        NameAndRelationshipMatchesPredicate predicate =
                new NameAndRelationshipMatchesPredicate(List.of("Alice", "Bob"), Relationship.CLIENT);
        Person clientAlice = new PersonBuilder().withName("Alice Pauline")
                .withRelationship(Relationship.CLIENT).build();

        assertTrue(predicate.test(clientAlice));
    }

    @Test
    public void test_onlyNameMatches_returnsFalse() {
        NameAndRelationshipMatchesPredicate predicate =
                new NameAndRelationshipMatchesPredicate(List.of("Alice"), Relationship.CLIENT);
        Person prospectAlice = new PersonBuilder().withName("Alice Pauline")
                .withRelationship(Relationship.PROSPECT).build();

        assertFalse(predicate.test(prospectAlice));
    }

    @Test
    public void test_onlyRelationshipMatches_returnsFalse() {
        NameAndRelationshipMatchesPredicate predicate =
                new NameAndRelationshipMatchesPredicate(List.of("Alice"), Relationship.CLIENT);
        Person clientBob = new PersonBuilder().withName("Bob Choo")
                .withRelationship(Relationship.CLIENT).build();

        assertFalse(predicate.test(clientBob));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("Alice", "Bob");
        NameAndRelationshipMatchesPredicate predicate =
                new NameAndRelationshipMatchesPredicate(keywords, Relationship.CLIENT);
        String expected = NameAndRelationshipMatchesPredicate.class.getCanonicalName()
                + "{namePredicate=" + new NameContainsKeywordsPredicate(keywords)
                + ", relationshipPredicate=" + new RelationshipMatchesPredicate(Relationship.CLIENT) + "}";

        assertEquals(expected, predicate.toString());
    }
}
