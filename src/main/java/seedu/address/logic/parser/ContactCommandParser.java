package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT_CHANNEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT_TIMING;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ContactCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PreferredContactChannel;
import seedu.address.model.person.PreferredContactTiming;

/** Parses a displayed index and one or both contact preferences. */
public class ContactCommandParser implements Parser<ContactCommand> {
    @Override
    public ContactCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + args,
                PREFIX_CONTACT_CHANNEL, PREFIX_CONTACT_TIMING);
        Index index = ParserUtil.parseIndex(fields.getPreamble());
        fields.verifyNoDuplicatePrefixesFor(PREFIX_CONTACT_CHANNEL, PREFIX_CONTACT_TIMING);
        if (fields.getValue(PREFIX_CONTACT_CHANNEL).isEmpty() && fields.getValue(PREFIX_CONTACT_TIMING).isEmpty()) {
            throw new ParseException(ContactCommand.MESSAGE_NO_FIELDS);
        }
        try {
            PreferredContactChannel channel = fields.getValue(PREFIX_CONTACT_CHANNEL)
                    .map(PreferredContactChannel::new).orElse(null);
            PreferredContactTiming timing = fields.getValue(PREFIX_CONTACT_TIMING)
                    .map(PreferredContactTiming::new).orElse(null);
            return new ContactCommand(index, channel, timing);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
