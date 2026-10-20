package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class NoteDeleteCommandTest {
    private static final InteractionNote NOTE = new InteractionNote(LocalDate.of(2000, 2, 29), "Called");
    private static final InteractionNote OTHER_NOTE = new InteractionNote(LocalDate.of(1999, 1, 1), "Met");

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_middleDuplicate_preservesRemainingOrderAndAllContactFields() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withRelationship(Relationship.CLIENT).withTags("client", "friend")
                .withFinancialNeed("Retirement").withPriority("High").withAreaOfInterest("Insurance")
                .withNextAction(new NextAction("Call", LocalDate.of(2026, 10, 20), true))
                .withPreferredContactChannel("Email").withPreferredContactTiming("Evenings")
                .withInteractionNotes(NOTE, OTHER_NOTE, NOTE, OTHER_NOTE).build();
        model.setPerson(model.getFilteredPersonList().get(0), original);

        CommandResult result = new NoteDeleteCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3)).execute(model);

        assertEquals(new PersonBuilder(original).withInteractionNotes(NOTE, OTHER_NOTE, OTHER_NOTE).build(),
                model.getFilteredPersonList().get(0));
        assertEquals("Deleted interaction note 3 for " + original.getName().fullName + ".",
                result.getFeedbackToUser());
        assertEquals(List.of(NOTE, OTHER_NOTE, NOTE, OTHER_NOTE), original.getInteractionNotes());
    }

    @Test
    public void execute_firstThenLastNote_leavesEmptyHistory() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withInteractionNotes(NOTE, OTHER_NOTE).build());
        NoteDeleteCommand command = new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);

        command.execute(model);
        assertEquals(new PersonBuilder(original).withInteractionNotes(OTHER_NOTE).build(),
                model.getFilteredPersonList().get(0));
        command.execute(model);
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_filteredList_updatesDisplayedProspectAndPreservesFilter() throws Exception {
        Person untouched = model.getFilteredPersonList().get(0);
        Person original = new PersonBuilder(model.getFilteredPersonList().get(1))
                .withRelationship(Relationship.PROSPECT).withInteractionNotes(NOTE, OTHER_NOTE).build();
        model.setPerson(model.getFilteredPersonList().get(1), original);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON).execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(original).withInteractionNotes(NOTE).build(),
                model.getFilteredPersonList().get(0));
        assertEquals(untouched, model.getAddressBook().getPersonList().get(0));
        assertCommandFailure(new NoteDeleteCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidContactIndex_rejectedWithoutChanges() {
        assertCommandFailure(new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new NoteDeleteCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1),
                INDEX_FIRST_PERSON), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_noNotesOrOutOfRangeNote_rejectedWithoutChanges() {
        assertCommandFailure(new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON), model,
                NoteDeleteCommand.MESSAGE_INVALID_NOTE_INDEX);
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withInteractionNotes(NOTE).build());
        assertCommandFailure(new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), model,
                NoteDeleteCommand.MESSAGE_INVALID_NOTE_INDEX);
    }

    @Test
    public void execute_throughLogic_deletionAndEmptyHistorySurviveReopening() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withTags("friend").withFinancialNeed("Retirement").withPriority("High")
                .withAreaOfInterest("Insurance").withPreferredContactChannel("Email")
                .withPreferredContactTiming("Evenings")
                .withNextAction(new NextAction("Call", LocalDate.of(2026, 10, 20), false)).build();
        model.setPerson(model.getFilteredPersonList().get(0),
                new PersonBuilder(original).withInteractionNotes(NOTE, OTHER_NOTE, NOTE).build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        logic.execute("notedelete 1 2");

        Model reopened = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reopened.getAddressBook());
        assertEquals(new PersonBuilder(original).withInteractionNotes(NOTE, NOTE).build(),
                reopened.getFilteredPersonList().get(0));
        logic.execute("notedelete 1 2");
        logic.execute("notedelete 1 1");
        reopened = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reopened.getAddressBook());
        assertEquals(original, reopened.getFilteredPersonList().get(0));
    }

    @Test
    public void constructorAndExecute_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new NoteDeleteCommand(null, INDEX_FIRST_PERSON));
        assertThrows(NullPointerException.class, () -> new NoteDeleteCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () ->
                new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void equals_comparesBothIndices() {
        NoteDeleteCommand command = new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);
        assertEquals(command, command);
        assertEquals(command, new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON));
        assertNotEquals(command, new NoteDeleteCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON));
        assertNotEquals(command, new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
        assertNotEquals(command, null);
        assertNotEquals(command, "Text");
    }
}
