package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/** Tests whether a person matches all supplied find filters. */
public class PersonMatchesFiltersPredicate implements Predicate<Person> {
    private final List<String> nameKeywords;
    private final Optional<Relationship> relationship;
    private final List<String> tagKeywords;
    private final List<String> detailKeywords;

    /** Creates a predicate from optional name, relationship, tag and remembered-detail filters. */
    public PersonMatchesFiltersPredicate(List<String> nameKeywords, Optional<Relationship> relationship,
            List<String> tagKeywords, List<String> detailKeywords) {
        this.nameKeywords = List.copyOf(requireNonNull(nameKeywords));
        this.relationship = requireNonNull(relationship);
        this.tagKeywords = List.copyOf(requireNonNull(tagKeywords));
        this.detailKeywords = List.copyOf(requireNonNull(detailKeywords));
    }

    @Override
    public boolean test(Person person) {
        boolean matchesName = nameKeywords.isEmpty()
                || new NameContainsKeywordsPredicate(nameKeywords).test(person);
        boolean matchesRelationship = relationship.isEmpty()
                || person.getRelationship().equals(relationship.get());
        boolean matchesTags = tagKeywords.isEmpty()
                || tagKeywords.stream().anyMatch(keyword -> person.getTags().stream()
                        .anyMatch(tag -> StringUtil.containsSubstringIgnoreCase(tag.tagName, keyword)));
        boolean matchesDetails = detailKeywords.isEmpty()
                || detailKeywords.stream().anyMatch(keyword -> containsDetailKeyword(person, keyword));
        return matchesName && matchesRelationship && matchesTags && matchesDetails;
    }

    private static boolean containsDetailKeyword(Person person, String keyword) {
        return person.getPreferredContactChannel()
                .map(channel -> StringUtil.containsSubstringIgnoreCase(channel.value, keyword)).orElse(false)
                || person.getPreferredContactTiming()
                .map(timing -> StringUtil.containsSubstringIgnoreCase(timing.value, keyword)).orElse(false)
                || person.getFinancialNeed()
                .map(need -> StringUtil.containsSubstringIgnoreCase(need.value, keyword)).orElse(false)
                || person.getPriority()
                .map(priority -> StringUtil.containsSubstringIgnoreCase(priority.value, keyword)).orElse(false)
                || person.getAreaOfInterest()
                .map(area -> StringUtil.containsSubstringIgnoreCase(area.value, keyword)).orElse(false)
                || person.getNextAction()
                .map(action -> StringUtil.containsSubstringIgnoreCase(action.getAction(), keyword)
                        || StringUtil.containsSubstringIgnoreCase(action.getDueDate().toString(), keyword))
                .orElse(false)
                || person.getInteractionNotes().stream()
                .anyMatch(note -> StringUtil.containsSubstringIgnoreCase(note.getDate().toString(), keyword)
                        || StringUtil.containsSubstringIgnoreCase(note.getText(), keyword));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PersonMatchesFiltersPredicate otherPredicate
                && nameKeywords.equals(otherPredicate.nameKeywords)
                && relationship.equals(otherPredicate.relationship)
                && tagKeywords.equals(otherPredicate.tagKeywords)
                && detailKeywords.equals(otherPredicate.detailKeywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameKeywords, relationship, tagKeywords, detailKeywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("nameKeywords", nameKeywords)
                .add("relationship", relationship)
                .add("tagKeywords", tagKeywords)
                .add("detailKeywords", detailKeywords)
                .toString();
    }
}
