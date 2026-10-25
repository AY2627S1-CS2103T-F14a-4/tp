package seedu.address.logic.commands;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns the visible person at the specified index in the complete client book.
     *
     * @throws CommandException if the index does not identify a person in the current filtered list
     */
    protected static Person getVisiblePersonAtIndex(Model model, Index index, String invalidIndexMessage)
            throws CommandException {
        List<Person> allPersons = model.getClientBook().getPersonList();
        if (index.getZeroBased() >= allPersons.size()) {
            throw new CommandException(invalidIndexMessage);
        }

        Person person = allPersons.get(index.getZeroBased());
        if (!model.getFilteredPersonList().contains(person)) {
            throw new CommandException(invalidIndexMessage);
        }
        return person;
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}
