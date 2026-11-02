package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests for an incomplete action or yearly reminder within the configured inclusive windows.
 * Uses a fixed reference date supplied by the caller; it never reads the system clock.
 */
public class NextActionDuePredicate implements Predicate<Person> {
    public static final int REMINDER_LOOKAHEAD_DAYS = 30;

    private final LocalDate today;
    private final int actionDays;

    /**
     * Creates a predicate whose eligibility and earliest dates use the same captured reference date.
     *
     * @param today Non-null date: actions due on or before it and reminders from it through 30 days
     *     later (inclusive) qualify. The date does not advance when the predicate is evaluated again.
     */
    public NextActionDuePredicate(LocalDate today) {
        this(today, 0);
    }

    /**
     * Creates a predicate for actions due through {@code today + actionDays} and reminders from today
     * through {@code max(30, actionDays)} days ahead, inclusive.
     */
    public NextActionDuePredicate(LocalDate today, int actionDays) {
        this.today = requireNonNull(today);
        this.actionDays = actionDays;
    }

    @Override
    public boolean test(Person person) {
        return earliestQualifyingDate(person).isPresent();
    }

    /** Returns the earliest qualifying action or reminder date, or empty when no event qualifies. */
    public Optional<LocalDate> earliestQualifyingDate(Person person) {
        Stream<LocalDate> actionDates = person.getNextAction()
                .filter(action -> !action.isCompleted() && !action.getDueDate().isAfter(today.plusDays(actionDays)))
                .map(NextAction::getDueDate).stream();
        Stream<LocalDate> reminderDates = person.getRecurringReminders().stream()
                .map(reminder -> reminder.nextOccurrence(today))
                .filter(date -> ChronoUnit.DAYS.between(today, date)
                        <= Math.max(REMINDER_LOOKAHEAD_DAYS, actionDays));
        return Stream.concat(actionDates, reminderDates).min(Comparator.naturalOrder());
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

        return today.equals(otherPredicate.today) && actionDays == otherPredicate.actionDays;
    }

    @Override
    public int hashCode() {
        return Objects.hash(today, actionDays);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("today", today).add("actionDays", actionDays).toString();
    }
}
