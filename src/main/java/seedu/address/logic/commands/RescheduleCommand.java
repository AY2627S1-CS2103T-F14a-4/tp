package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

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

    private final Index index;
    private final LocalDate dueDate;
    private final String action;

    /** Creates a command; a null action keeps the existing description. */
    public RescheduleCommand(Index index, LocalDate dueDate, String action) {
        requireAllNonNull(index, dueDate);
        this.index = index;
        this.dueDate = dueDate;
        this.action = action;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        if (original.getNextAction().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_NEXT_ACTION, original.getName().fullName));
        }
        NextAction updated = original.getNextAction().orElseThrow().reschedule(dueDate, action);
        model.setPerson(original, original.withNextAction(updated));
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RescheduleCommand command
                && index.equals(command.index) && dueDate.equals(command.dueDate)
                && Objects.equals(action, command.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), dueDate, action);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("dueDate", dueDate).add("action", action).toString();
    }
}
