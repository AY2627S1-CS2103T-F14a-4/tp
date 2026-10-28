package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ACTION;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UnremindCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a displayed index and the label of a yearly reminder to remove. */
public class UnremindCommandParser implements Parser<UnremindCommand> {
    @Override
    public UnremindCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + args, PREFIX_ACTION);
        Index index = ParserUtil.parseIndex(fields.getPreamble());
        fields.verifyNoDuplicatePrefixesFor(PREFIX_ACTION);
        if (fields.getValue(PREFIX_ACTION).isEmpty()) {
            throw new ParseException(UnremindCommand.MESSAGE_USAGE);
        }
        try {
            return new UnremindCommand(index, fields.getValue(PREFIX_ACTION).orElseThrow());
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
