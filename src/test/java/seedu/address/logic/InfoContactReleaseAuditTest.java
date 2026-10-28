package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.person.AreaOfInterest;
import seedu.address.model.person.FinancialNeed;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.InteractionOutcome;
import seedu.address.model.person.InteractionType;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.PreferredContactChannel;
import seedu.address.model.person.PreferredContactTiming;
import seedu.address.model.person.Priority;
import seedu.address.model.person.RecurringReminder;
import seedu.address.model.person.Relationship;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Release audit checks through command routing, execution and real JSON storage. */
public class InfoContactReleaseAuditTest {
    private static final String[] PREFIXES = {"fn/", "pr/", "ai/", "ch/", "tm/"};
    private static final String[] OLD = {"Old need", "Old priority", "Old interest", "Old channel", "Old timing"};
    private static final String[] NEW = {"New need", "New priority", "New interest", "New channel", "New timing"};

    @TempDir
    Path temporaryFolder;

    @Test
    public void execute_contactMalformedUtf16_rejectsWithoutChangingModelOrJson() throws Exception {
        Harness harness = new Harness(person(OLD));
        for (String value : List.of("Email\ud800", "\udc00Email", "x\ud800y", "\udc00\ud800")) {
            harness.reject("contact 1 ch/" + value, ParseException.class);
            harness.reject("contact 1 ch/- tm/" + value, ParseException.class);
        }
    }

    @Test
    public void execute_contactUnicodeLineSeparators_preservesDocumentedFreeText() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        JsonClientBookStorage json = new JsonClientBookStorage(temporaryFolder.resolve("clients.json"));
        Logic logic = new LogicManager(model, new StorageManager(json,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        for (String separator : List.of("\u0085", "\u2028", "\u2029")) {
            for (String field : List.of("ch/", "tm/")) {
                logic.execute("contact 1 " + field + separator + "Email" + separator);
                Person person = model.getClientBook().getPersonList().getFirst();
                assertEquals("Email", field.equals("ch/")
                        ? person.getPreferredContactChannel().orElseThrow().value
                        : person.getPreferredContactTiming().orElseThrow().value);
                logic.execute("contact 1 " + field + "Morning" + separator + "Evening");
                person = model.getClientBook().getPersonList().getFirst();
                assertEquals("Morning" + separator + "Evening", field.equals("ch/")
                        ? person.getPreferredContactChannel().orElseThrow().value
                        : person.getPreferredContactTiming().orElseThrow().value);
                assertEquals(model.getClientBook(), json.readClientBook().orElseThrow());
            }
        }
    }

    @Test
    public void execute_allPresenceChangeAndOrderCombinations_preservesWholePersonAfterRestart() throws Exception {
        for (int start : new int[] {0, 3}) {
            int count = start == 0 ? 3 : 2;
            for (int presence = 0; presence < (1 << count); presence++) {
                String[] initial = OLD.clone();
                for (int field = 0; field < count; field++) {
                    if ((presence & (1 << field)) == 0) {
                        initial[start + field] = null;
                    }
                }
                int combinations = (int) Math.pow(3, count);
                for (int change = 1; change < combinations; change++) {
                    int remaining = change;
                    String[] expected = initial.clone();
                    List<String> fields = new ArrayList<>();
                    for (int field = start; field < start + count; field++) {
                        int action = remaining % 3;
                        remaining /= 3;
                        if (action != 0) {
                            expected[field] = action == 1 ? NEW[field] : null;
                            fields.add(PREFIXES[field] + (action == 1 ? NEW[field] : "-"));
                        }
                    }
                    for (String ordering : permutations(fields)) {
                        Harness harness = new Harness(person(initial));
                        String command = (start == 0 ? "info" : "contact") + " 1 " + ordering;
                        harness.logic.execute(command);
                        assertEquals(person(expected), harness.first(), command);
                        harness.assertReopened();
                        String saved = Files.readString(harness.file);
                        harness.logic.execute(command);
                        assertEquals(person(expected), harness.first(), command);
                        assertEquals(saved, Files.readString(harness.file), command);
                    }
                }
            }
        }
    }

    @Test
    public void execute_lengthAndUnicodeBoundaries_usesUtf16LengthsForEveryField() throws Exception {
        Harness harness = new Harness(person(OLD));
        String letter = new String(Character.toChars(0x10400));
        for (int field = 0; field < PREFIXES.length; field++) {
            String command = field < 3 ? "info 1 " : "contact 1 ";
            for (int length : new int[] {1, 199, 200}) {
                for (String value : List.of("a".repeat(length), letter.repeat(length / 2)
                        + (length % 2 == 0 ? "" : "e"))) {
                    harness.logic.execute(command + PREFIXES[field] + "  " + value + "  ");
                    assertEquals(value, values(harness.first())[field]);
                    harness.assertReopened();
                }
            }
            for (String value : List.of("", "   ", "a".repeat(201), letter.repeat(100) + "a")) {
                harness.reject(command + PREFIXES[field] + value, ParseException.class);
            }
        }
        for (String value : List.of("e\u0301pargne", "\u5ba2\u6237", "\u0661\u0662", "\u0301",
                "Planning Life/Health insurance", "Save $3000/month + CPF/SRS 4%", ".,'()&-$%+/")) {
            harness.logic.execute("info 1 fn/" + value + " pr/" + value + " ai/" + value);
            assertEquals(value, harness.first().getFinancialNeed().orElseThrow().value);
            harness.assertReopened();
        }
        harness.logic.execute("contact 1 ch/" + "\ud83d\ude00".repeat(100));
        harness.assertReopened();
        harness.reject("contact 1 tm/" + "\ud83d\ude00".repeat(100) + "x", ParseException.class);
    }

    @Test
    public void execute_whitespaceAndControls_distinguishesClearFromText() throws Exception {
        Harness harness = new Harness(person(OLD));
        for (String value : List.of("\t", "\n", "\r", "\u0000", "\u001f", "\u00a0", "\u2003",
                "\u2007", "\u202f", "\u0085", "\u2028", "\u2029")) {
            for (String prefix : List.of("fn/", "pr/", "ai/")) {
                harness.reject("info 1 " + prefix + value + "Value", ParseException.class);
                harness.reject("info 1 " + prefix + "Value" + value, ParseException.class);
                harness.reject("info 1 " + prefix + "-" + value, ParseException.class);
            }
        }
        for (String padding : List.of("\t", "\n", "\r", "\u00a0", "\u2003", "\u2007",
                "\u202f", "\u0085", "\u2028", "\u2029")) {
            harness.reject("contact 1 ch/" + padding, ParseException.class);
            harness.logic.execute("contact 1 ch/" + padding + "-" + padding);
            assertEquals("-", harness.first().getPreferredContactChannel().orElseThrow().value);
            harness.assertReopened();
        }
        harness.logic.execute("contact 1 ch/ -  tm/ - ");
        assertTrue(harness.first().getPreferredContactChannel().isEmpty());
        assertTrue(harness.first().getPreferredContactTiming().isEmpty());
        harness.logic.execute("info 1 fn/ -  ai/ -  pr/ - ");
        assertTrue(harness.first().getFinancialNeed().isEmpty());
        assertTrue(harness.first().getPriority().isEmpty());
        assertTrue(harness.first().getAreaOfInterest().isEmpty());
        harness.assertReopened();
    }

    @Test
    public void execute_malformedParametersAndIndices_rejectsAtomically() throws Exception {
        Harness harness = new Harness(person(OLD));
        for (String fields : List.of("", "fn/", "fn/- pr/", "ai/- fn/Bad!", "pr/Bad:", "fn/Bad_",
                "fn/A fn/B", "pr/- pr/-", "ai/A fn/B ai/C", "/fn Old", "/pr/High", "/ai",
                "FN/Value", "Pr/High", "AI/Value", "fnn/Value", "prr/Value", "aii/Value",
                "xyz/Value fn/Good", "fn/Good /xyz", "fn/Good FN/Value", "fn/Good prr/High")) {
            harness.reject("info 1 " + fields, ParseException.class);
        }
        for (String fields : List.of("", "ch/", "ch/- tm/", "tm/- ch/", "ch/A ch/B", "tm/A ch/B tm/C",
                "/ch Email", "/tm/Evenings", "cch/Email", "chh/Email", "ttm/Evenings", "tmm/Evenings",
                "ch/Good /tm", "ch/Good tmm/Evenings", "CH/Email", "TM/Evenings", "xx/Value ch/Good")) {
            harness.reject("contact 1 " + fields, ParseException.class);
        }
        for (String index : List.of("", "0", "-1", "+1", "1.0", "1 2", "2147483648",
                "999999999999999999999", "\u00a01")) {
            harness.reject("info " + index + " fn/Good", ParseException.class);
            harness.reject("contact " + index + " ch/Good", ParseException.class);
        }
        for (String index : List.of("2", "2147483647")) {
            harness.reject("info " + index + " fn/Good", CommandException.class);
            harness.reject("contact " + index + " ch/Good", CommandException.class);
        }
        for (String index : List.of("01", "\u0661", "\uff11")) {
            harness.logic.execute("info " + index + " fn/Good");
            harness.logic.execute("contact " + index + " ch/Good");
            harness.assertReopened();
        }
        for (String command : List.of("", " \t", "INFO 1 fn/Good", "Contact 1 ch/Good")) {
            harness.reject(command, ParseException.class);
        }
    }

    @Test
    public void execute_documentedPrefixLimitations_preservesOrSplitsExactlyAsSpecified() throws Exception {
        Harness harness = new Harness(person(OLD));
        harness.logic.execute("info 1 fn/Review pr/High options ai/Planning xyz/Value");
        assertEquals("Review", values(harness.first())[0]);
        assertEquals("High options", values(harness.first())[1]);
        assertEquals("Planning xyz/Value", values(harness.first())[2]);
        harness.logic.execute("contact 1 ch/Email TM/Evenings xx/After 6 pm tm/Weekdays AM/PM");
        assertEquals("Email TM/Evenings xx/After 6 pm", values(harness.first())[3]);
        assertEquals("Weekdays AM/PM", values(harness.first())[4]);
        harness.logic.execute("contact 1 ch/Email\ttm/Evenings");
        assertEquals("Email\ttm/Evenings", values(harness.first())[3]);
        harness.logic.execute("info\t1 fn/Boundary");
        harness.logic.execute("contact\t1 ch/Boundary");
        harness.assertReopened();
    }

    @Test
    public void execute_seededValidUnicode_roundTripsWithoutNormalizationOrUnrelatedChanges() throws Exception {
        Random random = new Random(130012L);
        Harness harness = new Harness(person(OLD));
        String[] alphabet = {"a", "\u00e9", "\u0301", "\u5ba2", "\u0662", " ", "$", "%", "+", "-",
            new String(Character.toChars(0x10400))};
        for (int sample = 0; sample < 100; sample++) {
            StringBuilder value = new StringBuilder("a");
            int size = 1 + random.nextInt(90);
            for (int i = 0; i < size; i++) {
                value.append(alphabet[random.nextInt(alphabet.length)]);
            }
            value.append('z');
            harness.logic.execute("info 1 ai/" + value + " fn/" + value + " pr/" + value);
            harness.logic.execute("contact 1 tm/" + value + " ch/" + value);
            for (String actual : values(harness.first())) {
                assertEquals(value.toString(), actual, "seed=130012 sample=" + sample);
            }
            assertUnrelated(person(OLD), harness.first());
            harness.assertReopened();
        }
    }

    @Test
    public void execute_nonconsecutiveFilteredSortedIndices_updatesOriginalVisiblePerson() throws Exception {
        Harness harness = new Harness(person(OLD));
        Person bob = new PersonBuilder(person(OLD)).withName("Bob Bee").withPhone("88888888")
                .withEmail("bob@example.com").withNextAction(new NextAction("Bob", LocalDate.of(2001, 1, 1), false))
                .build();
        Person cara = new PersonBuilder(person(OLD)).withName("Cara Bee").withPhone("99999999")
                .withEmail("cara@example.com").withNextAction(new NextAction("Cara", LocalDate.of(1999, 1, 1), false))
                .build();
        harness.model.addPerson(bob);
        harness.model.addPerson(cara);
        harness.logic.execute("due");
        assertEquals(List.of(cara, person(OLD), bob), harness.model.getFilteredPersonList());
        harness.logic.execute("info 3 pr/Sorted");
        harness.logic.execute("contact 3 ch/Sorted");
        assertEquals("Sorted", harness.model.getClientBook().getPersonList().get(2)
                .getPreferredContactChannel().orElseThrow().value);
        assertEquals(person(OLD), harness.first());
        assertEquals(bob, harness.model.getClientBook().getPersonList().get(1));
        assertEquals("Cara Bee", harness.model.getFilteredPersonList().getFirst().getName().fullName);
        harness.logic.execute("find k/Sorted");
        assertEquals(1, harness.model.getFilteredPersonList().size());
        harness.reject("info 1 fn/Hidden", CommandException.class);
        harness.reject("contact 2 tm/Hidden", CommandException.class);
        harness.logic.execute("info 3 pr/-");
        assertEquals(1, harness.model.getFilteredPersonList().size());
        harness.logic.execute("contact 3 ch/-");
        assertTrue(harness.model.getFilteredPersonList().isEmpty());
        harness.reject("contact 3 ch/Invisible", CommandException.class);
        harness.assertReopened();
    }

    @Test
    public void execute_mixedFeatureSequence_backupRestoreAndRestartPreserveData() throws Exception {
        Harness harness = new Harness(person(OLD));
        harness.logic.execute("edit 1 n/Amy Audit r/Client");
        harness.logic.execute("tag 1 audit");
        harness.logic.execute("note 1 d/2000-01-02 t/CALL o/PENDING n/Second note");
        harness.logic.execute("noteedit 1 2 n/Edited second note");
        assertEquals(InteractionType.CALL, harness.first().getInteractionNotes().get(1).getType());
        assertEquals(InteractionOutcome.PENDING, harness.first().getInteractionNotes().get(1).getOutcome());
        harness.logic.execute("remind 1 a/Policy d/02-28");
        harness.logic.execute("next 1 a/Review d/" + LocalDate.now().plusDays(1));
        harness.logic.execute("done 1");
        Person before = harness.first();
        harness.logic.execute("info 1 fn/New need ai/-");
        harness.logic.execute("contact 1 tm/New timing ch/-");
        assertUnrelated(before, harness.first());
        harness.logic.execute("notedelete 1 2");
        harness.logic.execute("unremind 1 a/Policy");
        assertEquals(person(OLD).getInteractionNotes(), harness.first().getInteractionNotes());
        assertEquals(person(OLD).getRecurringReminders(), harness.first().getRecurringReminders());
        assertEquals("New need", values(harness.first())[0]);
        assertEquals("New timing", values(harness.first())[4]);
        Person backedUp = harness.first();
        harness.logic.execute("backup");
        harness.logic.execute("info 1 fn/- pr/- ai/-");
        harness.logic.execute("contact 1 ch/- tm/-");
        harness.logic.execute("restore");
        assertEquals(backedUp, harness.first());
        harness.assertReopened();
    }

    @Test
    public void execute_saveFailure_preventsCommitAndRetainsJsonFilterAndSort() throws Exception {
        Harness harness = new Harness(person(OLD));
        harness.logic.execute("due");
        StorageManager failing = new StorageManager(new JsonClientBookStorage(harness.file) {
            @Override
            public void saveClientBook(ReadOnlyClientBook book) throws IOException {
                throw new IOException("Audit injected write failure before opening file");
            }
        }, new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        Logic failedLogic = new LogicManager(harness.model, failing);
        String saved = Files.readString(harness.file);
        var comparator = harness.model.getFilteredPersonComparator();
        for (String command : List.of("info 1 fn/- ai/Changed", "contact 1 ch/- tm/Changed")) {
            assertThrows(CommandException.class, () -> failedLogic.execute(command));
            assertEquals(person(OLD), harness.first());
            assertEquals(saved, Files.readString(harness.file));
            assertEquals(comparator, harness.model.getFilteredPersonComparator());
            assertEquals(List.of(person(OLD)), harness.model.getFilteredPersonList());
        }
        harness.logic.execute("info 1 pr/Recovered");
        harness.logic.execute("contact 1 tm/Recovered");
        harness.assertReopened();
    }

    @Test
    public void readJson_malformedUtf16Preferences_rejectsEscapedSurrogates() throws Exception {
        Harness harness = new Harness(person(OLD));
        String saved = Files.readString(harness.file);
        for (String property : List.of("preferredContactChannel", "preferredContactTiming")) {
            for (String value : List.of("\\ud800", "\\udc00", "x\\ud800y")) {
                String malformed = saved.replaceAll("(\"" + property + "\"\\s*:\\s*)\"[^\"]*\"",
                        "$1\"" + java.util.regex.Matcher.quoteReplacement(value) + "\"");
                Files.writeString(harness.file, malformed);
                assertThrows(DataLoadingException.class, () -> harness.json.readClientBook());
                assertEquals(person(OLD), harness.first());
            }
        }
    }

    @Test
    public void execute_seededMalformedInputs_neverCrashesOrPartiallyMutates() throws Exception {
        Random random = new Random(15001213L);
        Harness harness = new Harness(person(OLD));
        String[] fragments = {"fn/", "pr/", "ai/", "ch/", "tm/", "FN/", "TM/", "xyz/", "/ch", "fnn/",
            " ", "\t", "\n", "\r", "\u0000", "\u0085", "\u00a0", "\u2028", "\ud800", "\udc00",
            "-", "!", "Good", "\ud83d\ude00", "a".repeat(201)};
        for (int sample = 0; sample < 250; sample++) {
            StringBuilder command = new StringBuilder(random.nextBoolean() ? "info 1 " : "contact 1 ");
            int count = 1 + random.nextInt(8);
            for (int i = 0; i < count; i++) {
                command.append(fragments[random.nextInt(fragments.length)]);
            }
            Person before = harness.first();
            String saved = Files.readString(harness.file);
            try {
                harness.logic.execute(command.toString());
                assertUnrelated(before, harness.first());
                harness.assertReopened();
            } catch (ParseException | CommandException expected) {
                assertEquals(before, harness.first(), "seed=15001213 sample=" + sample);
                assertEquals(saved, Files.readString(harness.file), "seed=15001213 sample=" + sample);
            }
        }
    }

    private static Set<String> permutations(List<String> fields) {
        if (fields.isEmpty()) {
            return Set.of("");
        }
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0; i < fields.size(); i++) {
            List<String> rest = new ArrayList<>(fields);
            String first = rest.remove(i);
            for (String suffix : permutations(rest)) {
                result.add((first + " " + suffix).trim());
            }
        }
        return result;
    }

    private static String[] values(Person person) {
        return new String[] {person.getFinancialNeed().map(value -> value.value).orElse(null),
            person.getPriority().map(value -> value.value).orElse(null),
            person.getAreaOfInterest().map(value -> value.value).orElse(null),
            person.getPreferredContactChannel().map(value -> value.value).orElse(null),
            person.getPreferredContactTiming().map(value -> value.value).orElse(null)};
    }

    private static Person person(String[] fields) {
        Person base = new PersonBuilder().withRelationship(Relationship.CLIENT).withTags("friend")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2000, 1, 1), "Original metadata",
                        InteractionType.MEETING, InteractionOutcome.UNSUCCESSFUL))
                .withNextAction(new NextAction("Original action", LocalDate.of(2000, 1, 1), false))
                .withRecurringReminders(new RecurringReminder("Birthday", java.time.MonthDay.of(1, 1))).build();
        return new Person(base.getName(), base.getPhone(), base.getEmail(), base.getTags(), base.getRelationship(),
                fields[0] == null ? null : new FinancialNeed(fields[0]),
                fields[1] == null ? null : new Priority(fields[1]),
                fields[2] == null ? null : new AreaOfInterest(fields[2]),
                base.getInteractionNotes(), base.getNextAction().orElseThrow(),
                fields[3] == null ? null : new PreferredContactChannel(fields[3]),
                fields[4] == null ? null : new PreferredContactTiming(fields[4]), base.getRecurringReminders());
    }

    private static void assertUnrelated(Person expected, Person actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPhone(), actual.getPhone());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getRelationship(), actual.getRelationship());
        assertEquals(expected.getTags(), actual.getTags());
        assertEquals(expected.getInteractionNotes(), actual.getInteractionNotes());
        assertEquals(expected.getRecurringReminders(), actual.getRecurringReminders());
        assertEquals(expected.getNextAction(), actual.getNextAction());
    }

    private class Harness {
        private final Path file = temporaryFolder.resolve("clients.json");
        private final ModelManager model = new ModelManager();
        private final JsonClientBookStorage json = new JsonClientBookStorage(file);
        private final Logic logic = new LogicManager(model, new StorageManager(json,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));

        Harness(Person person) throws IOException {
            model.addPerson(person);
            json.saveClientBook(model.getClientBook());
        }

        Person first() {
            return model.getClientBook().getPersonList().getFirst();
        }

        void assertReopened() throws Exception {
            ModelManager reopened = new ModelManager(json.readClientBook().orElseThrow(), model.getUserPrefs());
            assertEquals(model.getClientBook(), reopened.getClientBook());
        }

        void reject(String command, Class<? extends Exception> exception) throws Exception {
            List<Person> before = List.copyOf(model.getClientBook().getPersonList());
            List<Person> visible = List.copyOf(model.getFilteredPersonList());
            byte[] saved = Files.readAllBytes(file);
            assertThrows(exception, () -> logic.execute(command), command);
            assertEquals(before, model.getClientBook().getPersonList(), command);
            assertEquals(visible, model.getFilteredPersonList(), command);
            assertTrue(Arrays.equals(saved, Files.readAllBytes(file)), command);
        }
    }
}
