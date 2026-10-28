package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecurringReminder;
import seedu.address.storage.Storage;

/** Sets a labelled yearly reminder for a person in the displayed list. */
public class RemindCommand extends Command {
    public static final String COMMAND_WORD = "remind";
    public static final String MESSAGE_USAGE = "remind INDEX a/LABEL d/MM-dd";
    public static final String MESSAGE_SUCCESS = "Yearly reminder for %s has been updated successfully.";

    private final Index index;
    private final RecurringReminder reminder;

    /** Creates a command targeting a person in the displayed list. */
    public RemindCommand(Index index, RecurringReminder reminder) {
        requireAllNonNull(index, reminder);
        this.index = index;
        this.reminder = reminder;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        model.setPerson(original, original.withRecurringReminder(reminder));
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RemindCommand command
                && index.equals(command.index) && reminder.equals(command.reminder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), reminder);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("reminder", reminder).toString();
    }
}
