package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.model.person.InteractionNote;

public class NoteCommandParserTest {
    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_pastDateAndRealisticText_success() {
        String text = "客户's plan — https://example.com/a/b; n/text d/2000-01-01 " + "A".repeat(1000);
        assertParseSuccess(parser, " 1 d/2000-02-29 n/" + text + "  ",
                new NoteCommand(INDEX_FIRST_PERSON, new InteractionNote(LocalDate.of(2000, 2, 29), text)));
    }

    @Test
    public void parse_missingFields_clearErrors() {
        for (String args : List.of("", "d/2026-10-06 n/Text", "n/Text")) {
            assertParseFailure(parser, args, NoteCommandParser.MESSAGE_INDEX_REQUIRED);
        }
        assertParseFailure(parser, "1 n/Text", NoteCommandParser.MESSAGE_DATE_REQUIRED);
        assertParseFailure(parser, "1 d/ n/Text", NoteCommandParser.MESSAGE_DATE_REQUIRED);
        assertParseFailure(parser, "1 d/2026-10-06", NoteCommandParser.MESSAGE_NOTE_REQUIRED);
        assertParseFailure(parser, "1 d/2026-10-06 n/   ", InteractionNote.MESSAGE_BLANK);
        assertParseFailure(parser, "0 d/2026-10-06 n/Text", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidDates_clearError() {
        for (String date : List.of("2026-02-30", "2025-02-29", "2026-00-01", "2026-2-01", "tomorrow")) {
            assertParseFailure(parser, "1 d/" + date + " n/Text", InteractionNote.MESSAGE_INVALID_DATE);
        }
    }
}
