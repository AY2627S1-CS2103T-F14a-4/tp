package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ContactCommand;
import seedu.address.model.person.PreferredContactChannel;
import seedu.address.model.person.PreferredContactTiming;

public class ContactCommandParserTest {
    private final ContactCommandParser parser = new ContactCommandParser();

    @Test
    public void parse_fieldsInEitherOrder_success() throws Exception {
        ContactCommand expected = new ContactCommand(INDEX_FIRST_PERSON,
                new PreferredContactChannel("Phone  call"), new PreferredContactTiming("After 6:30 pm"));
        assertParseSuccess(parser, " 1 ch/ Phone  call  tm/ After 6:30 pm ", expected);
        assertParseSuccess(parser, " 1 tm/After 6:30 pm ch/Phone  call", expected);
        assertEquals(expected, new ClientBookParser().parseCommand("contact 1 ch/Phone  call tm/After 6:30 pm"));
        assertParseSuccess(parser, "1 ch/Email", new ContactCommand(INDEX_FIRST_PERSON,
                new PreferredContactChannel("Email"), null));
        assertParseSuccess(parser, "1 tm/Weekends", new ContactCommand(INDEX_FIRST_PERSON,
                null, new PreferredContactTiming("Weekends")));
    }

    @Test
    public void parse_slashesInFreeText_preservesValues() throws Exception {
        ContactCommand expected = new ContactCommand(INDEX_FIRST_PERSON,
                new PreferredContactChannel("Email or Phone/WhatsApp"),
                new PreferredContactTiming("Weekdays AM/PM"));
        assertParseSuccess(parser, " 1 ch/Email or Phone/WhatsApp tm/Weekdays AM/PM", expected);
        assertParseSuccess(parser, " 1 tm/Weekdays AM/PM ch/Email or Phone/WhatsApp", expected);
        assertEquals(expected, new ClientBookParser().parseCommand(
                "contact 1 ch/Email or Phone/WhatsApp tm/Weekdays AM/PM"));
        assertParseSuccess(parser, " 1 ch/Email xx/After 6 pm", new ContactCommand(INDEX_FIRST_PERSON,
                new PreferredContactChannel("Email xx/After 6 pm"), null));
        assertParseSuccess(parser, " 1 tm/Weekdays AM/PM", new ContactCommand(INDEX_FIRST_PERSON,
                null, new PreferredContactTiming("Weekdays AM/PM")));
    }

    @Test
    public void parse_valueBoundaries_success() {
        for (int length : new int[] {1, 199, 200}) {
            String value = "a".repeat(length);
            assertParseSuccess(parser, " 1 ch/ " + value + " tm/ " + value + " ",
                    new ContactCommand(INDEX_FIRST_PERSON,
                            new PreferredContactChannel(value), new PreferredContactTiming(value)));
            assertParseSuccess(parser, "1 ch/\u0085\u00a0\t" + value + "\u2007 tm/\u202f" + value + "\u0085 ",
                    new ContactCommand(INDEX_FIRST_PERSON,
                            new PreferredContactChannel(value), new PreferredContactTiming(value)));
        }
    }

    @Test
    public void parse_unicodeAndPunctuation_preservesInternalText() {
        String channel = "\u5ba2\u6237: Phone/WhatsApp + Email\u00a0or\u2007phone!\u0085Call";
        String timing = "Weekdays 6:30\u20138 pm\u202f(\u665a\u4e0a)\u0085Weekends";
        ContactCommand expected = new ContactCommand(INDEX_FIRST_PERSON,
                new PreferredContactChannel(channel), new PreferredContactTiming(timing));
        assertParseSuccess(parser, "1 ch/\u00a0" + channel + "\u2007 tm/\u202f" + timing + "\u00a0", expected);
        assertParseSuccess(parser, "1 tm/" + timing + " ch/" + channel, expected);
    }

    @Test
    public void parse_blankOrLongValues_failure() {
        for (String value : List.of("", "  ", "\t", "\r\n", "\u2003", "a".repeat(201),
                "\u00a0" + "a".repeat(201) + "\u202f")) {
            assertParseFailure(parser, " 1 ch/" + value, PreferredContactChannel.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, " 1 tm/" + value, PreferredContactTiming.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, " 1 ch/Email tm/" + value, PreferredContactTiming.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, " 1 ch/" + value + " tm/After 6 pm",
                    PreferredContactChannel.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_nonBreakingWhitespaceOnlyValues_failure() {
        for (String value : List.of("\u00a0", "\u2007", "\u202f", " \t\u2003\u00a0\u2007\u202f\n ")) {
            assertParseFailure(parser, "1 ch/" + value, PreferredContactChannel.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 tm/" + value, PreferredContactTiming.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 ch/Email tm/" + value, PreferredContactTiming.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 tm/Weekends ch/" + value, PreferredContactChannel.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_nextLineWhitespaceOnlyValues_failure() {
        for (String value : List.of("\u0085", " \t\u00a0\u0085\u001c ")) {
            assertParseFailure(parser, "1 ch/" + value, PreferredContactChannel.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 tm/" + value, PreferredContactTiming.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 ch/Email tm/" + value, PreferredContactTiming.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 tm/Weekends ch/" + value, PreferredContactChannel.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_missingOrInvalidIndex_failure() {
        for (String index : List.of("", "0", "-1", "abc", "1.5", "1 2", "2147483648")) {
            assertParseFailure(parser, index + " ch/Email", ParserUtil.MESSAGE_INVALID_INDEX);
        }
        assertParseFailure(parser, "", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_missingFieldsOrMalformedPrefixes_failure() {
        assertParseFailure(parser, " 1", ContactCommand.MESSAGE_NO_FIELDS);
        for (String input : List.of(" 1 xx/Email", " 1 /ch Email", " 1 chEmail", " 1 tm Evenings ch/Email")) {
            assertParseFailure(parser, input, ParserUtil.MESSAGE_INVALID_INDEX);
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        for (String values : List.of(" ch/A ch/B", " ch/ ch/B", " ch/A tm/B ch/C")) {
            assertParseFailure(parser, " 1" + values,
                    Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_CONTACT_CHANNEL));
        }
        assertParseFailure(parser, " 1 tm/A ch/B tm/C",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_CONTACT_TIMING));
    }
}
