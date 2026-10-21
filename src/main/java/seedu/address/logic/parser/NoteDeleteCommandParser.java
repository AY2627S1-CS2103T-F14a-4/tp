package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the contact and note indices for a note deletion. */
public class NoteDeleteCommandParser implements Parser<NoteDeleteCommand> {
    @Override
    public NoteDeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] indices = args.strip().split("\\s+");
        if (indices.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteDeleteCommand.MESSAGE_USAGE));
        }
        Index contactIndex = ParserUtil.parseIndex(indices[0]);
        Index noteIndex = ParserUtil.parseIndex(indices[1]);
        return new NoteDeleteCommand(contactIndex, noteIndex);
    }
}
