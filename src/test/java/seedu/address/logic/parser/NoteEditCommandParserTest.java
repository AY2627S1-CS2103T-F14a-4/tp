package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.NoteEditCommand;
import seedu.address.model.person.InteractionNote;

public class NoteEditCommandParserTest {
    private final NoteEditCommandParser parser = new NoteEditCommandParser();

    @Test
    public void parse_validText_preservesContentAndTrimsEdges() {
        String text = "客户's plan; https://example.com/a/b n/text d/2000-01-01 " + "A".repeat(1000);
        assertParseSuccess(parser, " 1 \t 2 n/  " + text + "  ",
                new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, text));
    }

    @Test
    public void parse_missingOrExtraFields_rejected() {
        for (String args : List.of("", "1", "1 2", "1 n/Text", "1 2 3 n/Text", "1 2 d/2000-01-01 n/Text")) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteEditCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_invalidIndices_rejected() {
        for (String index : List.of("0", "-1", "1.5", "abc", "2147483648")) {
            assertParseFailure(parser, index + " 1 n/Text", ParserUtil.MESSAGE_INVALID_INDEX);
            assertParseFailure(parser, "1 " + index + " n/Text", ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_blankContent_rejected() {
        for (String args : List.of("1 2 n/", "1 2 n/  \t ", "1 2 n/\u2003")) {
            assertParseFailure(parser, args, InteractionNote.MESSAGE_BLANK);
        }
    }

    @Test
    public void parseCommand_routesToNoteEditParser() throws Exception {
        assertEquals(new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, "Updated"),
                new ClientBookParser().parseCommand("noteedit 1 2 n/Updated"));
    }
}
