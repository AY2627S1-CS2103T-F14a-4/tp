package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Arrays;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.TagDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/** Parses an index followed by an optional list of whitespace-separated tags. */
public class TagDeleteCommandParser implements Parser<TagDeleteCommand> {

    @Override
    public TagDeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagDeleteCommand.MESSAGE_USAGE));
        }

        String[] arguments = trimmedArgs.split("\\s+");
        Index index = ParserUtil.parseIndex(arguments[0]);
        Set<Tag> tags = arguments.length == 1
                ? Set.of()
                : ParserUtil.parseTags(Arrays.asList(arguments).subList(1, arguments.length));
        return new TagDeleteCommand(index, tags);
    }
}
