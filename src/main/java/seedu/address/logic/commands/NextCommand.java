package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/** Sets or replaces the next action of a person in the displayed list. */
public class NextCommand extends Command {
    public static final String COMMAND_WORD = "next";
    public static final String MESSAGE_USAGE = "next INDEX a/ACTION d/yyyy-MM-dd";
    public static final String MESSAGE_SUCCESS = "Next action for %s has been updated successfully.";

    private final Index index;
    private final NextAction nextAction;

    /** Creates a command with an incomplete follow-up, including when replacing completed work. */
    public NextCommand(Index index, NextAction nextAction) {
        requireAllNonNull(index, nextAction);
        this.index = index;
        this.nextAction = new NextAction(nextAction.getAction(), nextAction.getDueDate(), false);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        model.setPerson(original, original.withNextAction(nextAction));
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NextCommand command
                && index.equals(command.index) && nextAction.equals(command.nextAction);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("nextAction", nextAction)
                .toString();
    }

}
