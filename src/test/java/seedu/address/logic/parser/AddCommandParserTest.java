package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Relationship;
import seedu.address.model.tag.Tag;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allRequiredFieldsPresent_success() {
        Person expectedPerson = new Person(new Name("John Doe"), new Phone("91234567"),
                new Email("john.doe@example.com"), Relationship.CLIENT);

        assertParseSuccess(parser, " r/Client n/John Doe e/john.doe@example.com p/91234567",
                new AddCommand(expectedPerson));
        assertParseSuccess(parser, " n/John Doe p/91234567 r/Client e/john.doe@example.com",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_optionalTagsPresent_success() {
        Person expectedPerson = new Person(new Name("John Doe"), new Phone("91234567"),
                new Email("john.doe@example.com"), Set.of(new Tag("priority"), new Tag("referral")),
                Relationship.CLIENT);

        assertParseSuccess(parser,
                " r/Client n/John Doe e/john.doe@example.com p/91234567 t/priority t/referral t/priority",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_invalidOrEmptyTag_failure() {
        assertParseFailure(parser,
                " r/Client n/John Doe e/john.doe@example.com p/91234567 t/close-friend",
                Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser,
                " r/Client n/John Doe e/john.doe@example.com p/91234567 t/",
                Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_missingPrefix_failure() {
        String expectedMessage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " n/John Doe e/john.doe@example.com p/91234567",
                expectedMessage);
        assertParseFailure(parser, " r/Client e/john.doe@example.com p/91234567",
                expectedMessage);
        assertParseFailure(parser, " r/Client n/John Doe p/91234567",
                expectedMessage);
        assertParseFailure(parser, " r/Client n/John Doe e/john.doe@example.com",
                expectedMessage);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, " r/Client n/John Doe e/john.doe@example.com p/91234567 r/Prospect",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_RELATIONSHIP));
        assertParseFailure(parser, " r/Client n/John Doe n/Jane Doe e/john.doe@example.com p/91234567",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME));
        assertParseFailure(parser,
                " r/Client n/John Doe e/john.doe@example.com e/jane.doe@example.com p/91234567",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_EMAIL));
        assertParseFailure(parser,
                " r/Client n/John Doe e/john.doe@example.com p/91234567 p/98765432",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_PHONE));
        assertParseFailure(parser, " r/Client r/Prospect n/John Doe n/Jane Doe e/john.doe@example.com p/91234567",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_RELATIONSHIP, CliSyntax.PREFIX_NAME));
    }

    @Test
    public void parse_invalidRelationship_failure() {
        assertParseFailure(parser, " r/Partner n/John Doe e/john.doe@example.com p/91234567",
                Relationship.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_domainLabelExceeds63Characters_failure() {
        String email = "john@" + "a".repeat(64) + ".com";

        assertParseFailure(parser, " r/Client n/John Doe e/" + email + " p/91234567",
                Email.MESSAGE_DOMAIN_LABEL_LENGTH);
    }
}
