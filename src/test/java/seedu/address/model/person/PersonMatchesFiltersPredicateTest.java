package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonMatchesFiltersPredicateTest {
    private final Person clientAlice = new PersonBuilder().withName("Alice Pauline")
            .withRelationship(Relationship.CLIENT).withTags("Friends", "priority").build();

    @Test
    public void test_noFilters_matches() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of()).test(clientAlice));
    }

    @Test
    public void test_multipleValues_useOrWithinFilter() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of("Bob", "Alice"), Optional.empty(), List.of())
                .test(clientAlice));
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("family", "friends"))
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Bob", "Carol"), Optional.empty(), List.of())
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("family", "referral"))
                .test(clientAlice));
    }

    @Test
    public void test_partialNameKeyword_matches() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of("lic"), Optional.empty(), List.of())
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Ace"), Optional.empty(), List.of())
                .test(clientAlice));
    }

    @Test
    public void test_partialTagKeyword_matches() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("friend"))
                .test(clientAlice));
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("RIEND"))
                .test(clientAlice));
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("prior"))
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("fiend"))
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("friendship"))
                .test(clientAlice));
    }

    @Test
    public void test_differentFilters_useAnd() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of("Alice"), Optional.of(Relationship.CLIENT),
                List.of("FRIENDS")).test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Alice"), Optional.of(Relationship.PROSPECT),
                List.of("friends")).test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Bob"), Optional.of(Relationship.CLIENT),
                List.of("friends")).test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Alice"), Optional.of(Relationship.CLIENT),
                List.of("referral")).test(clientAlice));
    }
}
