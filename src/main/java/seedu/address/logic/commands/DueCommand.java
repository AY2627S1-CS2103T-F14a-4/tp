package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.NextActionDuePredicate;

/**
 * Lists persons whose incomplete next action is due today or earlier.
 */
public class DueCommand extends Command {
    public static final String COMMAND_WORD = "due";
    public static final String MESSAGE_USAGE = "due";
    public static final String MESSAGE_NO_FOLLOW_UPS = "No follow-ups are due.";
    public static final String MESSAGE_SUCCESS = "%d follow-up(s) due today or earlier.";

    private final Clock clock;

    public DueCommand() {
        this(Clock.systemDefaultZone());
    }

    public DueCommand(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now(clock);
        model.updateFilteredPersonList(new NextActionDuePredicate(today));
        int count = model.getFilteredPersonList().size();
        return new CommandResult(count == 0 ? MESSAGE_NO_FOLLOW_UPS : String.format(MESSAGE_SUCCESS, count));
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
