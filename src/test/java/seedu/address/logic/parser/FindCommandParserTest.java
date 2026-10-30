package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAIL_KEYWORD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonMatchesFiltersPredicate;
import seedu.address.model.person.Relationship;
import seedu.address.model.tag.Tag;

/** Contains parsing tests for supported find filters and their combinations. */
public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void usageMessage_containsOnlyParameters() {
        assertEquals("Parameters: [KEYWORD [MORE_KEYWORDS]...] [r/RELATIONSHIP] "
                + "[t/TAG [MORE_TAGS...]] [k/DETAIL_KEYWORD [MORE_KEYWORDS...]]", FindCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new PersonMatchesFiltersPredicate(List.of("Alice", "Bob"), Optional.empty(),
                        List.of(), List.of()));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_validPunctuatedNameKeywords_returnsFindCommand() {
        assertParseSuccess(parser, "Anne-Marie", findCommandWithNameKeywords("Anne-Marie"));
        assertParseSuccess(parser, "O'Connor", findCommandWithNameKeywords("O'Connor"));
        assertParseSuccess(parser, "A. P. J. Abdul Kalam",
                findCommandWithNameKeywords("A.", "P.", "J.", "Abdul", "Kalam"));
        assertParseSuccess(parser, "Martin Luther King, Jr.",
                findCommandWithNameKeywords("Martin", "Luther", "King,", "Jr."));
        assertParseSuccess(parser, "Jack s/o David", findCommandWithNameKeywords("Jack", "s/o", "David"));
        assertParseSuccess(parser, "s/o", findCommandWithNameKeywords("s/o"));
        assertParseSuccess(parser, "Jack s/o", findCommandWithNameKeywords("Jack", "s/o"));
        assertParseSuccess(parser, "s/o David", findCommandWithNameKeywords("s/o", "David"));
        assertParseSuccess(parser, "D/O w/O", findCommandWithNameKeywords("D/O", "w/O"));
    }

    @Test
    public void parse_invalidNameKeywords_throwsParseException() {
        assertParseFailure(parser, "1", Name.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "Alice1", Name.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "Alice Bob2 r/client", Name.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "Alice@", Name.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "John/Smith", Name.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "s/x", Name.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "Anne-", Name.MESSAGE_INVALID_CHARACTERS);
    }

    @Test
    public void parse_validRelationship_returnsFindCommand() {
        FindCommand expectedClientFindCommand =
                new FindCommand(new PersonMatchesFiltersPredicate(List.of(), Optional.of(Relationship.CLIENT),
                        List.of(), List.of()));
        FindCommand expectedProspectFindCommand =
                new FindCommand(new PersonMatchesFiltersPredicate(List.of(), Optional.of(Relationship.PROSPECT),
                        List.of(), List.of()));

        assertParseSuccess(parser, "r/client", expectedClientFindCommand);
        assertParseSuccess(parser, " r/Client ", expectedClientFindCommand);
        assertParseSuccess(parser, " r/CLIENT ", expectedClientFindCommand);
        assertParseSuccess(parser, "r/prospect", expectedProspectFindCommand);
        assertParseSuccess(parser, " r/Prospect ", expectedProspectFindCommand);
    }

    @Test
    public void parse_invalidRelationship_throwsParseException() {
        assertParseFailure(parser, " r/customer", Relationship.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "Alice r/customer", Relationship.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " r/", Relationship.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedRelationship_throwsParseException() {
        assertParseFailure(parser, " r/client r/prospect",
                getErrorMessageForDuplicatePrefixes(PREFIX_RELATIONSHIP));
    }

    @Test
    public void parse_tags_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("friends", "priority"),
                        List.of()));

        assertParseSuccess(parser, "t/priority friends priority", expectedFindCommand);
        assertParseSuccess(parser, " t/friends priority ", expectedFindCommand);
    }

    @Test
    public void parse_invalidOrRepeatedTagFilter_throwsParseException() {
        assertParseFailure(parser, "t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/close-friend", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/friends t/priority", getErrorMessageForDuplicatePrefixes(PREFIX_TAG));
    }

    @Test
    public void parse_detailKeywords_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of(),
                        List.of("retirement", "WhatsApp", "2026-10-06", "A/B")));

        assertParseSuccess(parser, "k/retirement WhatsApp 2026-10-06 A/B", expectedFindCommand);
        assertParseSuccess(parser, " k/retirement   WhatsApp  2026-10-06 A/B ", expectedFindCommand);
    }

    @Test
    public void parse_emptyOrRepeatedDetailFilter_throwsParseException() {
        assertParseFailure(parser, "k/",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "k/retirement k/WhatsApp",
                getErrorMessageForDuplicatePrefixes(PREFIX_DETAIL_KEYWORD));
    }

    @Test
    public void parse_nameAndRelationship_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new PersonMatchesFiltersPredicate(List.of("Alice", "Bob"), Optional.of(Relationship.CLIENT),
                        List.of(), List.of()));

        assertParseSuccess(parser, "Alice Bob r/client", expectedFindCommand);
        assertParseSuccess(parser, " \n Alice \t Bob  r/CLIENT ", expectedFindCommand);
    }

    @Test
    public void parse_allFilters_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new PersonMatchesFiltersPredicate(List.of("Alice", "Bob"), Optional.of(Relationship.CLIENT),
                        List.of("friends", "priority"), List.of("retirement", "WhatsApp")));

        assertParseSuccess(parser, "Alice Bob r/client t/priority friends k/retirement WhatsApp",
                expectedFindCommand);
        assertParseSuccess(parser, "Alice Bob k/retirement WhatsApp t/friends priority r/client",
                expectedFindCommand);
    }

    /** Creates a find command containing only the supplied name keywords. */
    private FindCommand findCommandWithNameKeywords(String... nameKeywords) {
        return new FindCommand(new PersonMatchesFiltersPredicate(List.of(nameKeywords), Optional.empty(), List.of(),
                List.of()));
    }

}
