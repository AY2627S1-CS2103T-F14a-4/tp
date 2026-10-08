package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists all persons in the address book who match the specified search criterion.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all persons whose names contain any of "
            + "the specified keywords, or whose relationship matches the specified relationship "
            + "(case-insensitive), and displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]... | " + PREFIX_RELATIONSHIP + "RELATIONSHIP\n"
            + "Examples:\n"
            + COMMAND_WORD + " alice bob charlie\n"
            + COMMAND_WORD + " " + PREFIX_RELATIONSHIP + "client";

    private final Predicate<Person> predicate;

    /**
     * Creates a command that finds persons matching the specified {@code predicate}.
     */
    public FindCommand(Predicate<Person> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
