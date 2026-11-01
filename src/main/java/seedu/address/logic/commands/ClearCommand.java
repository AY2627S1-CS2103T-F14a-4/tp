package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.ClientBook;
import seedu.address.model.Model;
import seedu.address.storage.Storage;

/**
 * Clears the client book.
 */
public class ClearCommand extends Command implements ConfirmableCommand {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Client book has been cleared!";
    public static final String MESSAGE_CONFIRMATION = "Are you sure you want to clear all client records?\n"
            + "Enter Y/Yes to confirm or N/No to cancel.";
    public static final String MESSAGE_CANCELLED = "Clear cancelled.";

    @Override
    public String getConfirmationMessage(Model model) {
        requireNonNull(model);
        return MESSAGE_CONFIRMATION;
    }

    @Override
    public String getCancellationMessage() {
        return MESSAGE_CANCELLED;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) {
        requireNonNull(model);
        model.setClientBook(new ClientBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
