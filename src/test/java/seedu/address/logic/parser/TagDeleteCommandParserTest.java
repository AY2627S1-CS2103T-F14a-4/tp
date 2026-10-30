package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.TagDeleteCommand;
import seedu.address.model.tag.Tag;

/** Contains parsing tests for deleting all tags or a selected set of tags. */
public class TagDeleteCommandParserTest {
    private final TagDeleteCommandParser parser = new TagDeleteCommandParser();

    @Test
    public void parse_indexOnly_success() {
        assertParseSuccess(parser, " 1 ", new TagDeleteCommand(INDEX_FIRST_PERSON, Set.of()));
    }

    @Test
    public void parse_indexAndTags_success() {
        assertParseSuccess(parser, " 1 friend referral friend ", new TagDeleteCommand(INDEX_FIRST_PERSON,
                Set.of(new Tag("friend"), new Tag("referral"))));
    }

    @Test
    public void parse_missingIndex_failure() {
        for (String args : List.of("", "  ")) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagDeleteCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_invalidIndexOrTag_failure() {
        assertParseFailure(parser, "0", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1 friend", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 close-friend", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_null_failure() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
