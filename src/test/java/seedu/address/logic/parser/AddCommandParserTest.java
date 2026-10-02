package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
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
    public void parse_missingOrRepeatedPrefix_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " n/John Doe e/john.doe@example.com p/91234567", expectedMessage);
        assertParseFailure(parser, " r/Client n/John Doe e/john.doe@example.com p/91234567 r/Prospect",
                "Multiple values specified for the following single-valued field(s): t/");
    }

    @Test
    public void parse_invalidType_failure() {
        assertParseFailure(parser, " t/Partner n/John Doe e/john.doe@example.com p/91234567",
                Relationship.MESSAGE_CONSTRAINTS);
    }
}
