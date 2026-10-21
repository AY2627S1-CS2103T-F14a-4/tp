package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.NoteDeleteCommand;

public class NoteDeleteCommandParserTest {
    private final NoteDeleteCommandParser parser = new NoteDeleteCommandParser();

    @Test
    public void parse_twoIndices_success() {
        assertParseSuccess(parser, " 1 \t 2  ",
                new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
    }

    @Test
    public void parse_missingOrExtraArguments_rejected() {
        for (String args : List.of("", "  ", "1", "1 2 3", "1 2 n/Text")) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteDeleteCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_invalidIndices_rejected() {
        for (String index : List.of("0", "-1", "+1", "1.5", "abc", "2147483648")) {
            assertParseFailure(parser, index + " 1", ParserUtil.MESSAGE_INVALID_INDEX);
            assertParseFailure(parser, "1 " + index, ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_null_rejected() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_routesToNoteDeleteParser() throws Exception {
        assertEquals(new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON),
                new AddressBookParser().parseCommand("notedelete 1 2"));
    }
}
