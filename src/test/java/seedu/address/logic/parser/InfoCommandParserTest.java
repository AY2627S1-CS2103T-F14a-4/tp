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
    public void parse_oldSyntax_helpfulParameterError() {
        for (String fields : new String[] {"/fn Retirement", "/pr High", "/ai Insurance",
            "/fn", "/pr -", "/ai Insurance /pr High", "fn/Retirement /pr High"}) {
            for (String index : new String[] {"1", "2", "2147483647"}) {
                assertParseFailure(parser, index + " " + fields, InfoCommandParser.MESSAGE_INVALID_PARAMETER);
            }
        }
    }

    @Test
    public void parse_prefixLikeTokens_reservedForParameters() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Review"));
        descriptor.setPriority(new Priority("High options"));
        assertParseSuccess(parser, "1 fn/Review pr/High options", new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseFailure(parser, "1 fn/Review fn/Retirement",
                String.format(InfoCommandParser.MESSAGE_DUPLICATE_PARAMETER, "fn/"));
        for (String token : new String[] {"fnn/Value", "prr/High", "aii/Insurance",
            "FN/Value", "Pr/High", "AI/Insurance"}) {
            assertParseFailure(parser, "1 " + token, InfoCommandParser.MESSAGE_INVALID_PARAMETER);
            assertParseFailure(parser, "1 fn/Review " + token, InfoCommandParser.MESSAGE_INVALID_PARAMETER);
        }
    }

    @Test
    public void parse_spacesAroundOrdinaryValues_success() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Retirement planning"));
        descriptor.setPriority(new Priority("High"));
        descriptor.setAreaOfInterest(null);
        assertParseSuccess(parser, "  1 fn/   Retirement planning   pr/  High   ai/   -   ",
                new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseFailure(parser, "1fn/Retirement", InfoCommandParser.MESSAGE_INVALID_INDEX);
        InfoDescriptor slashValue = new InfoDescriptor();
        slashValue.setFinancialNeed(new FinancialNeed("CPF/SRS"));
        assertParseSuccess(parser, "1 fn/ CPF/SRS", new InfoCommand(INDEX_FIRST_PERSON, slashValue));
    }

    @Test
    public void parse_attachedSlashes_preservesValueText() {
        for (String value : new String[] {"CPF/SRS", "Life/Health insurance", "Investment/Insurance",
            "pr/High", "fn/Retirement", "ai/Insurance", "Review/pr/High options", "CPF/SRSpr/High"}) {
            InfoDescriptor descriptor = new InfoDescriptor();
            descriptor.setFinancialNeed(new FinancialNeed(value));
            descriptor.setPriority(new Priority(value));
            descriptor.setAreaOfInterest(new AreaOfInterest(value));
            assertParseSuccess(parser, "1 fn/" + value + " pr/" + value + " ai/" + value,
                    new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_arbitrarySlashWordsInsideValues_preservesAmbiguousText() {
        for (String value : new String[] {"Review xyz/Value", "Review fna/Value", "Review fnValue/Value",
            "Planning CPF/SRS and Life/Health insurance", "Save $3000/month or $40000/year"}) {
            InfoDescriptor descriptor = new InfoDescriptor();
            descriptor.setFinancialNeed(new FinancialNeed(value));
            descriptor.setPriority(new Priority("High"));
            descriptor.setAreaOfInterest(null);
            assertParseSuccess(parser, "1 fn/" + value + " ai/- pr/High",
                    new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_unknownPrefixBeforeFirstField_rejectsWithoutHidingInvalidIndex() {
        for (String prefix : new String[] {"xyz/Value", "fna/Value", "fnValue/Value", "CPF/SRS"}) {
            assertParseFailure(parser, "2 " + prefix, InfoCommandParser.MESSAGE_INVALID_PARAMETER);
            assertParseFailure(parser, "2 " + prefix + " pr/High", InfoCommandParser.MESSAGE_INVALID_PARAMETER);
            for (String index : new String[] {"0", "-1", "abc", "1.5", "2147483648", "1 2"}) {
                assertParseFailure(parser, index + " " + prefix + " pr/High",
                        InfoCommandParser.MESSAGE_INVALID_INDEX);
            }
        }
    }

    @Test
    public void parse_financialNotationInEachField_success() {
        for (String value : new String[] {"Retire on $3000 monthly", "Target 4% return", "CPF + SRS",
            "Life/Health insurance", "Target 6% return", "CPF/SRS planning", "Planning CPF/SRS",
            "Save $3000/month", "Planning Life/Health insurance", "e\u0301pargne $3000 + CPF/SRS 4%",
            "\u00e9pargne", "e\u0301pargne",
            "\u0b93\u0baf\u0bcd\u0bb5\u0bc2\u0ba4\u0bbf\u0baf\u0bae\u0bcd"}) {
            InfoDescriptor need = new InfoDescriptor();
            need.setFinancialNeed(new FinancialNeed(value));
            assertParseSuccess(parser, "1 fn/" + value + "  ", new InfoCommand(INDEX_FIRST_PERSON, need));
            InfoDescriptor priority = new InfoDescriptor();
            priority.setPriority(new Priority(value));
            assertParseSuccess(parser, "1 pr/" + value + "  ", new InfoCommand(INDEX_FIRST_PERSON, priority));
            InfoDescriptor interest = new InfoDescriptor();
            interest.setAreaOfInterest(new AreaOfInterest(value));
            assertParseSuccess(parser, "1 ai/" + value + "  ", new InfoCommand(INDEX_FIRST_PERSON, interest));
        }
    }

    @Test
    public void parse_financialNotationAnyFieldOrderAndClearing_success() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Retire on $3000 monthly"));
        descriptor.setPriority(new Priority("Target 6% return"));
        descriptor.setAreaOfInterest(new AreaOfInterest("CPF/SRS + insurance"));
        String need = "fn/Retire on $3000 monthly";
        String priority = "pr/Target 6% return";
        String interest = "ai/CPF/SRS + insurance";
        for (String fields : new String[] {need + " " + priority + " " + interest,
            need + " " + interest + " " + priority, priority + " " + need + " " + interest,
            priority + " " + interest + " " + need, interest + " " + need + " " + priority,
            interest + " " + priority + " " + need}) {
            assertParseSuccess(parser, "1 " + fields, new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        }
        descriptor.setPriority(null);
        assertParseSuccess(parser, "1 " + interest + " pr/  -   " + need,
                new InfoCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_financialSymbolBoundariesAndMixedInvalidFields_preservesValidation() {
        for (String symbol : new String[] {"$", "%", "+", "/"}) {
            String value = "A" + symbol.repeat(199);
            InfoDescriptor descriptor = new InfoDescriptor();
            descriptor.setFinancialNeed(new FinancialNeed(value));
            descriptor.setPriority(new Priority(value));
            descriptor.setAreaOfInterest(new AreaOfInterest(value));
            assertParseSuccess(parser, "1 fn/" + value + " pr/" + value + " ai/" + value,
                    new InfoCommand(INDEX_FIRST_PERSON, descriptor));
            assertParseFailure(parser, "1 ai/" + value + symbol + " pr/" + value + symbol
                    + " fn/" + value + symbol, FinancialNeed.MESSAGE_LENGTH + "\n"
                            + Priority.MESSAGE_LENGTH + "\n" + AreaOfInterest.MESSAGE_LENGTH);
        }
        assertParseFailure(parser, "1 ai/Bad! pr/ fn/Retire on $3000 monthly",
                Priority.MESSAGE_BLANK + "\n" + AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "1 ai/CPF/SRS pr/Bad! fn/-", Priority.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "1 fn/CPF + SRS ai/Plan\t", AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "1 pr/Target 6% return fn/Bad!", FinancialNeed.MESSAGE_INVALID_CHARACTERS);
    }

    @Test
    public void parse_financialNotationWithMalformedOrDuplicateParameters_failure() {
        for (String prefix : new String[] {"fnn/", "prr/", "aii/", "FN/", "PR/", "AI/"}) {
            assertParseFailure(parser, "1 " + prefix + " value", InfoCommandParser.MESSAGE_INVALID_PARAMETER);
            assertParseFailure(parser, "1 fn/CPF/SRS " + prefix + " value",
                    InfoCommandParser.MESSAGE_INVALID_PARAMETER);
        }
        for (String value : new String[] {"Life /Health", "Life / Health", "Life /", "/", "/ Health"}) {
            for (String prefix : new String[] {"fn/", "pr/", "ai/"}) {
                assertParseFailure(parser, "1 " + prefix + " " + value,
                        InfoCommandParser.MESSAGE_INVALID_PARAMETER);
            }
        }
        for (String prefix : new String[] {"fn/", "pr/", "ai/"}) {
            assertParseFailure(parser, "1 " + prefix + "CPF/SRS " + prefix + " $3000 + 4%",
                    String.format(InfoCommandParser.MESSAGE_DUPLICATE_PARAMETER, prefix));
        }
    }

    @Test
    public void parse_clearIndividualFieldsWithSpaces_success() {
        InfoDescriptor need = new InfoDescriptor();
        need.setFinancialNeed(null);
        InfoDescriptor priority = new InfoDescriptor();
        priority.setPriority(null);
        InfoDescriptor interest = new InfoDescriptor();
        interest.setAreaOfInterest(null);
        for (String marker : new String[] {"-", "   -    "}) {
            assertParseSuccess(parser, "1 fn/" + marker, new InfoCommand(INDEX_FIRST_PERSON, need));
            assertParseSuccess(parser, "1 pr/" + marker, new InfoCommand(INDEX_FIRST_PERSON, priority));
            assertParseSuccess(parser, "1 ai/" + marker, new InfoCommand(INDEX_FIRST_PERSON, interest));
        }
    }

    @Test
    public void parse_clearAndSetFieldsAnyOrder_success() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(null);
        descriptor.setPriority(new Priority("High"));
        descriptor.setAreaOfInterest(null);
        for (String fields : new String[] {"fn/- pr/High ai/-", "ai/- pr/High fn/-",
            "pr/High ai/- fn/-", "fn/- ai/- pr/High", "pr/High fn/- ai/-", "ai/- fn/- pr/High"}) {
            assertParseSuccess(parser, "1 " + fields, new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_hyphenWithinNormalValueAndBoundaries_setsValue() {
        for (String value : new String[] {"Long-term", "-High", "High-", "--", "- -", "A", "A".repeat(200)}) {
            InfoDescriptor descriptor = new InfoDescriptor();
            descriptor.setFinancialNeed(new FinancialNeed(value));
            descriptor.setPriority(new Priority(value));
            descriptor.setAreaOfInterest(new AreaOfInterest(value));
            assertParseSuccess(parser, "1 fn/" + value + " pr/" + value + " ai/" + value,
                    new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_barePrefixesAndControlCharacters_doNotClear() {
        assertParseFailure(parser, "1 fn/", FinancialNeed.MESSAGE_BLANK);
        assertParseFailure(parser, "1 pr/", Priority.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/", AreaOfInterest.MESSAGE_BLANK);
        assertParseFailure(parser, "1 fn/- pr/", Priority.MESSAGE_BLANK);
        assertParseFailure(parser, "1 pr/- ai/", AreaOfInterest.MESSAGE_BLANK);
        for (String marker : new String[] {"\t-", "-\n", "-\r"}) {
            assertParseFailure(parser, "1 fn/" + marker, FinancialNeed.MESSAGE_INVALID_CHARACTERS);
            assertParseFailure(parser, "1 pr/" + marker, Priority.MESSAGE_INVALID_CHARACTERS);
            assertParseFailure(parser, "1 ai/" + marker, AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void parse_individualFields_success() {
        InfoDescriptor need = new InfoDescriptor();
        need.setFinancialNeed(new FinancialNeed("Retirement"));
        assertParseSuccess(parser, "1 fn/Retirement", new InfoCommand(INDEX_FIRST_PERSON, need));
        InfoDescriptor priority = new InfoDescriptor();
        priority.setPriority(new Priority("High"));
        assertParseSuccess(parser, "1 pr/High", new InfoCommand(INDEX_FIRST_PERSON, priority));
        InfoDescriptor interest = new InfoDescriptor();
        interest.setAreaOfInterest(new AreaOfInterest("Insurance"));
        assertParseSuccess(parser, "1 ai/Insurance", new InfoCommand(INDEX_FIRST_PERSON, interest));
    }

    @Test
    public void parse_multipleFieldsAnyOrder_success() {
        InfoDescriptor descriptor = new InfoDescriptor();
        descriptor.setFinancialNeed(new FinancialNeed("Long  term"));
        descriptor.setPriority(new Priority("High"));
        descriptor.setAreaOfInterest(new AreaOfInterest("Insurance"));
        for (String fields : new String[] {
            "fn/Long  term pr/High ai/Insurance", "fn/Long  term ai/Insurance pr/High",
            "pr/High fn/Long  term ai/Insurance", "pr/High ai/Insurance fn/Long  term",
            "ai/Insurance fn/Long  term pr/High", "ai/Insurance pr/High fn/Long  term"
        }) {
            assertParseSuccess(parser, "1 " + fields, new InfoCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_missingOrInvalidIndex_failure() {
        for (String args : new String[] {"", "  ", "fn/Retirement"}) {
            assertParseFailure(parser, args, "Client index is required");
        }
        for (String index : new String[] {"0", "-1", "abc", "1.5", "2147483648", "1 2"}) {
            assertParseFailure(parser, index + " fn/Retirement", "Client index must be a positive integer.");
            assertParseFailure(parser, index + " pr/-", "Client index must be a positive integer.");
        }
        assertParseFailure(parser, "1", InfoCommandParser.MESSAGE_NO_PARAMETERS);
    }

    @Test
    public void parse_duplicateParameters_failure() {
        for (String prefix : new String[] {"fn/", "pr/", "ai/"}) {
            for (String values : new String[] {" - " + prefix + " High", " High " + prefix + " -",
                " - " + prefix + " -", " A " + prefix + " B"}) {
                assertParseFailure(parser, "1 " + prefix + values, prefix + " can only be specified once.");
            }
        }
    }

    @Test
    public void parse_unknownParameters_failure() {
        for (String args : new String[] {"1 /xyz test", "1 fn/retirement /xyz test",
            "1 /fnextra test", "1 pr/High ai/Insurance /unknown value", "1 /FN value"}) {
            assertParseFailure(parser, args, InfoCommandParser.MESSAGE_INVALID_PARAMETER);
        }
    }

    @Test
    public void parse_twoInvalidFields_reportsBothInFieldOrder() {
        String needAndPriority = FinancialNeed.MESSAGE_BLANK + "\n" + Priority.MESSAGE_BLANK;
        for (String fields : new String[] {"fn/ pr/", "pr/ fn/"}) {
            assertParseFailure(parser, "1 " + fields, needAndPriority);
        }
        String needAndInterest = FinancialNeed.MESSAGE_BLANK + "\n" + AreaOfInterest.MESSAGE_BLANK;
        for (String fields : new String[] {"fn/ ai/", "ai/ fn/"}) {
            assertParseFailure(parser, "1 " + fields, needAndInterest);
        }
        String priorityAndInterest = Priority.MESSAGE_BLANK + "\n" + AreaOfInterest.MESSAGE_BLANK;
        for (String fields : new String[] {"pr/ ai/", "ai/ pr/"}) {
            assertParseFailure(parser, "1 " + fields, priorityAndInterest);
        }
    }

    @Test
    public void parse_threeInvalidFieldsAnyOrder_reportsAllInFieldOrder() {
        String expected = FinancialNeed.MESSAGE_BLANK + "\n" + Priority.MESSAGE_BLANK
                + "\n" + AreaOfInterest.MESSAGE_BLANK;
        for (String fields : new String[] {"fn/ pr/ ai/", "fn/ ai/ pr/", "pr/ fn/ ai/",
            "pr/ ai/ fn/", "ai/ fn/ pr/", "ai/ pr/ fn/"}) {
            assertParseFailure(parser, "1 " + fields, expected);
        }
    }

    @Test
    public void parse_mixedValidAndInvalidFields_reportsOnlyInvalidFields() {
        assertParseFailure(parser, "1 ai/ pr/ fn/Retirement",
                Priority.MESSAGE_BLANK + "\n" + AreaOfInterest.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/ pr/High fn/",
                FinancialNeed.MESSAGE_BLANK + "\n" + AreaOfInterest.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/Insurance pr/ fn/",
                FinancialNeed.MESSAGE_BLANK + "\n" + Priority.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/Insurance pr/High fn/", FinancialNeed.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/Insurance pr/ fn/Retirement", Priority.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/ pr/High fn/Retirement", AreaOfInterest.MESSAGE_BLANK);
    }

    @Test
    public void parse_multipleInvalidValueCategories_reportsExistingMessages() {
        String longValue = "A".repeat(201);
        assertParseFailure(parser, "1 ai/" + longValue + " pr/" + longValue + " fn/" + longValue,
                FinancialNeed.MESSAGE_LENGTH + "\n" + Priority.MESSAGE_LENGTH + "\n" + AreaOfInterest.MESSAGE_LENGTH);
        for (String value : new String[] {"Bad!", "Needs:Wants", "\tValue", "Value\n", "Value\r"}) {
            assertParseFailure(parser, "1 ai/" + value + " pr/" + value + " fn/" + value,
                    FinancialNeed.MESSAGE_INVALID_CHARACTERS + "\n" + Priority.MESSAGE_INVALID_CHARACTERS
                            + "\n" + AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        }
        assertParseFailure(parser, "1 ai/Bad! pr/" + longValue + " fn/",
                FinancialNeed.MESSAGE_BLANK + "\n" + Priority.MESSAGE_LENGTH
                        + "\n" + AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
    }

    @Test
    public void parse_clearMarkersWithInvalidFields_reportsOnlyInvalidFields() {
        assertParseFailure(parser, "1 ai/ pr/ fn/  -   ",
                Priority.MESSAGE_BLANK + "\n" + AreaOfInterest.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/ pr/  -   fn/",
                FinancialNeed.MESSAGE_BLANK + "\n" + AreaOfInterest.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/  -   pr/ fn/",
                FinancialNeed.MESSAGE_BLANK + "\n" + Priority.MESSAGE_BLANK);
        for (String marker : new String[] {"\t-", "-\n", "-\r"}) {
            assertParseFailure(parser, "1 ai/" + marker + " pr/- fn/" + marker,
                    FinancialNeed.MESSAGE_INVALID_CHARACTERS + "\n" + AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void parse_invalidIndexOrPrefixesWithInvalidFields_preservesErrorPrecedence() {
        assertParseFailure(parser, "ai/ pr/ fn/", InfoCommandParser.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, "0 ai/ pr/ fn/", InfoCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 ai/ fn/ pr/ fn/",
                String.format(InfoCommandParser.MESSAGE_DUPLICATE_PARAMETER, "fn/"));
        assertParseFailure(parser, "1 ai/ pr/ fn/ /unknown", InfoCommandParser.MESSAGE_INVALID_PARAMETER);
    }

    @Test
    public void parse_invalidFinancialNeed_exactMessages() {
        assertParseFailure(parser, "1 fn/", FinancialNeed.MESSAGE_BLANK);
        assertParseFailure(parser, "1 fn/" + "A".repeat(201), FinancialNeed.MESSAGE_LENGTH);
        for (String value : new String[] {"Bad!", "Needs:Wants", "\tRetirement", "Retirement\n", "Retirement\r"}) {
            assertParseFailure(parser, "1 fn/" + value, FinancialNeed.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void parse_invalidPriority_exactMessages() {
        assertParseFailure(parser, "1 pr/", Priority.MESSAGE_BLANK);
        assertParseFailure(parser, "1 pr/" + "A".repeat(201), Priority.MESSAGE_LENGTH);
        for (String value : new String[] {"Bad!", "Needs:Wants", "\tRetirement", "Retirement\n", "Retirement\r"}) {
            assertParseFailure(parser, "1 pr/" + value, Priority.MESSAGE_INVALID_CHARACTERS);
        }
    }

    @Test
    public void parse_invalidAreaOfInterest_exactMessages() {
        assertParseFailure(parser, "1 ai/", AreaOfInterest.MESSAGE_BLANK);
        assertParseFailure(parser, "1 ai/" + "A".repeat(201), AreaOfInterest.MESSAGE_LENGTH);
        for (String value : new String[] {"Bad!", "Needs:Wants", "\tRetirement", "Retirement\n", "Retirement\r"}) {
            assertParseFailure(parser, "1 ai/" + value, AreaOfInterest.MESSAGE_INVALID_CHARACTERS);
        }
    }
}
