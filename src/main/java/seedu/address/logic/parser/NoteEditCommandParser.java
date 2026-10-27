package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;

/** Parses two indices, an optional date and an optional note body, preserving prefix-like text in the body. */
public class NoteEditCommandParser implements Parser<NoteEditCommand> {
    private static final Pattern FORMAT = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s+(.*)$", Pattern.DOTALL);
    private static final Pattern NOTE_PREFIX = Pattern.compile("(?<!\\S)n/");

    @Override
    public NoteEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = FORMAT.matcher(args);
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteEditCommand.MESSAGE_USAGE));
        }
        Index contactIndex = ParserUtil.parseIndex(matcher.group(1));
        Index noteIndex = ParserUtil.parseIndex(matcher.group(2));
        String fields = matcher.group(3);
        LocalDate date = null;
        String text = null;
        if (fields.startsWith("n/")) {
            text = fields.substring(2);
        } else if (fields.startsWith("d/")) {
            Matcher noteMatcher = NOTE_PREFIX.matcher(fields);
            boolean hasText = noteMatcher.find();
            String dateText = fields.substring(2, hasText ? noteMatcher.start() : fields.length()).strip();
            try {
                date = InteractionNote.parseDate(dateText);
            } catch (IllegalArgumentException e) {
                throw new ParseException(e.getMessage(), e);
            }
            if (hasText) {
                text = fields.substring(noteMatcher.end());
            }
        } else {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteEditCommand.MESSAGE_USAGE));
        }
        if (text != null && text.isBlank()) {
            throw new ParseException(InteractionNote.MESSAGE_BLANK);
        }
        return new NoteEditCommand(contactIndex, noteIndex, date, text);
    }
}
