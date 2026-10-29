package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** A command that validates and requests confirmation before execution. */
public interface ConfirmableCommand {
    String MESSAGE_INVALID_CONFIRMATION = "Please enter Y/Yes or N/No.";

    /** Validates the command and describes its effects without changing the model. */
    String getConfirmationMessage(Model model) throws CommandException;

    /** Returns the feedback when the user cancels this command. */
    String getCancellationMessage();
}
