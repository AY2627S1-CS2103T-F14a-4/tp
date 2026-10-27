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

    /** Creates a predicate from optional name, relationship and tag filters. */
    public PersonMatchesFiltersPredicate(List<String> nameKeywords, Optional<Relationship> relationship,
            List<String> tagKeywords) {
        this.nameKeywords = List.copyOf(requireNonNull(nameKeywords));
        this.relationship = requireNonNull(relationship);
        this.tagKeywords = List.copyOf(requireNonNull(tagKeywords));
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
        return matchesName && matchesRelationship && matchesTags;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PersonMatchesFiltersPredicate otherPredicate
                && nameKeywords.equals(otherPredicate.nameKeywords)
                && relationship.equals(otherPredicate.relationship)
                && tagKeywords.equals(otherPredicate.tagKeywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameKeywords, relationship, tagKeywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("nameKeywords", nameKeywords)
                .add("relationship", relationship)
                .add("tagKeywords", tagKeywords)
                .toString();
    }
}
