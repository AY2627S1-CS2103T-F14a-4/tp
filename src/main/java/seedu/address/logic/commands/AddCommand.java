package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * Adds a person to the client book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = "Parameters: "
            + PREFIX_RELATIONSHIP + "RELATIONSHIP "
            + PREFIX_NAME + "NAME "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_PHONE + "PHONE_NUMBER";

    public static final String MESSAGE_SUCCESS = "New person added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This contact already exists.";
    public static final String MESSAGE_DUPLICATE_EMAIL = "A contact with this email address already exists.";
    public static final String MESSAGE_DUPLICATE_PHONE = "A contact with this phone number already exists.";
    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireNonNull(model);

        boolean hasMatchingEmail = model.getClientBook().getPersonList().stream()
                .anyMatch(person -> person.getEmail().equals(toAdd.getEmail()));
        boolean hasMatchingPhone = model.getClientBook().getPersonList().stream()
                .anyMatch(person -> person.getPhone().hasSamePhoneNumber(toAdd.getPhone()));
        boolean hasMatchingEmailAndPhone = model.getClientBook().getPersonList().stream()
                .anyMatch(person -> person.getEmail().equals(toAdd.getEmail())
                        && person.getPhone().hasSamePhoneNumber(toAdd.getPhone()));

        if (hasMatchingEmailAndPhone) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }
        if (hasMatchingEmail) {
            throw new CommandException(MESSAGE_DUPLICATE_EMAIL);
        }
        if (hasMatchingPhone) {
            throw new CommandException(MESSAGE_DUPLICATE_PHONE);
        }

        model.addPerson(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(toAdd)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
