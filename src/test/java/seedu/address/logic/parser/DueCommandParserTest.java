package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DueCommand;

public class DueCommandParserTest {
    private final DueCommandParser parser = new DueCommandParser();

    @Test
    public void parse_validDays_returnsDueCommand() {
        assertParseSuccess(parser, "", new DueCommand());
        assertParseSuccess(parser, " \t ", new DueCommand(0));
        for (int days : new int[] {0, 7, 365}) {
            assertParseSuccess(parser, Integer.toString(days), new DueCommand(days));
        }
        assertParseSuccess(parser, " \t7\t ", new DueCommand(7));
    }

    @Test
    public void parse_invalidDays_throwsParseException() {
        for (String input : List.of("-1", "366", "abc", "7 8", "7\t8", "1.5", "+1", "2147483648")) {
            assertParseFailure(parser, input, DueCommandParser.MESSAGE_INVALID_DAYS);
        }
    }
}
