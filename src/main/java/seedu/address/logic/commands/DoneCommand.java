package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;

/** Completes the next action of a person in the displayed list. */
public class DoneCommand extends Command {
    public static final String COMMAND_WORD = "done";
    public static final String MESSAGE_USAGE = "done INDEX";
    public static final String MESSAGE_SUCCESS = "Next action for %s has been marked as completed.";
    public static final String MESSAGE_NO_NEXT_ACTION = "%s has no next action to complete.";
    public static final String MESSAGE_ALREADY_COMPLETED = "Next action for %s is already completed.";

    private final Index index;

    /** Creates a command targeting the displayed index. */
    public DoneCommand(Index index) {
        requireAllNonNull(index);
        this.index = index;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireAllNonNull(model);
        Person original = getVisiblePersonAtIndex(model, index,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        String name = original.getName().fullName;
        if (original.getNextAction().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_NEXT_ACTION, name));
        }
        NextAction nextAction = original.getNextAction().orElseThrow();
        if (nextAction.isCompleted()) {
            return new CommandResult(String.format(MESSAGE_ALREADY_COMPLETED, name));
        }
        model.setPerson(original, original.withNextAction(nextAction.markCompleted()));
        return new CommandResult(String.format(MESSAGE_SUCCESS, name));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DoneCommand command && index.equals(command.index);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .toString();
    }
}
