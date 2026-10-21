package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonMatchesFiltersPredicate;
import seedu.address.model.person.Relationship;
import seedu.address.model.tag.Tag;

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

        ArgumentMultimap argumentMultimap = ArgumentTokenizer.tokenize(" " + trimmedArgs, PREFIX_RELATIONSHIP,
                PREFIX_TAG);
        argumentMultimap.verifyNoDuplicatePrefixesFor(PREFIX_RELATIONSHIP, PREFIX_TAG);
        Optional<String> relationshipValue = argumentMultimap.getValue(PREFIX_RELATIONSHIP);
        Optional<Relationship> relationship = relationshipValue.isEmpty()
                ? Optional.empty() : Optional.of(ParserUtil.parseRelationship(relationshipValue.get()));
        String nameKeywordsValue = argumentMultimap.getPreamble();
        List<String> nameKeywords = nameKeywordsValue.isEmpty()
                ? List.of() : List.of(nameKeywordsValue.split("\\s+"));
        List<String> tagKeywords = parseTagKeywords(argumentMultimap.getValue(PREFIX_TAG));
        return new FindCommand(new PersonMatchesFiltersPredicate(nameKeywords, relationship, tagKeywords));
    }

    private static List<String> parseTagKeywords(Optional<String> tagValue) throws ParseException {
        if (tagValue.isEmpty()) {
            return List.of();
        }
        Set<Tag> tags = ParserUtil.parseTags(List.of(tagValue.get().split("\\s+")));
        return tags.stream().map(tag -> tag.tagName).sorted().toList();
    }
}
