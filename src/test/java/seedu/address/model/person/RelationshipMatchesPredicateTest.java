package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class RelationshipMatchesPredicateTest {

    @Test
    public void equals() {
        RelationshipMatchesPredicate clientPredicate = new RelationshipMatchesPredicate(Relationship.CLIENT);
        RelationshipMatchesPredicate prospectPredicate = new RelationshipMatchesPredicate(Relationship.PROSPECT);

        assertTrue(clientPredicate.equals(clientPredicate));
        assertTrue(clientPredicate.equals(new RelationshipMatchesPredicate(Relationship.CLIENT)));
        assertFalse(clientPredicate.equals(prospectPredicate));
        assertFalse(clientPredicate.equals(1));
        assertFalse(clientPredicate.equals(null));
    }

    @Test
    public void test_matchingRelationship_returnsTrue() {
        RelationshipMatchesPredicate predicate = new RelationshipMatchesPredicate(Relationship.CLIENT);
        Person client = new PersonBuilder().withRelationship(Relationship.CLIENT).build();

        assertTrue(predicate.test(client));
    }

    @Test
    public void test_differentRelationship_returnsFalse() {
        RelationshipMatchesPredicate predicate = new RelationshipMatchesPredicate(Relationship.CLIENT);
        Person prospect = new PersonBuilder().withRelationship(Relationship.PROSPECT).build();

        assertFalse(predicate.test(prospect));
    }

    @Test
    public void toStringMethod() {
        RelationshipMatchesPredicate predicate = new RelationshipMatchesPredicate(Relationship.CLIENT);
        String expected = RelationshipMatchesPredicate.class.getCanonicalName() + "{relationship=Client}";

        assertEquals(expected, predicate.toString());
    }
}
