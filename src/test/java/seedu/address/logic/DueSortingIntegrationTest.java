package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.TagDeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class DueSortingIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final LocalDate today = LocalDate.now();
    private final Person alice = new PersonBuilder().withName("Alice").withPhone("90000001")
            .withEmail("alice@example.com").withNextAction(new NextAction("Call", today, false)).build();
    private final Person zed = new PersonBuilder().withName("Zed").withPhone("90000002")
            .withEmail("zed@example.com").withNextAction(new NextAction("Call", today.minusDays(1), false)).build();
    private final Person bob = new PersonBuilder().withName("Bob").withPhone("90000003")
            .withEmail("bob@example.com").withTags("review")
            .withNextAction(new NextAction("Call", today.minusDays(1), false)).build();

    private Model model;
    private Logic logic;
    private JsonClientBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(new ClientBookBuilder().withPerson(alice).withPerson(zed).withPerson(bob).build(),
                new UserPrefs());
        storage = new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json"));
        storage.saveClientBook(model.getClientBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_dueThenMutation_keepsDisplayOrderAndPersistsOriginalContactOrder() throws Exception {
        logic.execute("due");
        assertEquals(List.of(bob, zed, alice), model.getFilteredPersonList());
        assertThrows(CommandException.class, () -> logic.execute("done 999"));
        assertEquals(List.of(bob, zed, alice), model.getFilteredPersonList());

        logic.execute("note 3 d/2000-01-01 n/Discussed plans");
        Person updatedBob = model.getFilteredPersonList().getFirst();
        assertEquals(bob.getName(), updatedBob.getName());
        assertEquals(1, updatedBob.getInteractionNotes().size());
        assertEquals(List.of(updatedBob, zed, alice), model.getFilteredPersonList());
        assertEquals(List.of(alice, zed, updatedBob), storage.readClientBook().orElseThrow().getPersonList());

        logic.execute("done 3");
        assertEquals(List.of(zed, alice), model.getFilteredPersonList());
        assertTrue(model.getClientBook().getPersonList().get(2).getNextAction().orElseThrow().isCompleted());
        logic.execute("delete 2");
        assertEquals(List.of(zed, alice), model.getFilteredPersonList());
        logic.execute("yes");
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertEquals(List.of(alice, model.getClientBook().getPersonList().get(1)),
                storage.readClientBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_futureNoteRejected_preservesSortedViewAndSavedData() throws Exception {
        logic.execute("due");
        String savedBefore = Files.readString(storage.getClientBookFilePath());
        String command = "note 3 d/" + today.plusDays(1) + " n/Future interaction";
        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command));

        assertEquals(NoteCommand.MESSAGE_FUTURE_DATE, exception.getMessage());
        assertEquals(List.of(bob, zed, alice), model.getFilteredPersonList());
        assertEquals(savedBefore, Files.readString(storage.getClientBookFilePath()));
    }

    @Test
    public void execute_tagDeleteConfirmation_preservesSortedViewAndOriginalClientIndices() throws Exception {
        logic.execute("due");
        assertTrue(logic.execute("tagdelete 3").getFeedbackToUser().contains("Bob"));
        assertEquals(List.of(bob, zed, alice), model.getFilteredPersonList());
        assertEquals(TagDeleteCommand.MESSAGE_CANCELLED, logic.execute("no").getFeedbackToUser());
        assertEquals(List.of(bob, zed, alice), model.getFilteredPersonList());

        logic.execute("tagdelete 3");
        logic.execute("yes");
        Person updatedBob = model.getFilteredPersonList().getFirst();
        assertEquals(bob.getName(), updatedBob.getName());
        assertTrue(updatedBob.getTags().isEmpty());
        assertEquals(bob.getNextAction(), updatedBob.getNextAction());
        assertEquals(List.of(updatedBob, zed, alice), model.getFilteredPersonList());
        assertEquals(List.of(alice, zed, updatedBob), storage.readClientBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_listAndFind_resetDueSortingToOriginalContactOrder() throws Exception {
        logic.execute("due");
        logic.execute("list");
        assertEquals(List.of(alice, zed, bob), model.getFilteredPersonList());
        logic.execute("due");
        logic.execute("find Alice Bob");
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
    }

    @Test
    public void execute_dateAndNameChanges_reorderTheActiveDueView() throws Exception {
        logic.execute("due");
        logic.execute("edit 3 n/Zoe");
        assertEquals(List.of(zed.getName(), new seedu.address.model.person.Name("Zoe"), alice.getName()),
                model.getFilteredPersonList().stream().map(Person::getName).toList());
        logic.execute("next 2 a/Call d/9999-12-31");
        assertEquals(List.of("Zoe", "Alice"), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }
}
