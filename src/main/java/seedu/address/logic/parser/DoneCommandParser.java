package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.DoneCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a single displayed index for the done command. */
public class DoneCommandParser implements Parser<DoneCommand> {
    public static final String MESSAGE_INDEX_REQUIRED = "Person index is required.";

    @Override
    public DoneCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            throw new ParseException(MESSAGE_INDEX_REQUIRED);
        }
        return new DoneCommand(ParserUtil.parseIndex(args));
    }
}
