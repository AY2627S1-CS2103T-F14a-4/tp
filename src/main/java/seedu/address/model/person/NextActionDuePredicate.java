package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests for an incomplete due action or a yearly reminder in the next 30 days, inclusive.
 */
public class NextActionDuePredicate implements Predicate<Person> {
    public static final int REMINDER_LOOKAHEAD_DAYS = 30;

    private final LocalDate today;

    public NextActionDuePredicate(LocalDate today) {
        this.today = requireNonNull(today);
    }

    @Override
    public boolean test(Person person) {
        return person.getNextAction()
                .filter(action -> !action.isCompleted() && !action.getDueDate().isAfter(today))
                .isPresent() || person.getRecurringReminders().stream()
                .anyMatch(reminder -> ChronoUnit.DAYS.between(today, reminder.nextOccurrence(today))
                        <= REMINDER_LOOKAHEAD_DAYS);
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
