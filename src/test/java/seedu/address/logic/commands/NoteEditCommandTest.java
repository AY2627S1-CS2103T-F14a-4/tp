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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

public class NoteEditCommandTest {
    private static final InteractionNote NOTE = new InteractionNote(LocalDate.of(2000, 2, 29), "Called");
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_duplicateNotes_preservesDateOtherNotesAndAllContactFields() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withRelationship(Relationship.CLIENT).withTags("client", "friend").withFinancialNeed("Retirement")
                .withPriority("High").withAreaOfInterest("Insurance")
                .withPreferredContactChannel("Email").withPreferredContactTiming("Evenings")
                .withNextAction(new NextAction("Call", LocalDate.of(2026, 10, 20), true))
                .withInteractionNotes(NOTE, NOTE, NOTE).build();
        model.setPerson(model.getFilteredPersonList().get(0), original);

        CommandResult result = new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, " Updated ")
                .execute(model);

        Person expected = new PersonBuilder(original).withInteractionNotes(NOTE,
                new InteractionNote(NOTE.getDate(), "Updated"), NOTE).build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals("Updated interaction note 2 for " + original.getName().fullName + ".",
                result.getFeedbackToUser());
        assertEquals(NOTE, original.getInteractionNotes().get(1));
    }

    @Test
    public void execute_filteredList_updatesDisplayedProspectAndPreservesFilter() throws Exception {
        Person untouched = model.getFilteredPersonList().get(0);
        Person original = new PersonBuilder(model.getFilteredPersonList().get(1))
                .withRelationship(Relationship.PROSPECT).withInteractionNotes(NOTE).build();
        model.setPerson(model.getFilteredPersonList().get(1), original);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Updated").execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(original).withInteractionNotes(
                new InteractionNote(NOTE.getDate(), "Updated")).build(), model.getFilteredPersonList().get(0));
        assertEquals(untouched, model.getAddressBook().getPersonList().get(0));
        assertCommandFailure(new NoteEditCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON, "Updated"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyContactList_rejectsContactIndex() {
        assertCommandFailure(new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Updated"),
                new ModelManager(), Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_noNotesOrOutOfRangeNote_rejectsWithoutChanges() {
        assertCommandFailure(new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Updated"), model,
                NoteEditCommand.MESSAGE_INVALID_NOTE_INDEX);
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withInteractionNotes(NOTE).build());
        assertCommandFailure(new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, "Updated"), model,
                NoteEditCommand.MESSAGE_INVALID_NOTE_INDEX);
    }

    @Test
    public void execute_throughLogic_savedEditSurvivesReopening() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withRelationship(Relationship.CLIENT).withTags("client", "friend")
                .withFinancialNeed("Retirement").withPriority("High").withAreaOfInterest("Insurance")
                .withPreferredContactChannel("Email").withPreferredContactTiming("Evenings")
                .withNextAction(new NextAction("Call", LocalDate.of(2026, 10, 20), false)).build();
        model.setPerson(model.getFilteredPersonList().get(0),
                new PersonBuilder(original).withInteractionNotes(NOTE, NOTE).build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        logic.execute("noteedit 1 2 n/Updated");

        Model reopened = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reopened.getAddressBook());
        assertEquals(new PersonBuilder(original).withInteractionNotes(NOTE,
                new InteractionNote(NOTE.getDate(), "Updated")).build(), reopened.getFilteredPersonList().get(0));
    }

    @Test
    public void constructor_invalidArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new NoteEditCommand(null, INDEX_FIRST_PERSON, "Text"));
        assertThrows(NullPointerException.class, () -> new NoteEditCommand(INDEX_FIRST_PERSON, null, "Text"));
        assertThrows(NullPointerException.class, () ->
                new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null));
        assertThrows(IllegalArgumentException.class, InteractionNote.MESSAGE_BLANK, () ->
                new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, " \t "));
    }

    @Test
    public void equals_comparesBothIndicesAndText() {
        NoteEditCommand command = new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Text");
        assertEquals(command, command);
        assertEquals(command, new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Text"));
        assertNotEquals(command, new NoteEditCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON, "Text"));
        assertNotEquals(command, new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, "Text"));
        assertNotEquals(command, new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Other"));
        assertNotEquals(command, null);
        assertNotEquals(command, "Text");
    }
}
