package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ACTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE_DATE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NextCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NextAction;

/** Parses a displayed index, action description and due date. */
public class NextCommandParser implements Parser<NextCommand> {
    public static final String MESSAGE_INDEX_REQUIRED = "Person index is required.";
    public static final String MESSAGE_ACTION_REQUIRED = "Next action is required: use a/ACTION.";
    public static final String MESSAGE_DATE_REQUIRED = "Due date is required: use d/yyyy-MM-dd.";

    @Override
    public NextCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + args, PREFIX_ACTION, PREFIX_DUE_DATE);
        if (fields.getPreamble().isBlank()) {
            throw new ParseException(MESSAGE_INDEX_REQUIRED);
        }
        Index index = ParserUtil.parseIndex(fields.getPreamble());
        fields.verifyNoDuplicatePrefixesFor(PREFIX_ACTION, PREFIX_DUE_DATE);
        if (fields.getValue(PREFIX_ACTION).isEmpty()) {
            throw new ParseException(MESSAGE_ACTION_REQUIRED);
        }
        if (fields.getValue(PREFIX_DUE_DATE).isEmpty()
                || fields.getValue(PREFIX_DUE_DATE).orElseThrow().isBlank()) {
            throw new ParseException(MESSAGE_DATE_REQUIRED);
        }
        try {
            NextAction nextAction = new NextAction(fields.getValue(PREFIX_ACTION).orElseThrow(),
                    NextAction.parseDate(fields.getValue(PREFIX_DUE_DATE).orElseThrow()), false);
            return new NextCommand(index, nextAction);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
