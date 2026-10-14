package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
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

    // Boundary and whitespace tests drafted with OpenAI ChatGPT/Codex assistance for ZachL1113.
    @Test
    public void parse_largestSupportedIndex_returnsDeleteCommand() {
        assertParseSuccess(parser, "2147483647", new DeleteCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_overflowOrNonIntegerIndex_throwsParseException() {
        for (String input : new String[] {"2147483648", "999999999999999999999", "1.5", "+1", "1abc"}) {
            assertParseFailure(parser, input, DeleteCommandParser.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_whitespaceAroundSingleIndex_returnsDeleteCommand() {
        assertParseSuccess(parser, "\t 1 \t", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_argumentsSeparatedByTabs_throwsParseException() {
        assertParseFailure(parser, "1\t2", DeleteCommandParser.MESSAGE_TOO_MANY_ARGUMENTS);
        assertParseFailure(parser, "1\tabc", DeleteCommandParser.MESSAGE_TOO_MANY_ARGUMENTS);
    }
}

