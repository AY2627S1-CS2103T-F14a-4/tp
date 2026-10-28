package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTERACTION_OUTCOME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTERACTION_TYPE;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.InteractionOutcome;
import seedu.address.model.person.InteractionType;

/** Parses date and optional interaction metadata before the literal note body. */
public class NoteCommandParser implements Parser<NoteCommand> {
    public static final String MESSAGE_INDEX_REQUIRED = "Person index is required.";
    public static final String MESSAGE_DATE_REQUIRED = "Interaction date is required: use d/yyyy-MM-dd before n/NOTE.";
    public static final String MESSAGE_NOTE_REQUIRED = "Interaction note is required: use n/NOTE after the date.";

    private static final Pattern INDEX = Pattern.compile("^\\s*(\\S+)(?:\\s+|$)");
    private static final Pattern NOTE_PREFIX = Pattern.compile("(?<!\\S)n/");

    @Override
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher indexMatcher = INDEX.matcher(args);
        if (!indexMatcher.find() || indexMatcher.group(1).startsWith("d/")
                || indexMatcher.group(1).startsWith("n/") || indexMatcher.group(1).startsWith("t/")
                || indexMatcher.group(1).startsWith("o/")) {
            throw new ParseException(MESSAGE_INDEX_REQUIRED);
        }
        Index index = ParserUtil.parseIndex(indexMatcher.group(1));
        String fields = args.substring(indexMatcher.end()).strip();
        if (!fields.startsWith("d/")) {
            throw new ParseException(MESSAGE_DATE_REQUIRED);
        }
        Matcher noteMatcher = NOTE_PREFIX.matcher(fields);
        if (!noteMatcher.find()) {
            throw new ParseException(MESSAGE_NOTE_REQUIRED);
        }
        ArgumentMultimap header = ArgumentTokenizer.tokenize(" " + fields.substring(0, noteMatcher.start())
                .replaceAll("\\s", " "), PREFIX_DUE_DATE, PREFIX_INTERACTION_TYPE, PREFIX_INTERACTION_OUTCOME);
        header.verifyNoDuplicatePrefixesFor(PREFIX_DUE_DATE, PREFIX_INTERACTION_TYPE, PREFIX_INTERACTION_OUTCOME);
        String dateText = header.getValue(PREFIX_DUE_DATE).orElse("");
        if (dateText.isEmpty()) {
            throw new ParseException(MESSAGE_DATE_REQUIRED);
        }
        try {
            LocalDate date = InteractionNote.parseDate(dateText);
            InteractionType type = header.getValue(PREFIX_INTERACTION_TYPE)
                    .map(InteractionType::parse).orElse(InteractionType.UNSPECIFIED);
            InteractionOutcome outcome = header.getValue(PREFIX_INTERACTION_OUTCOME)
                    .map(InteractionOutcome::parse).orElse(InteractionOutcome.UNSPECIFIED);
            return new NoteCommand(index,
                    new InteractionNote(date, fields.substring(noteMatcher.end()), type, outcome));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
