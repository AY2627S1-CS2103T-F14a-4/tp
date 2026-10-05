package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AREA_OF_INTEREST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FINANCIAL_NEED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.InfoCommand;
import seedu.address.logic.commands.InfoCommand.InfoDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the index and optional financial information supplied to an info command. */
public class InfoCommandParser implements Parser<InfoCommand> {
    public static final String MESSAGE_INDEX_REQUIRED = "Client index is required";
    public static final String MESSAGE_INVALID_INDEX = "Client index must be a positive integer.";
    public static final String MESSAGE_NO_PARAMETERS = "At least one of /fn, /pr or /ai is required.";
    public static final String MESSAGE_DUPLICATE_PARAMETER = "%s can only be specified once.";
    public static final String MESSAGE_INVALID_PARAMETER =
            "Invalid parameter. Valid parameters are /fn, /pr and /ai.";

    private static final Pattern PARAMETER = Pattern.compile("(?<!\\S)/\\S+");

    @Override
    public InfoCommand parse(String args) throws ParseException {
        requireNonNull(args);
        // Scan every slash-prefixed token, including unknown parameters. Keep raw values so control
        // characters are validated rather than silently removed by ArgumentTokenizer's trim().
        Matcher matcher = PARAMETER.matcher(args);
        Map<String, String> values = new HashMap<>();
        String previous = null;
        int valueStart = 0;
        int preambleEnd = args.length();
        while (matcher.find()) {
            String parameter = matcher.group();
            if (!parameter.equals(PREFIX_FINANCIAL_NEED.getPrefix())
                    && !parameter.equals(PREFIX_PRIORITY.getPrefix())
                    && !parameter.equals(PREFIX_AREA_OF_INTEREST.getPrefix())) {
                throw new ParseException(MESSAGE_INVALID_PARAMETER);
            }
            if (previous == null) {
                preambleEnd = matcher.start();
            } else {
                values.put(previous, args.substring(valueStart, matcher.start()));
            }
            if (values.containsKey(parameter)) {
                throw new ParseException(String.format(MESSAGE_DUPLICATE_PARAMETER, parameter));
            }
            previous = parameter;
            valueStart = matcher.end();
        }
        if (previous != null) {
            values.put(previous, args.substring(valueStart));
        }
        String indexText = args.substring(0, preambleEnd).trim();
        if (indexText.isEmpty()) {
            throw new ParseException(MESSAGE_INDEX_REQUIRED);
        }
        Index index;
        try {
            index = ParserUtil.parseIndex(indexText);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_INDEX, e);
        }
        if (values.isEmpty()) {
            throw new ParseException(MESSAGE_NO_PARAMETERS);
        }
        InfoDescriptor descriptor = new InfoDescriptor();
        if (values.containsKey(PREFIX_FINANCIAL_NEED.getPrefix())) {
            descriptor.setFinancialNeed(ParserUtil.parseFinancialNeed(values.get(PREFIX_FINANCIAL_NEED.getPrefix())));
        }
        if (values.containsKey(PREFIX_PRIORITY.getPrefix())) {
            descriptor.setPriority(ParserUtil.parsePriority(values.get(PREFIX_PRIORITY.getPrefix())));
        }
        if (values.containsKey(PREFIX_AREA_OF_INTEREST.getPrefix())) {
            descriptor.setAreaOfInterest(
                    ParserUtil.parseAreaOfInterest(values.get(PREFIX_AREA_OF_INTEREST.getPrefix())));
        }
        return new InfoCommand(index, descriptor);
    }
}
