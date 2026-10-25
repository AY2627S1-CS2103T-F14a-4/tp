package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonClientBookStorage clientBookStorage =
                new JsonClientBookStorage(temporaryFolder.resolve("clientBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(clientBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_contactThenUpdates_preferencesSurviveReopening() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withTags("friend")
                .withFinancialNeed("Retirement").withPriority("High").withAreaOfInterest("Insurance").build());
        logic.execute("contact 1 ch/WhatsApp tm/Weekdays after 6 pm");
        logic.execute("edit 1 p/81234567");
        logic.execute("info 1 /pr Protection");
        logic.execute("note 1 d/2000-01-01 n/Discussed plans");
        logic.execute("next 1 a/Call d/2000-01-02");
        logic.execute("done 1");
        Person beforeInvalidInput = model.getFilteredPersonList().getFirst();
        assertThrows(ParseException.class, () -> logic.execute("contact 1 ch/Phone tm/"));
        assertEquals(beforeInvalidInput, model.getFilteredPersonList().getFirst());
        JsonClientBookStorage reopened = new JsonClientBookStorage(temporaryFolder.resolve("clientBook.json"));
        assertEquals(model.getClientBook(), reopened.readClientBook().orElseThrow());
        Person restored = reopened.readClientBook().orElseThrow().getPersonList().getFirst();
        assertEquals("WhatsApp", restored.getPreferredContactChannel().orElseThrow().value);
        assertEquals("Weekdays after 6 pm", restored.getPreferredContactTiming().orElseThrow().value);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, String.format(DeleteCommand.MESSAGE_INVALID_INDEX, 9));
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, "Listed all clients and prospects.", model);
    }

    @Test
    public void execute_clearFinancialFields_savedAbsenceSurvivesReopening() throws Exception {
        Person original = new PersonBuilder(AMY).withRelationship(Relationship.CLIENT).withTags("friend")
                .withFinancialNeed("Retirement").withPriority("High").withAreaOfInterest("Insurance")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2000, 1, 1), "Discussed plans"))
                .withNextAction(new NextAction("Call", LocalDate.of(2000, 1, 2), false)).build();
        model.addPerson(original);
        logic.execute("info 1 /pr -");
        Person expected = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(), original.getFinancialNeed().orElseThrow(), null,
                original.getAreaOfInterest().orElseThrow(), original.getInteractionNotes(),
                original.getNextAction().orElseThrow());
        JsonClientBookStorage reopened = new JsonClientBookStorage(temporaryFolder.resolve("clientBook.json"));
        assertEquals(expected, reopened.readClientBook().orElseThrow().getPersonList().getFirst());
        assertEquals(expected, model.getFilteredPersonList().getFirst());

        // A requested clear followed by an invalid field must change neither the model nor the saved file.
        assertThrows(ParseException.class, () -> logic.execute("info 1 /fn - /ai"));
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        assertEquals(expected, reopened.readClientBook().orElseThrow().getPersonList().getFirst());
        logic.execute("info 1 /ai - /fn - /pr High-priority");
        Person restored = reopened.readClientBook().orElseThrow().getPersonList().getFirst();
        assertEquals(model.getFilteredPersonList().getFirst(), restored);
        assertTrue(restored.getFinancialNeed().isEmpty());
        assertTrue(restored.getAreaOfInterest().isEmpty());
        assertEquals("High-priority", restored.getPriority().orElseThrow().value);
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getPhone(), restored.getPhone());
        assertEquals(original.getEmail(), restored.getEmail());
        assertEquals(original.getRelationship(), restored.getRelationship());
        assertEquals(original.getTags(), restored.getTags());
        assertEquals(original.getInteractionNotes(), restored.getInteractionNotes());
        assertEquals(original.getNextAction(), restored.getNextAction());
    }

    @Test
    public void execute_notesThenUpdates_savedHistorySurvivesReopening() throws Exception {
        model.addPerson(new Person(AMY.getName(), AMY.getPhone(), AMY.getEmail(), Relationship.CLIENT));
        logic.execute("note 1 d/2000-02-29 n/Discussed retirement; https://example.com/a/b n/text d/text");
        logic.execute("note 1 d/2000-02-29 n/Discussed retirement; https://example.com/a/b n/text d/text");
        assertEquals(2, model.getFilteredPersonList().get(0).getInteractionNotes().size());
        logic.execute("edit 1 p/81234567");
        logic.execute("info 1 /fn Retirement");

        JsonClientBookStorage reopenedStorage =
                new JsonClientBookStorage(temporaryFolder.resolve("clientBook.json"));
        Model reopened = new ModelManager(reopenedStorage.readClientBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getClientBook(), reopened.getClientBook());
        Person restored = reopened.getFilteredPersonList().get(0);
        assertEquals(2, restored.getInteractionNotes().size());
        assertEquals(restored.getInteractionNotes().get(0), restored.getInteractionNotes().get(1));
        assertEquals("81234567", restored.getPhone().value);
        assertEquals("Retirement", restored.getFinancialNeed().orElseThrow().value);
        assertEquals(Relationship.CLIENT, restored.getRelationship());
    }

    @Test
    public void execute_findThenList_restoresCompleteList() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        Model expectedModel = new ModelManager(model.getClientBook(), new UserPrefs());

        logic.execute("find Amy");
        assertEquals(List.of(AMY), logic.getFilteredPersonList());
        assertCommandSuccess("list", "Listed all clients and prospects.", expectedModel);
        assertEquals(List.of(AMY, BOB), logic.getFilteredPersonList());

        logic.execute("find NoMatchingName");
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertCommandSuccess("list", "Listed all clients and prospects.", expectedModel);
        assertEquals(List.of(AMY, BOB), logic.getFilteredPersonList());
    }

    @Test
    public void execute_listVariants_restoreSameCompleteList() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        Model expectedModel = new ModelManager(model.getClientBook(), new UserPrefs());

        for (String command : List.of("list", "  list  ", "list clients", "  list clients  ")) {
            logic.execute("find Amy");
            assertEquals(List.of(AMY), logic.getFilteredPersonList());
            assertCommandSuccess(command, "Listed all clients and prospects.", expectedModel);
            assertEquals(List.of(AMY, BOB), logic.getFilteredPersonList());
        }
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageFailure_preservesFilteredList() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        logic.execute("find Amy");

        JsonClientBookStorage clientBookStorage = new JsonClientBookStorage(
                temporaryFolder.resolve("ExceptionClientBook.json")) {
            @Override
            public void saveClientBook(ReadOnlyClientBook clientBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        logic = new LogicManager(model, new StorageManager(clientBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"))));

        String addCommand = "add r/Client n/Test User e/test@example.com p/91234567";
        assertThrows(CommandException.class, () -> logic.execute(addCommand));

        assertEquals(List.of(AMY), logic.getFilteredPersonList());
        assertEquals(List.of(AMY, BOB), model.getClientBook().getPersonList());
    }

    @Test
    public void execute_restoreSavesPrimaryFileOnce() throws Exception {
        Path primaryFile = temporaryFolder.resolve("clientbook.json");
        Path backupFile = primaryFile.resolveSibling(BackupCommand.BACKUP_FILE_NAME);
        new JsonClientBookStorage(backupFile).saveClientBook(new ModelManager().getClientBook());
        AtomicInteger saveAttempts = new AtomicInteger();
        StorageManager storage = new StorageManager(new JsonClientBookStorage(primaryFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))) {
            @Override
            public void saveClientBook(ReadOnlyClientBook clientBook) throws IOException {
                saveAttempts.incrementAndGet();
                super.saveClientBook(clientBook);
            }
        };
        logic = new LogicManager(model, storage);

        logic.execute("restore");

        assertEquals(1, saveAttempts.get());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getClientBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonClientBookStorage that throws the IOException e when saving
        JsonClientBookStorage clientBookStorage = new JsonClientBookStorage(prefPath) {
            @Override
            public void saveClientBook(ReadOnlyClientBook clientBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(clientBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveClientBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + " r/Prospect" + NAME_DESC_AMY + EMAIL_DESC_AMY + PHONE_DESC_AMY;
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
