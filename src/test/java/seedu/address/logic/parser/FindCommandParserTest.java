package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RELATIONSHIP;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Relationship;
import seedu.address.model.person.RelationshipMatchesPredicate;

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
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_validRelationship_returnsFindCommand() {
        FindCommand expectedClientFindCommand =
                new FindCommand(new RelationshipMatchesPredicate(Relationship.CLIENT));
        FindCommand expectedProspectFindCommand =
                new FindCommand(new RelationshipMatchesPredicate(Relationship.PROSPECT));

        assertParseSuccess(parser, "r/client", expectedClientFindCommand);
        assertParseSuccess(parser, " r/Client ", expectedClientFindCommand);
        assertParseSuccess(parser, " r/CLIENT ", expectedClientFindCommand);
        assertParseSuccess(parser, "r/prospect", expectedProspectFindCommand);
        assertParseSuccess(parser, " r/Prospect ", expectedProspectFindCommand);
    }

    @Test
    public void parse_invalidRelationship_throwsParseException() {
        assertParseFailure(parser, " r/customer", Relationship.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " r/", Relationship.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedRelationship_throwsParseException() {
        assertParseFailure(parser, " r/client r/prospect",
                getErrorMessageForDuplicatePrefixes(PREFIX_RELATIONSHIP));
    }

    @Test
    public void parse_nameAndRelationship_throwsParseException() {
        assertParseFailure(parser, "Alice r/client",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

}
