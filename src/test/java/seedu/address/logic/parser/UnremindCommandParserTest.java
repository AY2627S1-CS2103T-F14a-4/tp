package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.UnremindCommand;
import seedu.address.model.person.RecurringReminder;

public class UnremindCommandParserTest {
    private final UnremindCommandParser parser = new UnremindCommandParser();

    @Test
    public void parse_validLabelAndDispatch_success() throws Exception {
        UnremindCommand expected = new UnremindCommand(INDEX_FIRST_PERSON, "bIRTHDAY");
        assertParseSuccess(parser, " 1 a/ bIRTHDAY ", expected);
        assertParseSuccess(parser, " 1 a/Policy renewal", new UnremindCommand(INDEX_FIRST_PERSON, "Policy renewal"));
        assertEquals(expected, new ClientBookParser().parseCommand("unremind 1 a/bIRTHDAY"));
    }

    @Test
    public void parse_missingInvalidOrDuplicateFields_failure() {
        for (String index : List.of("0", "-1", "abc", "2147483648")) {
            assertParseFailure(parser, " " + index + " a/Birthday", ParserUtil.MESSAGE_INVALID_INDEX);
        }
        assertParseFailure(parser, " 1", UnremindCommand.MESSAGE_USAGE);
        for (String label : List.of("", " ", "a".repeat(51), "Birthday d/", "A/B", "A\tB")) {
            assertParseFailure(parser, " 1 a/" + label, RecurringReminder.MESSAGE_LABEL_CONSTRAINTS);
        }
        assertParseFailure(parser, " 1 a/Birthday a/Renewal",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ACTION));
    }

    @Test
    public void parse_extraArguments_reportsUnexpectedArgument() {
        assertParseFailure(parser, "1 extra",
                "unremind accepts exactly one index. Unexpected argument: extra");
        for (String separator : List.of(" ", "  ", "\t", "\n")) {
            assertParseFailure(parser, " 1" + separator + "extra  more a/Birthday",
                    "unremind accepts exactly one index. Unexpected argument: extra  more");
        }
        assertParseFailure(parser, "1 2 a/Birthday",
                "unremind accepts exactly one index. Unexpected argument: 2");
    }

    @Test
    public void parse_missingIndex_reportsUsage() {
        for (String input : List.of("", " \t\n", " a/Birthday")) {
            assertParseFailure(parser, input,
                    UnremindCommand.MESSAGE_USAGE);
        }
    }
}
