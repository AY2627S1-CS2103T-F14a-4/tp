package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an optional action lookahead for the due command. */
public class DueCommandParser implements Parser<DueCommand> {
    public static final String MESSAGE_INVALID_DAYS = "DAYS must be a whole number from 0 to 365, e.g. due 7";
    private static final int MAX_DAYS = 365;

    @Override
    public DueCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            return new DueCommand();
        }
        String trimmedArgs = args.strip();
        if (!trimmedArgs.matches("\\d+")) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
        try {
            int days = Integer.parseInt(trimmedArgs);
            if (days > MAX_DAYS) {
                throw new ParseException(MESSAGE_INVALID_DAYS);
            }
            return new DueCommand(days);
        } catch (NumberFormatException e) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
    }
}
