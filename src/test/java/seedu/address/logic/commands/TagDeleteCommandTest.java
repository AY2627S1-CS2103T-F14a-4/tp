package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class TagDeleteCommandTest {
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());

    @Test
    public void execute_noTagsSpecified_deletesAllTagsAndPreservesFields() throws Exception {
        Person original = model.getFilteredPersonList().get(1);

        CommandResult result = new TagDeleteCommand(INDEX_SECOND_PERSON, Set.of()).execute(model, null);

        assertEquals(new PersonBuilder(original).withTags().build(), model.getFilteredPersonList().get(1));
        assertEquals(String.format(TagDeleteCommand.MESSAGE_DELETE_ALL_SUCCESS, original.getName()),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_selectedTags_deletesAllSelectedTagsCaseInsensitively() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withTags("friend", "referral", "priority").build();
        model.setPerson(model.getFilteredPersonList().get(0), original);
        Set<Tag> tagsToDelete = Set.of(new Tag("FRIEND"), new Tag("referral"));

        CommandResult result = new TagDeleteCommand(INDEX_FIRST_PERSON, tagsToDelete).execute(model, null);

        assertEquals(new PersonBuilder(original).withTags("priority").build(), model.getFilteredPersonList().get(0));
        assertEquals(String.format(TagDeleteCommand.MESSAGE_DELETE_SELECTED_SUCCESS,
                original.getName(), "[FRIEND, referral]"), result.getFeedbackToUser());
    }

    @Test
    public void execute_filteredIndex_updatesDisplayedPersonOnly() throws Exception {
        Person untouched = model.getFilteredPersonList().get(0);
        Person target = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new TagDeleteCommand(INDEX_FIRST_PERSON, Set.of(new Tag("friends"))).execute(model, null);

        assertEquals(untouched, model.getClientBook().getPersonList().get(0));
        assertEquals(new PersonBuilder(target).withTags("owesMoney").build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_missingTag_failureWithoutDeletingExistingTags() {
        TagDeleteCommand command = new TagDeleteCommand(INDEX_FIRST_PERSON,
                Set.of(new Tag("friends"), new Tag("missing")));

        assertCommandFailure(command, model,
                String.format(TagDeleteCommand.MESSAGE_TAGS_NOT_FOUND, "[missing]"));
    }

    @Test
    public void execute_contactWithoutTags_failure() {
        assertCommandFailure(new TagDeleteCommand(INDEX_FIRST_PERSON, Set.of()),
                new ModelManager(new ClientBookBuilder()
                        .withPerson(new PersonBuilder().withTags().build()).build(), new UserPrefs()),
                TagDeleteCommand.MESSAGE_NO_TAGS);
    }

    @Test
    public void execute_invalidDisplayedIndex_failure() {
        assertCommandFailure(new TagDeleteCommand(INDEX_SECOND_PERSON, Set.of()), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_throughLogic_requiresConfirmationBeforeDeletingAndSaving() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        JsonClientBookStorage clientBookStorage =
                new JsonClientBookStorage(temporaryFolder.resolve("clientBook.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(clientBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        CommandResult confirmation = logic.execute("tagdelete 1 friends");

        assertEquals(String.format(TagDeleteCommand.MESSAGE_CONFIRMATION,
                original.getName(), "[friends]"), confirmation.getFeedbackToUser());
        assertEquals(original, model.getFilteredPersonList().get(0));
        assertFalse(clientBookStorage.readClientBook().isPresent());

        CommandResult result = logic.execute("YeS");

        assertEquals(new PersonBuilder(original).withTags().build(), model.getFilteredPersonList().get(0));
        assertEquals(model.getClientBook(), clientBookStorage.readClientBook().orElseThrow());
    }

    @Test
    public void confirmationResponse_shortAndFullFormsAreCaseInsensitive() throws Exception {
        int fileNumber = 0;
        for (String response : Set.of("y", "Y", "yes", "YES")) {
            Model responseModel = new ModelManager(getTypicalClientBook(), new UserPrefs());
            LogicManager logic = createLogic(responseModel, "confirm-" + fileNumber++);
            Person original = responseModel.getFilteredPersonList().get(0);

            logic.execute("tagdelete 1 friends");
            logic.execute(response);

            assertEquals(new PersonBuilder(original).withTags().build(), responseModel.getFilteredPersonList().get(0));
        }

        for (String response : Set.of("n", "N", "no", "NO")) {
            Model responseModel = new ModelManager(getTypicalClientBook(), new UserPrefs());
            LogicManager logic = createLogic(responseModel, "cancel-" + fileNumber++);
            Person original = responseModel.getFilteredPersonList().get(0);

            logic.execute("tagdelete 1 friends");
            CommandResult result = logic.execute(response);

            assertEquals(TagDeleteCommand.MESSAGE_CANCELLED, result.getFeedbackToUser());
            assertEquals(original, responseModel.getFilteredPersonList().get(0));
            assertEquals("Listed all clients and prospects.", logic.execute("list").getFeedbackToUser());
        }
    }

    @Test
    public void confirmationResponse_invalidResponse_rejectedAndConfirmationRemainsPending() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        LogicManager logic = createLogic(model, "invalid-response");
        logic.execute("tagdelete 1 friends");

        assertThrows(CommandException.class, TagDeleteCommand.MESSAGE_INVALID_CONFIRMATION, () ->
                logic.execute("maybe"));
        assertEquals(original, model.getFilteredPersonList().get(0));

        logic.execute("Y");
        assertEquals(new PersonBuilder(original).withTags().build(), model.getFilteredPersonList().get(0));
    }

    @Test
    public void getConfirmationMessage_noTagsSpecified_listsAllStoredTags() throws Exception {
        Person original = model.getFilteredPersonList().get(1);

        String message = new TagDeleteCommand(INDEX_SECOND_PERSON, Set.of()).getConfirmationMessage(model);

        assertEquals(String.format(TagDeleteCommand.MESSAGE_CONFIRMATION,
                original.getName(), "[friends, owesMoney]"), message);
        assertEquals(original, model.getFilteredPersonList().get(1));
    }

    @Test
    public void constructorAndExecute_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new TagDeleteCommand(null, Set.of()));
        assertThrows(NullPointerException.class, () -> new TagDeleteCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () ->
                new TagDeleteCommand(INDEX_FIRST_PERSON, Set.of()).execute(null, null));
    }

    @Test
    public void equals_comparesIndexAndTags() {
        TagDeleteCommand command = new TagDeleteCommand(INDEX_FIRST_PERSON, Set.of(new Tag("friend")));

        assertEquals(command, command);
        assertEquals(command, new TagDeleteCommand(INDEX_FIRST_PERSON, Set.of(new Tag("FRIEND"))));
        assertNotEquals(command, new TagDeleteCommand(INDEX_SECOND_PERSON, Set.of(new Tag("friend"))));
        assertNotEquals(command, new TagDeleteCommand(INDEX_FIRST_PERSON, Set.of()));
        assertNotEquals(command, null);
        assertNotEquals(command, "Text");
    }

    private LogicManager createLogic(Model targetModel, String fileName) {
        return new LogicManager(targetModel, new StorageManager(
                new JsonClientBookStorage(temporaryFolder.resolve(fileName + ".json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve(fileName + "-preferences.json"))));
    }
}
