package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.NextCommand;
import seedu.address.model.person.NextAction;

public class NextCommandParserTest {
    private final NextCommandParser parser = new NextCommandParser();

    @Test
    public void parse_validFieldsInEitherOrder_success() {
        NextCommand expected = new NextCommand(INDEX_FIRST_PERSON,
                new NextAction("Call client", LocalDate.of(2000, 1, 1), false));
        assertParseSuccess(parser, " 1 a/  Call client  d/2000-01-01 ", expected);
        assertParseSuccess(parser, " 1 d/2000-01-01 a/Call client", expected);
        assertParseSuccess(parser, " 1 a/" + "a".repeat(200) + " d/2024-02-29",
                new NextCommand(INDEX_FIRST_PERSON,
                        new NextAction("a".repeat(200), LocalDate.of(2024, 2, 29), false)));
    }

    @Test
    public void parse_boundaryDateYears_success() {
        for (LocalDate date : List.of(LocalDate.of(1, 1, 1), LocalDate.of(9999, 12, 31))) {
            assertParseSuccess(parser, " 1 a/Call d/" + date,
                    new NextCommand(INDEX_FIRST_PERSON, new NextAction("Call", date, false)));
        }
    }

    @Test
    public void parse_missingFields_failure() {
        assertParseFailure(parser, "", NextCommandParser.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, " a/Call d/2026-10-19", NextCommandParser.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, "a/Call d/2026-10-19", NextCommandParser.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, " 1 d/2026-10-19", NextCommandParser.MESSAGE_ACTION_REQUIRED);
        assertParseFailure(parser, " 1 a/Call", NextCommandParser.MESSAGE_DATE_REQUIRED);
        assertParseFailure(parser, " 1 a/Call d/", NextCommandParser.MESSAGE_DATE_REQUIRED);
        assertParseFailure(parser, " 1 a/ d/2026-10-19", NextAction.MESSAGE_ACTION_CONSTRAINTS);
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String index : List.of("0", "-1", "2147483648", "abc", "1.5", "1 2")) {
            assertParseFailure(parser, " " + index + " a/Call d/2026-10-19", ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_invalidDateOrLongAction_failure() {
        for (String date : List.of("2026-02-30", "2026-02-29", "2026-2-03", "2026-13-01",
                "0000-01-01", "0000-02-29", "-0001-01-01", "10000-01-01")) {
            assertParseFailure(parser, " 1 a/Call d/" + date, NextAction.MESSAGE_INVALID_DATE);
        }
        assertParseFailure(parser, " 1 a/" + "a".repeat(201) + " d/2026-10-19",
                NextAction.MESSAGE_ACTION_CONSTRAINTS);
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, " 1 a/Call a/Email d/2026-10-19",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ACTION));
        assertParseFailure(parser, " 1 a/Call d/2026-10-19 d/2026-10-20",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DUE_DATE));
    }
}
