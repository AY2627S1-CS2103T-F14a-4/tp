package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonMatchesFiltersPredicateTest {
    private final Person clientAlice = new PersonBuilder().withName("Alice Pauline")
            .withRelationship(Relationship.CLIENT).withTags("Friends", "priority")
            .withPreferredContactChannel("WhatsApp").withPreferredContactTiming("Weekdays after 6 pm")
            .withFinancialNeed("Retirement planning").withPriority("Protect family income")
            .withAreaOfInterest("Life insurance")
            .withInteractionNotes(new InteractionNote(LocalDate.of(2026, 10, 6),
                    "Discussed university fund and option A/B"))
            .withNextAction(new NextAction("Call about investment review", LocalDate.of(2026, 12, 15), false))
            .build();

    @Test
    public void test_noFilters_matches() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of(), List.of())
                .test(clientAlice));
    }

    @Test
    public void test_multipleValues_useOrWithinFilter() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of("Bob", "Alice"), Optional.empty(), List.of(), List.of())
                .test(clientAlice));
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("family", "friends"),
                List.of())
                .test(clientAlice));
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of(),
                List.of("missing", "retirement")).test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Bob", "Carol"), Optional.empty(), List.of(), List.of())
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("family", "referral"),
                List.of())
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of(),
                List.of("missing", "unknown")).test(clientAlice));
    }

    @Test
    public void test_partialNameKeyword_matches() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of("lic"), Optional.empty(), List.of(), List.of())
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Ace"), Optional.empty(), List.of(), List.of())
                .test(clientAlice));
    }

    @Test
    public void test_partialTagKeyword_matches() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("friend"), List.of())
                .test(clientAlice));
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("RIEND"), List.of())
                .test(clientAlice));
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("prior"), List.of())
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("fiend"), List.of())
                .test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("friendship"), List.of())
                .test(clientAlice));
    }

    @Test
    public void test_detailKeywordMatchesSupportedFields() {
        assertDetailKeywordMatches("whats");
        assertDetailKeywordMatches("6");
        assertDetailKeywordMatches("retire");
        assertDetailKeywordMatches("TECT");
        assertDetailKeywordMatches("insurance");
        assertDetailKeywordMatches("university");
        assertDetailKeywordMatches("2026-10-06");
        assertDetailKeywordMatches("2026-10");
        assertDetailKeywordMatches("A/B");
        assertDetailKeywordMatches("investment");
        assertDetailKeywordMatches("CALL");
        assertDetailKeywordMatches("2026-12-15");
        assertDetailKeywordMatches("12-15");
    }

    @Test
    public void test_detailKeywordDoesNotMatchExcludedFields() {
        assertDetailKeywordDoesNotMatch("Alice");
        assertDetailKeywordDoesNotMatch("Friends");
        assertDetailKeywordDoesNotMatch("amy@gmail.com");
        assertDetailKeywordDoesNotMatch("85355255");
        assertDetailKeywordDoesNotMatch("2027-12-15");
    }

    @Test
    public void test_differentFilters_useAnd() {
        assertTrue(new PersonMatchesFiltersPredicate(List.of("Alice"), Optional.of(Relationship.CLIENT),
                List.of("FRIENDS"), List.of("retirement")).test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Alice"), Optional.of(Relationship.PROSPECT),
                List.of("friends"), List.of("retirement")).test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Bob"), Optional.of(Relationship.CLIENT),
                List.of("friends"), List.of("retirement")).test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Alice"), Optional.of(Relationship.CLIENT),
                List.of("referral"), List.of("retirement")).test(clientAlice));
        assertFalse(new PersonMatchesFiltersPredicate(List.of("Alice"), Optional.of(Relationship.CLIENT),
                List.of("friends"), List.of("missing")).test(clientAlice));
    }

    private void assertDetailKeywordMatches(String keyword) {
        assertTrue(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of(), List.of(keyword))
                .test(clientAlice));
    }

    private void assertDetailKeywordDoesNotMatch(String keyword) {
        assertFalse(new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of(), List.of(keyword))
                .test(clientAlice));
    }
}
