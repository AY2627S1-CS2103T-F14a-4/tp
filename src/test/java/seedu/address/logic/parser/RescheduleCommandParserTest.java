package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RescheduleCommand;
import seedu.address.model.person.NextAction;

public class RescheduleCommandParserTest {
    private final RescheduleCommandParser parser = new RescheduleCommandParser();

    @Test
    public void parse_validDateOnlyOrDescription_success() {
        LocalDate date = LocalDate.of(2000, 1, 1);
        assertParseSuccess(parser, " 1 d/2000-01-01", new RescheduleCommand(INDEX_FIRST_PERSON, date, null));
        RescheduleCommand expected = new RescheduleCommand(INDEX_FIRST_PERSON, date, "Call client");
        assertParseSuccess(parser, " 1 d/2000-01-01 a/  Call client  ", expected);
        assertParseSuccess(parser, " 1 a/Call client d/2000-01-01 ", expected);
        for (LocalDate boundary : List.of(LocalDate.of(1, 1, 1), LocalDate.of(9999, 12, 31),
                LocalDate.of(2024, 2, 29))) {
            assertParseSuccess(parser, " 1 d/" + boundary,
                    new RescheduleCommand(INDEX_FIRST_PERSON, boundary, null));
        }
    }

    @Test
    public void parse_missingIndexOrDate_failure() {
        assertParseFailure(parser, "", NextCommandParser.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, "d/2026-10-20", NextCommandParser.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, " 1", NextCommandParser.MESSAGE_DATE_REQUIRED);
        assertParseFailure(parser, " 1 a/Call", NextCommandParser.MESSAGE_DATE_REQUIRED);
        assertParseFailure(parser, " 1 d/", NextCommandParser.MESSAGE_DATE_REQUIRED);
    }

    @Test
    public void parse_invalidIndexDateOrDescription_failure() {
        for (String index : List.of("0", "-1", "abc", "1.5", "2147483648", "1 2")) {
            assertParseFailure(parser, " " + index + " d/2026-10-20", ParserUtil.MESSAGE_INVALID_INDEX);
        }
        for (String date : List.of("2026-02-29", "2026-02-30", "2026-2-03", "0000-01-01",
                "10000-01-01", "2026-13-01", "2026-10-20 extra")) {
            assertParseFailure(parser, " 1 d/" + date, NextAction.MESSAGE_INVALID_DATE);
        }
        assertParseFailure(parser, " 1 d/2026-10-20 a/", NextAction.MESSAGE_ACTION_CONSTRAINTS);
        assertParseFailure(parser, " 1 d/2026-10-20 a/" + "a".repeat(201),
                NextAction.MESSAGE_ACTION_CONSTRAINTS);
        assertParseSuccess(parser, " 1 d/2026-10-20 a/" + "a".repeat(200),
                new RescheduleCommand(INDEX_FIRST_PERSON, LocalDate.of(2026, 10, 20), "a".repeat(200)));
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, " 1 d/2026-10-20 d/2026-10-21",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DUE_DATE));
        assertParseFailure(parser, " 1 d/2026-10-20 a/Call a/Email",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ACTION));
    }
}
