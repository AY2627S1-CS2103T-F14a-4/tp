package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ACTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE_DATE;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RescheduleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NextAction;

/** Parses a displayed index, replacement date and optional follow-up description. */
public class RescheduleCommandParser implements Parser<RescheduleCommand> {
    @Override
    public RescheduleCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + args, PREFIX_ACTION, PREFIX_DUE_DATE);
        if (fields.getPreamble().isBlank()) {
            throw new ParseException(NextCommandParser.MESSAGE_INDEX_REQUIRED);
        }
        Index index = ParserUtil.parseIndex(fields.getPreamble());
        fields.verifyNoDuplicatePrefixesFor(PREFIX_ACTION, PREFIX_DUE_DATE);
        if (fields.getValue(PREFIX_DUE_DATE).isEmpty()
                || fields.getValue(PREFIX_DUE_DATE).orElseThrow().isBlank()) {
            throw new ParseException(NextCommandParser.MESSAGE_DATE_REQUIRED);
        }
        try {
            LocalDate dueDate = NextAction.parseDate(fields.getValue(PREFIX_DUE_DATE).orElseThrow());
            String action = fields.getValue(PREFIX_ACTION)
                    .map(NextAction::parseAction).orElse(null);
            return new RescheduleCommand(index, dueDate, action);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
