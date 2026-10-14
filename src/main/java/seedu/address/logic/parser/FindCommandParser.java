package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;

import java.util.List;
import java.util.Optional;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Relationship;
import seedu.address.model.person.RelationshipMatchesPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        ArgumentMultimap argumentMultimap = ArgumentTokenizer.tokenize(" " + trimmedArgs, PREFIX_RELATIONSHIP);
        argumentMultimap.verifyNoDuplicatePrefixesFor(PREFIX_RELATIONSHIP);
        Optional<String> relationshipValue = argumentMultimap.getValue(PREFIX_RELATIONSHIP);
        if (relationshipValue.isPresent()) {
            if (!argumentMultimap.getPreamble().isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }

            Relationship relationship = ParserUtil.parseRelationship(relationshipValue.get());
            return new FindCommand(new RelationshipMatchesPredicate(relationship));
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

}
