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

/** Sets or replaces the next action of a person in the displayed list. */
public class NextCommand extends Command {
    public static final String COMMAND_WORD = "next";
    public static final String MESSAGE_USAGE = "next INDEX a/ACTION d/yyyy-MM-dd";
    public static final String MESSAGE_SUCCESS = "Next action for %s has been updated successfully.";
    public static final String MESSAGE_PAST_DATE = "Follow-up date cannot be before today.";

    private final Index index;
    private final NextAction nextAction;
    private final Clock clock;

    /** Creates a command with an incomplete follow-up, including when replacing completed work. */
    public NextCommand(Index index, NextAction nextAction) {
        this(index, nextAction, Clock.systemDefaultZone());
    }

    /** Creates a command with a clock for validating the due date at execution. */
    public NextCommand(Index index, NextAction nextAction, Clock clock) {
        requireAllNonNull(index, nextAction, clock);
        this.index = index;
        this.nextAction = new NextAction(nextAction.getAction(), nextAction.getDueDate(), false);
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        Person original = getVisiblePersonAtIndex(model, index,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        if (nextAction.getDueDate().isBefore(LocalDate.now(clock))) {
            throw new CommandException(MESSAGE_PAST_DATE);
        }
        model.setPerson(original, original.withNextAction(nextAction));
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NextCommand command
                && index.equals(command.index) && nextAction.equals(command.nextAction) && clock.equals(command.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), nextAction, clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("nextAction", nextAction)
                .add("clock", clock)
                .toString();
    }

}
