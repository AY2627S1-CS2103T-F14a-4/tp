package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.MonthDay;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RemindCommand;
import seedu.address.model.person.RecurringReminder;

public class RemindCommandParserTest {
    private final RemindCommandParser parser = new RemindCommandParser();

    @Test
    public void parse_validFieldsInEitherOrderAndCommandDispatch_success() throws Exception {
        RemindCommand expected = new RemindCommand(INDEX_FIRST_PERSON,
                new RecurringReminder("Birthday", MonthDay.of(2, 29)));
        assertParseSuccess(parser, " 1 a/ Birthday  d/02-29 ", expected);
        assertParseSuccess(parser, " 1 d/02-29 a/Birthday", expected);
        assertEquals(expected, new ClientBookParser().parseCommand("remind 1 a/Birthday d/02-29"));
    }

    @Test
    public void parse_missingOrInvalidFields_failure() {
        for (String index : List.of("", "0", "-1", "abc", "1 2", "2147483648")) {
            assertParseFailure(parser, " " + index + " a/Birthday d/01-01", ParserUtil.MESSAGE_INVALID_INDEX);
        }
        assertParseFailure(parser, " 1 a/Birthday", RemindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 1 d/01-01", RemindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 1 a/ d/01-01", RecurringReminder.MESSAGE_LABEL_CONSTRAINTS);
        assertParseFailure(parser, " 1 a/" + "a".repeat(51) + " d/01-01",
                RecurringReminder.MESSAGE_LABEL_CONSTRAINTS);
        for (String date : List.of("", "2-29", "02-30", "2026-01-01", "13-01")) {
            assertParseFailure(parser, " 1 a/Birthday d/" + date, RecurringReminder.MESSAGE_INVALID_DATE);
        }
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, " 1 a/Birthday a/Renewal d/01-01",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ACTION));
        assertParseFailure(parser, " 1 a/Birthday d/01-01 d/02-02",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DUE_DATE));
    }
}
