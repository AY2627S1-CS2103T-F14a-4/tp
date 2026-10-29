package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.TagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/** Parses an index followed by one or more whitespace-separated tags. */
public class TagCommandParser implements Parser<TagCommand> {

    @Override
    public TagCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        String[] arguments = trimmedArgs.isEmpty() ? new String[0] : trimmedArgs.split("\\s+");
        if (arguments.length < 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseIndex(arguments[0]);
        List<Tag> parsedTags = new ArrayList<>();
        for (String tagName : Arrays.asList(arguments).subList(1, arguments.length)) {
            parsedTags.add(ParserUtil.parseTag(tagName));
        }

        Set<Tag> tags = new HashSet<>();
        Set<Tag> duplicateTags = new HashSet<>();
        for (Tag tag : parsedTags) {
            if (!tags.add(tag)) {
                duplicateTags.add(tag);
            }
        }
        if (!duplicateTags.isEmpty()) {
            throw new ParseException(String.format(TagCommand.MESSAGE_DUPLICATE_INPUT_TAGS,
                    formatTags(duplicateTags)));
        }
        return new TagCommand(index, tags);
    }

    private static String formatTags(Set<Tag> tags) {
        return tags.stream().map(tag -> tag.tagName).sorted(String.CASE_INSENSITIVE_ORDER).toList().toString();
    }
}
