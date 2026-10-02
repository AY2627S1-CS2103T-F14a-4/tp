package seedu.address.logic.parser;

import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Name;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {
    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_namePhoneAndEmail_success() {
        String userInput = INDEX_FIRST_PERSON.getOneBased() + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY).build();

        assertParseSuccess(parser, userInput, new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_missingOrInvalidField_failure() {
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "1" + PHONE_DESC_AMY + PHONE_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_PHONE));
    }

    @Test
    public void parse_tagPrefix_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();

        assertParseSuccess(parser, INDEX_FIRST_PERSON.getOneBased() + TAG_DESC_FRIEND,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }
}
