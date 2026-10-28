package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT_CHANNEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT_TIMING;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ContactCommand;
import seedu.address.logic.commands.ContactCommand.ContactDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PreferredContactChannel;
import seedu.address.model.person.PreferredContactTiming;

/** Parses a displayed index and one or both contact preferences. */
public class ContactCommandParser implements Parser<ContactCommand> {
    public static final String MESSAGE_INVALID_PARAMETER =
            "Invalid contact parameter. Use ch/CHANNEL or tm/TIMING; use - to clear a preference.";

    private static final Pattern CONTACT_PREFIX = Pattern.compile("(?<= )(ch/|tm/)");
    private static final Pattern MISTYPED_PREFIX = Pattern.compile(
            "(?<= )(?:/(?:ch|tm)(?=\\s|/|$)|cch/|chh/|ttm/|tmm/)");

    @Override
    public ContactCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = tokenizeRawValues(" " + args);
        Index index = parseContactIndex(fields.getPreamble());
        fields.verifyNoDuplicatePrefixesFor(PREFIX_CONTACT_CHANNEL, PREFIX_CONTACT_TIMING);
        if (MISTYPED_PREFIX.matcher(" " + args).find()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETER);
        }
        if (fields.getValue(PREFIX_CONTACT_CHANNEL).isEmpty() && fields.getValue(PREFIX_CONTACT_TIMING).isEmpty()) {
            throw new ParseException(ContactCommand.MESSAGE_NO_FIELDS);
        }
        try {
            ContactDescriptor descriptor = new ContactDescriptor();
            if (fields.getValue(PREFIX_CONTACT_CHANNEL).isPresent()) {
                String value = fields.getValue(PREFIX_CONTACT_CHANNEL).orElseThrow();
                descriptor.setChannel(isClearMarker(value) ? null : new PreferredContactChannel(value.trim()));
            }
            if (fields.getValue(PREFIX_CONTACT_TIMING).isPresent()) {
                String value = fields.getValue(PREFIX_CONTACT_TIMING).orElseThrow();
                descriptor.setTiming(isClearMarker(value) ? null : new PreferredContactTiming(value.trim()));
            }
            return new ContactCommand(index, descriptor);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /** Validates the index before reporting a recognized parameter mistake in the preamble. */
    private static Index parseContactIndex(String preamble) throws ParseException {
        String text = " " + preamble;
        Matcher matcher = MISTYPED_PREFIX.matcher(text);
        return ParserUtil.parseIndex(matcher.find() ? text.substring(0, matcher.start()) : text);
    }

    /** Retains raw values for marker recognition, using the shared tokenizer's prefix boundaries. */
    private static ArgumentMultimap tokenizeRawValues(String args) {
        ArgumentMultimap fields = new ArgumentMultimap();
        Matcher matcher = CONTACT_PREFIX.matcher(args);
        Prefix previous = new Prefix("");
        int valueStart = 0;
        while (matcher.find()) {
            String value = args.substring(valueStart, matcher.start());
            fields.put(previous, previous.getPrefix().isEmpty() ? value.trim() : value);
            previous = new Prefix(matcher.group());
            valueStart = matcher.end();
        }
        String value = args.substring(valueStart);
        fields.put(previous, previous.getPrefix().isEmpty() ? value.trim() : value);
        return fields;
    }

    /** Only ordinary spaces may surround the exact clearing marker. */
    private static boolean isClearMarker(String value) {
        return value.matches(" *- *");
    }
}
