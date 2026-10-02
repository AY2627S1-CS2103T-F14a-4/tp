package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Relationship;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_RELATIONSHIP, PREFIX_NAME, PREFIX_EMAIL, PREFIX_PHONE);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        validateRequiredPrefixes(argMultimap);
        validateNoDuplicatePrefixes(argMultimap);
        Relationship relationship = ParserUtil.parseRelationship(argMultimap.getValue(PREFIX_RELATIONSHIP).get());
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        // Tag support for add is disabled. Restore PREFIX_TAG to the tokenizer before re-enabling this line.
        // Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
        Person person = new Person(name, phone, email, relationship);

        return new AddCommand(person);
    }

    private static void validateRequiredPrefixes(ArgumentMultimap argumentMultimap) throws ParseException {
        boolean hasRelationship = argumentMultimap.getValue(PREFIX_RELATIONSHIP).isPresent();
        boolean hasName = argumentMultimap.getValue(PREFIX_NAME).isPresent();
        boolean hasEmail = argumentMultimap.getValue(PREFIX_EMAIL).isPresent();
        boolean hasPhone = argumentMultimap.getValue(PREFIX_PHONE).isPresent();

        if (!hasRelationship && !hasName && !hasEmail && !hasPhone) {
            throw new ParseException(AddCommand.MESSAGE_ALL_PARAMETERS_REQUIRED);
        }
        if (!hasRelationship) {
            throw new ParseException(AddCommand.MESSAGE_RELATIONSHIP_REQUIRED);
        }
        if (!hasName) {
            throw new ParseException(AddCommand.MESSAGE_NAME_REQUIRED);
        }
        if (!hasEmail) {
            throw new ParseException(AddCommand.MESSAGE_EMAIL_REQUIRED);
        }
        if (!hasPhone) {
            throw new ParseException(AddCommand.MESSAGE_PHONE_REQUIRED);
        }
    }

    private static void validateNoDuplicatePrefixes(ArgumentMultimap argumentMultimap) throws ParseException {
        if (argumentMultimap.getAllValues(PREFIX_RELATIONSHIP).size() > 1) {
            throw new ParseException(AddCommand.MESSAGE_RELATIONSHIP_DUPLICATED);
        }
        if (argumentMultimap.getAllValues(PREFIX_NAME).size() > 1) {
            throw new ParseException(AddCommand.MESSAGE_NAME_DUPLICATED);
        }
        if (argumentMultimap.getAllValues(PREFIX_EMAIL).size() > 1) {
            throw new ParseException(AddCommand.MESSAGE_EMAIL_DUPLICATED);
        }
        if (argumentMultimap.getAllValues(PREFIX_PHONE).size() > 1) {
            throw new ParseException(AddCommand.MESSAGE_PHONE_DUPLICATED);
        }
    }

}
