package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    public static final String MESSAGE_INDEX_REQUIRED = "Client index is required.";
    public static final String MESSAGE_TOO_MANY_ARGUMENTS = "Delete command only accepts a client index.";
    public static final String MESSAGE_INVALID_INDEX = "Client index must be a positive integer.";

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(MESSAGE_INDEX_REQUIRED);
        }
        if (trimmedArgs.split("\\s+").length > 1) {
            throw new ParseException(MESSAGE_TOO_MANY_ARGUMENTS);
        }
        try {
            Index index = ParserUtil.parseIndex(trimmedArgs);
            return new DeleteCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(MESSAGE_INVALID_INDEX, pe);
        }
    }

}
