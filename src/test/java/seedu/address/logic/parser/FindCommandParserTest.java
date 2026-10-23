package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.PersonMatchesFiltersPredicate;
import seedu.address.model.person.Relationship;
import seedu.address.model.tag.Tag;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new PersonMatchesFiltersPredicate(List.of("Alice", "Bob"), Optional.empty(),
                        List.of()));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_validRelationship_returnsFindCommand() {
        FindCommand expectedClientFindCommand =
                new FindCommand(new PersonMatchesFiltersPredicate(List.of(), Optional.of(Relationship.CLIENT),
                        List.of()));
        FindCommand expectedProspectFindCommand =
                new FindCommand(new PersonMatchesFiltersPredicate(List.of(), Optional.of(Relationship.PROSPECT),
                        List.of()));

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
                new PersonMatchesFiltersPredicate(List.of(), Optional.empty(), List.of("friends", "priority")));

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
    public void parse_nameAndRelationship_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new PersonMatchesFiltersPredicate(List.of("Alice", "Bob"), Optional.of(Relationship.CLIENT),
                        List.of()));

        assertParseSuccess(parser, "Alice Bob r/client", expectedFindCommand);
        assertParseSuccess(parser, " \n Alice \t Bob  r/CLIENT ", expectedFindCommand);
    }

    @Test
    public void parse_allFilters_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new PersonMatchesFiltersPredicate(List.of("Alice", "Bob"), Optional.of(Relationship.CLIENT),
                        List.of("friends", "priority")));

        assertParseSuccess(parser, "Alice Bob r/client t/priority friends", expectedFindCommand);
    }

}
