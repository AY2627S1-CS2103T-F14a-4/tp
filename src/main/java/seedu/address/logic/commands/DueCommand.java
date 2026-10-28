package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.NextActionDuePredicate;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * Lists persons with due actions or upcoming yearly reminders.
 */
public class DueCommand extends Command {
    public static final String COMMAND_WORD = "due";
    public static final String MESSAGE_USAGE = "due";
    public static final String MESSAGE_NO_FOLLOW_UPS = "No follow-ups are due and no yearly reminders are upcoming.";
    public static final String MESSAGE_SUCCESS =
            "%d person(s) with due follow-ups or yearly reminders in the next 30 days.";

    private final Clock clock;

    public DueCommand() {
        this(Clock.systemDefaultZone());
    }

    public DueCommand(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) {
        requireNonNull(model);
        LocalDate today = LocalDate.now(clock);
        model.updateFilteredPersonList(new NextActionDuePredicate(today), dueOrder(today));
        int count = model.getFilteredPersonList().size();
        return new CommandResult(count == 0 ? MESSAGE_NO_FOLLOW_UPS : String.format(MESSAGE_SUCCESS, count));
    }

    /** Orders each person by their earliest qualifying event, using the filter's captured date. */
    private static Comparator<Person> dueOrder(LocalDate today) {
        return Comparator.comparing((Person person) -> earliestDueDate(person, today))
                .thenComparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(person -> person.getName().fullName);
    }

    private static LocalDate earliestDueDate(Person person, LocalDate today) {
        LocalDate actionDate = person.getNextAction()
                .filter(action -> !action.isCompleted() && !action.getDueDate().isAfter(today))
                .map(action -> action.getDueDate()).orElse(LocalDate.MAX);
        LocalDate reminderDate = person.getRecurringReminders().stream()
                .map(reminder -> reminder.nextOccurrence(today))
                .filter(date -> !date.isAfter(today.plusDays(NextActionDuePredicate.REMINDER_LOOKAHEAD_DAYS)))
                .min(Comparator.naturalOrder()).orElse(LocalDate.MAX);
        return actionDate.isBefore(reminderDate) ? actionDate : reminderDate;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DueCommand otherCommand)) {
            return false;
        }

        return clock.equals(otherCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clock", clock).toString();
    }
}
