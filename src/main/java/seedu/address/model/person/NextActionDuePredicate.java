package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a person's incomplete next action is due on or before the given date.
 */
public class NextActionDuePredicate implements Predicate<Person> {
    private final LocalDate today;

    public NextActionDuePredicate(LocalDate today) {
        this.today = requireNonNull(today);
    }

    @Override
    public boolean test(Person person) {
        return person.getNextAction()
                .filter(action -> !action.isCompleted() && !action.getDueDate().isAfter(today))
                .isPresent();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NextActionDuePredicate otherPredicate)) {
            return false;
        }

        return today.equals(otherPredicate.today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("today", today).toString();
    }
}
