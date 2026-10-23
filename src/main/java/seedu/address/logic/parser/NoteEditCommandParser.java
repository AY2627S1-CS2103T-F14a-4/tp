package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;

/** Parses two indices followed by a note body, preserving prefix-like text in the body. */
public class NoteEditCommandParser implements Parser<NoteEditCommand> {
    private static final Pattern FORMAT = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s+n/(.*)$", Pattern.DOTALL);

    @Override
    public NoteEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = FORMAT.matcher(args);
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteEditCommand.MESSAGE_USAGE));
        }
        Index contactIndex = ParserUtil.parseIndex(matcher.group(1));
        Index noteIndex = ParserUtil.parseIndex(matcher.group(2));
        String text = matcher.group(3);
        if (text.isBlank()) {
            throw new ParseException(InteractionNote.MESSAGE_BLANK);
        }
        return new NoteEditCommand(contactIndex, noteIndex, text);
    }
}
