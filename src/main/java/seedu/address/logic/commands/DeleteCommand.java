package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * Deletes a client or prospect identified using its displayed index from the client list.
 */
public class DeleteCommand extends Command implements ConfirmableCommand {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the client/prospect identified by the index number used in the displayed list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "%1$s %2$s has been deleted successfully.";
    public static final String MESSAGE_INVALID_INDEX = "No client exists at index %d.";
    public static final String MESSAGE_CONFIRMATION = "Are you sure you want to delete this?\n"
            + "Contact: %s\n"
            + "Enter Y/Yes to confirm or N/No to cancel.";
    public static final String MESSAGE_CANCELLED = "Person deletion cancelled.";

    private final Index targetIndex;

    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public String getConfirmationMessage(Model model) throws CommandException {
        Person person = getValidatedPerson(model);
        return String.format(MESSAGE_CONFIRMATION, person.getName());
    }

    @Override
    public String getCancellationMessage() {
        return MESSAGE_CANCELLED;
    }

    private Person getValidatedPerson(Model model) throws CommandException {
        requireNonNull(model);
        String invalidIndexMessage = String.format(MESSAGE_INVALID_INDEX, targetIndex.getOneBased());
        return getVisiblePersonAtIndex(model, targetIndex, invalidIndexMessage);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        Person personToDelete = getValidatedPerson(model);
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS,
                personToDelete.getRelationship(), personToDelete.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
