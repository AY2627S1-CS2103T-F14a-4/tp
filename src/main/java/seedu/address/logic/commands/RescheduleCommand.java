package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/** Changes the date and optionally description of an existing follow-up. */
public class RescheduleCommand extends Command {
    public static final String COMMAND_WORD = "reschedule";
    public static final String MESSAGE_USAGE = "reschedule INDEX d/yyyy-MM-dd [a/ACTION]";
    public static final String MESSAGE_SUCCESS = "Follow-up for %s has been rescheduled successfully.";
    public static final String MESSAGE_NO_NEXT_ACTION = "%s has no follow-up to reschedule. Use next to create one.";
    public static final String MESSAGE_PAST_DATE = "Follow-up date cannot be before today.";

    private final Index index;
    private final LocalDate dueDate;
    private final String action;
    private final Clock clock;

    /** Creates a command; a null action keeps the existing description. */
    public RescheduleCommand(Index index, LocalDate dueDate, String action) {
        this(index, dueDate, action, Clock.systemDefaultZone());
    }

    /** Creates a command with a clock for determining today's local date at execution. */
    public RescheduleCommand(Index index, LocalDate dueDate, String action, Clock clock) {
        requireAllNonNull(index, dueDate, clock);
        this.index = index;
        this.dueDate = dueDate;
        this.action = action;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        Person original = getVisiblePersonAtIndex(model, index,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        if (original.getNextAction().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_NEXT_ACTION, original.getName().fullName));
        }
        if (dueDate.isBefore(LocalDate.now(clock))) {
            throw new CommandException(MESSAGE_PAST_DATE);
        }
        NextAction updated = original.getNextAction().orElseThrow().reschedule(dueDate, action);
        model.setPerson(original, original.withNextAction(updated));
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RescheduleCommand command
                && index.equals(command.index) && dueDate.equals(command.dueDate)
                && Objects.equals(action, command.action) && clock.equals(command.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), dueDate, action, clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("dueDate", dueDate)
                .add("action", action).add("clock", clock).toString();
    }
}
