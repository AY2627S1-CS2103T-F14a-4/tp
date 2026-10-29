package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.TagCommand;
import seedu.address.model.tag.Tag;

public class TagCommandParserTest {
    private final TagCommandParser parser = new TagCommandParser();

    @Test
    public void usageMessage_containsOnlyParameters() {
        assertEquals("Parameters: INDEX TAG [MORE_TAGS...]", TagCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_validIndexAndTags_success() {
        assertParseSuccess(parser, " 1 priority referral ",
                new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("priority"), new Tag("referral"))));
    }

    @Test
    public void parse_duplicateTags_failure() {
        assertParseFailure(parser, "1 priority priority",
                String.format(TagCommand.MESSAGE_DUPLICATE_INPUT_TAGS, "[priority]"));
        assertParseFailure(parser, "1 priority PRIORITY",
                String.format(TagCommand.MESSAGE_DUPLICATE_INPUT_TAGS, "[PRIORITY]"));
        assertParseFailure(parser, "1 priority PRIORITY referral REFERRAL",
                String.format(TagCommand.MESSAGE_DUPLICATE_INPUT_TAGS, "[PRIORITY, REFERRAL]"));
    }

    @Test
    public void parse_missingIndexOrTags_failure() {
        assertParseFailure(parser, "",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "priority",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidIndexOrTag_failure() {
        assertParseFailure(parser, "0 priority", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 close-friend", Tag.MESSAGE_CONSTRAINTS);
    }
}
