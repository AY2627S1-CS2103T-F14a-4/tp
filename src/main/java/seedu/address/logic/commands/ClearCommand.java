package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.ClientBook;
import seedu.address.model.Model;
import seedu.address.storage.Storage;

/**
 * Clears the client book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Client book has been cleared!";


    @Override
    public CommandResult execute(Model model, Storage storage) {
        requireNonNull(model);
        model.setClientBook(new ClientBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
