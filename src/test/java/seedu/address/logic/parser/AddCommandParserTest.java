package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Relationship;

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
    public void parse_missingPrefix_failure() {
        assertParseFailure(parser, "", AddCommand.MESSAGE_ALL_PARAMETERS_REQUIRED);
        assertParseFailure(parser, " n/John Doe e/john.doe@example.com p/91234567",
                AddCommand.MESSAGE_RELATIONSHIP_REQUIRED);
        assertParseFailure(parser, " r/Client e/john.doe@example.com p/91234567",
                AddCommand.MESSAGE_NAME_REQUIRED);
        assertParseFailure(parser, " r/Client n/John Doe p/91234567",
                AddCommand.MESSAGE_EMAIL_REQUIRED);
        assertParseFailure(parser, " r/Client n/John Doe e/john.doe@example.com",
                AddCommand.MESSAGE_PHONE_REQUIRED);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, " r/Client n/John Doe e/john.doe@example.com p/91234567 r/Prospect",
                AddCommand.MESSAGE_RELATIONSHIP_DUPLICATED);
        assertParseFailure(parser, " r/Client n/John Doe n/Jane Doe e/john.doe@example.com p/91234567",
                AddCommand.MESSAGE_NAME_DUPLICATED);
        assertParseFailure(parser,
                " r/Client n/John Doe e/john.doe@example.com e/jane.doe@example.com p/91234567",
                AddCommand.MESSAGE_EMAIL_DUPLICATED);
        assertParseFailure(parser,
                " r/Client n/John Doe e/john.doe@example.com p/91234567 p/98765432",
                AddCommand.MESSAGE_PHONE_DUPLICATED);
    }

    @Test
    public void parse_invalidRelationship_failure() {
        assertParseFailure(parser, " r/Partner n/John Doe e/john.doe@example.com p/91234567",
                Relationship.MESSAGE_CONSTRAINTS);
    }
}
