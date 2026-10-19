package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DoneCommand;

public class DoneCommandParserTest {
    private final DoneCommandParser parser = new DoneCommandParser();

    @Test
    public void parse_validIndex_returnsDoneCommand() {
        assertParseSuccess(parser, "1", new DoneCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " \t 1 \t ", new DoneCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "2147483647", new DoneCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_emptyArguments_reportsMissingIndex() {
        for (String input : List.of("", "   ", "\t\n")) {
            assertParseFailure(parser, input, DoneCommandParser.MESSAGE_INDEX_REQUIRED);
        }
    }

    @Test
    public void parse_invalidIndex_reportsParserUtilError() {
        for (String input : List.of("0", "-1", "abc", "1.5", "+1", "1 2", "1 abc", "1\t2",
                "2147483648", "99999999999999999999")) {
            assertParseFailure(parser, input, ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }
}
