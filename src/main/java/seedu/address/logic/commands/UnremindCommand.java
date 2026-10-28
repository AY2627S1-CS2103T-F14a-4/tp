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

/** Removes a labelled yearly reminder from a person in the displayed list. */
public class UnremindCommand extends Command {
    public static final String COMMAND_WORD = "unremind";
    public static final String MESSAGE_USAGE = "unremind INDEX a/LABEL";
    public static final String MESSAGE_SUCCESS = "Yearly reminder \"%s\" for %s has been removed successfully.";
    public static final String MESSAGE_NOT_FOUND = "%s has no yearly reminder matching label \"%s\".";

    private final Index index;
    private final String label;

    /** Creates a command targeting a reminder label in the displayed person list. */
    public UnremindCommand(Index index, String label) {
        requireAllNonNull(index, label);
        this.index = index;
        this.label = RecurringReminder.parseLabel(label);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        RecurringReminder reminder = original.getRecurringReminders().stream()
                .filter(value -> value.getLabel().equalsIgnoreCase(label)).findFirst()
                .orElseThrow(() -> new CommandException(
                        String.format(MESSAGE_NOT_FOUND, original.getName().fullName, label)));
        model.setPerson(original, original.withoutRecurringReminder(label));
        return new CommandResult(String.format(MESSAGE_SUCCESS, reminder.getLabel(), original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnremindCommand command
                && index.equals(command.index) && label.equals(command.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), label);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("label", label).toString();
    }
}
