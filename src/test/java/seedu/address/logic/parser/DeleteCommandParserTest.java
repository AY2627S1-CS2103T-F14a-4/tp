package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;

public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
        // leading and trailing spaces are ignored
        assertParseSuccess(parser, "   1   ", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        assertParseFailure(parser, "", DeleteCommandParser.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, "   ", DeleteCommandParser.MESSAGE_INDEX_REQUIRED);
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        assertParseFailure(parser, "1 2", DeleteCommandParser.MESSAGE_TOO_MANY_ARGUMENTS);
        assertParseFailure(parser, "1 abc", DeleteCommandParser.MESSAGE_TOO_MANY_ARGUMENTS);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "a", DeleteCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "0", DeleteCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1", DeleteCommandParser.MESSAGE_INVALID_INDEX);
    }
}
