package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ACTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE_DATE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RemindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.RecurringReminder;

/** Parses a displayed index, reminder label and yearly month/day. */
public class RemindCommandParser implements Parser<RemindCommand> {
    @Override
    public RemindCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + args, PREFIX_ACTION, PREFIX_DUE_DATE);
        Index index = ParserUtil.parseIndex(fields.getPreamble());
        fields.verifyNoDuplicatePrefixesFor(PREFIX_ACTION, PREFIX_DUE_DATE);
        if (fields.getValue(PREFIX_ACTION).isEmpty() || fields.getValue(PREFIX_DUE_DATE).isEmpty()) {
            throw new ParseException(RemindCommand.MESSAGE_USAGE);
        }
        try {
            return new RemindCommand(index, new RecurringReminder(fields.getValue(PREFIX_ACTION).orElseThrow(),
                    RecurringReminder.parseDate(fields.getValue(PREFIX_DUE_DATE).orElseThrow())));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
