package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.InfoCommand;
import seedu.address.logic.commands.InfoCommand.InfoDescriptor;
import seedu.address.model.person.AreaOfInterest;
import seedu.address.model.person.FinancialNeed;
import seedu.address.model.person.Priority;

public class InfoCommandParserTest {
    private final InfoCommandParser parser = new InfoCommandParser();

    @Test
    public void parse_individualFields_success() {
        InfoDescriptor need = new InfoDescriptor();
        need.setFinancialNeed(new FinancialNeed("Retirement"));
        assertParseSuccess(parser, "1 /fn Retirement", new InfoCommand(INDEX_FIRST_PERSON, need));
        InfoDescriptor priority = new InfoDescriptor();
        priority.setPriority(new Priority("High"));
        assertParseSuccess(parser, "1 /pr High", new InfoCommand(INDEX_FIRST_PERSON, priority));
        InfoDescriptor interest = new InfoDescriptor();
        interest.setAreaOfInterest(new AreaOfInterest("Insurance"));
        assertParseSuccess(parser, "1 /ai Insurance", new InfoCommand(INDEX_FIRST_PERSON, interest));
    }

    @Test
    public void parse_multipleFieldsAnyOrder_success() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Long  term"));
        descriptor.setPriority(new Priority("High"));
        descriptor.setAreaOfInterest(new AreaOfInterest("Insurance"));
        for (String fields : new String[] {
            "/fn Long  term /pr High /ai Insurance", "/fn Long  term /ai Insurance /pr High",
            "/pr High /fn Long  term /ai Insurance", "/pr High /ai Insurance /fn Long  term",
            "/ai Insurance /fn Long  term /pr High", "/ai Insurance /pr High /fn Long  term"
        }) {
            assertParseSuccess(parser, "1 " + fields, new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_missingOrInvalidIndex_failure() {
        for (String args : new String[] {"", "  ", "/fn Retirement"}) {
            assertParseFailure(parser, args, "Client index is required");
        }
        for (String index : new String[] {"0", "-1", "abc", "1.5", "2147483648", "1 2"}) {
            assertParseFailure(parser, index + " /fn Retirement", "Client index must be a positive integer.");
        }
        assertParseFailure(parser, "1", "At least one of /fn, /pr or /ai is required.");
    }

    @Test
    public void parse_duplicateParameters_failure() {
        for (String prefix : new String[] {"/fn", "/pr", "/ai"}) {
            assertParseFailure(parser, "1 " + prefix + " A " + prefix + " B",
                    prefix + " can only be specified once.");
        }
    }

    @Test
    public void parse_unknownParameters_failure() {
        for (String args : new String[] {"1 /xyz test", "1 /fn retirement /xyz test",
            "1 /fnextra test", "1 /pr High /ai Insurance /unknown value", "1 /FN value"}) {
            assertParseFailure(parser, args, "Invalid parameter. Valid parameters are /fn, /pr and /ai.");
        }
    }

    @Test
    public void parse_invalidFinancialNeed_exactMessages() {
        assertParseFailure(parser, "1 /fn ", FinancialNeed.MESSAGE_BLANK);
        assertParseFailure(parser, "1 /fn " + "A".repeat(201), FinancialNeed.MESSAGE_LENGTH);
        for (String value : new String[] {"Bad!", "Needs/Wants", "\tRetirement", "Retirement\n", "Retirement\r"}) {
            assertParseFailure(parser, "1 /fn " + value, FinancialNeed.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void parse_invalidPriority_exactMessages() {
        assertParseFailure(parser, "1 /pr ", Priority.MESSAGE_BLANK);
        assertParseFailure(parser, "1 /pr " + "A".repeat(201), Priority.MESSAGE_LENGTH);
        for (String value : new String[] {"Bad!", "Needs/Wants", "\tRetirement", "Retirement\n", "Retirement\r"}) {
            assertParseFailure(parser, "1 /pr " + value, Priority.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void parse_invalidAreaOfInterest_exactMessages() {
        assertParseFailure(parser, "1 /ai ", AreaOfInterest.MESSAGE_BLANK);
        assertParseFailure(parser, "1 /ai " + "A".repeat(201), AreaOfInterest.MESSAGE_LENGTH);
        for (String value : new String[] {"Bad!", "Needs/Wants", "\tRetirement", "Retirement\n", "Retirement\r"}) {
            assertParseFailure(parser, "1 /ai " + value, AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        }
    }
}
