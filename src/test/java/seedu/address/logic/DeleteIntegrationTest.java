package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises deletion through the parser, logic, model and real JSON storage.
 * Drafted with OpenAI ChatGPT/Codex assistance for ZachL1113.
 */
public class DeleteIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private final Person client = new PersonBuilder().withName("John Doe").withPhone("91234567")
            .withEmail("john@example.com").withRelationship(Relationship.CLIENT).build();
    private final Person prospect = new PersonBuilder().withName("Jane Tan").withPhone("98765432")
            .withEmail("jane@example.com").withRelationship(Relationship.PROSPECT)
            .withTags("referral").withFinancialNeed("Retirement planning").withPriority("Protect family income")
            .withAreaOfInterest("Life insurance")
            .withInteractionNotes(new InteractionNote(LocalDate.of(2026, 10, 6), "Discussed next steps."))
            .build();

    private Model model;
    private Logic logic;
    private JsonClientBookStorage clientBookStorage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(client);
        model.addPerson(prospect);
        clientBookStorage = new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json"));
        clientBookStorage.saveClientBook(model.getClientBook());
        StorageManager storage = new StorageManager(clientBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_deleteClient_preservesProspectDetailsAfterReopening() throws Exception {
        assertEquals("Client John Doe has been deleted successfully.",
                logic.execute("delete 1").getFeedbackToUser());
        assertEquals(List.of(prospect), logic.getFilteredPersonList());
        assertSavedPersons(prospect);
    }

    @Test
    public void execute_deleteProspect_preservesClientAfterReopening() throws Exception {
        assertEquals("Prospect Jane Tan has been deleted successfully.",
                logic.execute("delete 2").getFeedbackToUser());
        assertEquals(List.of(client), logic.getFilteredPersonList());
        assertSavedPersons(client);
    }

    @Test
    public void execute_deleteAfterNameSearch_usesOriginalIndex() throws Exception {
        logic.execute("find Jane");
        assertEquals(List.of(prospect), logic.getFilteredPersonList());
        assertEquals("Prospect Jane Tan has been deleted successfully.",
                logic.execute("delete 2").getFeedbackToUser());
        assertEquals(List.of(), logic.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(client), logic.getFilteredPersonList());
        assertSavedPersons(client);
    }

    @Test
    public void execute_deleteAfterRelationshipSearch_preservesHiddenClient() throws Exception {
        logic.execute("find r/prospect");
        assertEquals(List.of(prospect), logic.getFilteredPersonList());
        assertEquals("Prospect Jane Tan has been deleted successfully.",
                logic.execute("delete 2").getFeedbackToUser());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertSavedPersons(client);
    }

    @Test
    public void execute_deleteRemainingRecord_reindexesAndSavesEmptyList() throws Exception {
        logic.execute("delete 1");
        assertEquals(List.of(prospect), logic.getFilteredPersonList());
        assertEquals("Prospect Jane Tan has been deleted successfully.",
                logic.execute("  delete 1  ").getFeedbackToUser());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertSavedPersons();

        assertRejectedWithoutChanges("delete 1", CommandException.class, "No client exists at index 1.");
    }

    @Test
    public void execute_invalidDelete_preservesRecordsAndSavedFile() throws Exception {
        assertRejectedWithoutChanges("delete", ParseException.class, "Client index is required.");
        for (String command : List.of("delete 0", "delete -1", "delete abc", "delete 1.5", "delete 2147483648")) {
            assertRejectedWithoutChanges(command, ParseException.class, "Client index must be a positive integer.");
        }
        for (String command : List.of("delete 1 2", "delete 1 abc", "delete 1\t2")) {
            assertRejectedWithoutChanges(command, ParseException.class, "Delete command only accepts a client index.");
        }
        assertRejectedWithoutChanges("delete 3", CommandException.class, "No client exists at index 3.");
        assertSavedPersons(client, prospect);
    }

    @Test
    public void execute_invalidFilteredIndex_preservesFilterAndHiddenRecords() throws Exception {
        logic.execute("find Jane");
        assertRejectedWithoutChanges("delete 1", CommandException.class, "No client exists at index 1.");
        assertEquals(List.of(prospect), logic.getFilteredPersonList());
        assertSavedPersons(client, prospect);

        logic.execute("find Nobody");
        assertRejectedWithoutChanges("delete 1", CommandException.class, "No client exists at index 1.");
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertSavedPersons(client, prospect);
    }

    /** Verifies both in-memory records and the result of reopening the saved JSON file. */
    private void assertSavedPersons(Person... expectedPersons) throws Exception {
        assertEquals(List.of(expectedPersons), model.getClientBook().getPersonList());
        Model reopened = new ModelManager(clientBookStorage.readClientBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(expectedPersons), reopened.getClientBook().getPersonList());
    }

    /** Rejected input must preserve records, the current filter and the exact saved bytes. */
    private void assertRejectedWithoutChanges(String command, Class<? extends Throwable> exceptionType,
            String expectedMessage) throws Exception {
        List<Person> recordsBefore = List.copyOf(model.getClientBook().getPersonList());
        List<Person> displayedBefore = List.copyOf(logic.getFilteredPersonList());
        String savedBefore = Files.readString(clientBookStorage.getClientBookFilePath());

        assertThrows(exceptionType, expectedMessage, () -> logic.execute(command));

        assertEquals(recordsBefore, model.getClientBook().getPersonList());
        assertEquals(displayedBefore, logic.getFilteredPersonList());
        assertEquals(savedBefore, Files.readString(clientBookStorage.getClientBookFilePath()));
    }
}
