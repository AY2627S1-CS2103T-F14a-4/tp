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

    /** Creates a command that determines today's date in the system default time zone at execution. */
    public DueCommand() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a command that captures today's date once per execution, using the clock's time zone.
     * The filter and sort order retain that date until another command replaces them.
     *
     * @param clock Non-null clock used to determine the date at execution, not construction.
     */
    public DueCommand(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) {
        requireNonNull(model);
        LocalDate today = LocalDate.now(clock);
        NextActionDuePredicate predicate = new NextActionDuePredicate(today);
        model.updateFilteredPersonList(predicate, dueOrder(predicate));
        int count = model.getFilteredPersonList().size();
        return new CommandResult(count == 0 ? MESSAGE_NO_FOLLOW_UPS : String.format(MESSAGE_SUCCESS, count));
    }

    /** Orders each person by their earliest qualifying event, using the filter's captured date. */
    private static Comparator<Person> dueOrder(NextActionDuePredicate predicate) {
        return Comparator.comparing((Person person) -> predicate.earliestQualifyingDate(person).orElse(LocalDate.MAX))
                .thenComparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(person -> person.getName().fullName);
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
    public int hashCode() {
        return clock.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clock", clock).toString();
    }
}
