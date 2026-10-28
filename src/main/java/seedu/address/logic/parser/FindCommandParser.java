package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAIL_KEYWORD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonMatchesFiltersPredicate;
import seedu.address.model.person.Relationship;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    private static final Pattern VALID_NAME_KEYWORD_PATTERN = Pattern.compile(
            "(?:[\\p{L}]+(?:[-'][\\p{L}]+)*|s/o|d/o|w/o)", Pattern.CASE_INSENSITIVE);

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
                PREFIX_TAG, PREFIX_DETAIL_KEYWORD);
        argumentMultimap.verifyNoDuplicatePrefixesFor(PREFIX_RELATIONSHIP, PREFIX_TAG, PREFIX_DETAIL_KEYWORD);
        Optional<String> relationshipValue = argumentMultimap.getValue(PREFIX_RELATIONSHIP);
        Optional<Relationship> relationship = relationshipValue.isEmpty()
                ? Optional.empty() : Optional.of(ParserUtil.parseRelationship(relationshipValue.get()));
        String nameKeywordsValue = argumentMultimap.getPreamble();
        List<String> nameKeywords = parseNameKeywords(nameKeywordsValue);
        List<String> tagKeywords = parseTagKeywords(argumentMultimap.getValue(PREFIX_TAG));
        List<String> detailKeywords = parseDetailKeywords(argumentMultimap.getValue(PREFIX_DETAIL_KEYWORD));
        return new FindCommand(new PersonMatchesFiltersPredicate(nameKeywords, relationship, tagKeywords,
                detailKeywords));
    }

    private static List<String> parseNameKeywords(String nameKeywordsValue) throws ParseException {
        String normalizedNameKeywords = nameKeywordsValue.trim();
        if (normalizedNameKeywords.isEmpty()) {
            return List.of();
        }
        List<String> nameKeywords = List.of(normalizedNameKeywords.split("\\s+"));
        boolean hasInvalidKeyword = nameKeywords.stream()
                .anyMatch(keyword -> !VALID_NAME_KEYWORD_PATTERN.matcher(keyword).matches());
        if (hasInvalidKeyword) {
            throw new ParseException(Name.MESSAGE_INVALID_CHARACTERS);
        }
        return nameKeywords;
    }

    private static List<String> parseTagKeywords(Optional<String> tagValue) throws ParseException {
        if (tagValue.isEmpty()) {
            return List.of();
        }
        Set<Tag> tags = ParserUtil.parseTags(List.of(tagValue.get().split("\\s+")));
        return tags.stream().map(tag -> tag.tagName).sorted().toList();
    }

    private static List<String> parseDetailKeywords(Optional<String> detailValue) throws ParseException {
        if (detailValue.isEmpty()) {
            return List.of();
        }
        String normalizedDetailKeywords = detailValue.get().trim();
        if (normalizedDetailKeywords.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        return List.of(normalizedDetailKeywords.split("\\s+"));
    }
}
