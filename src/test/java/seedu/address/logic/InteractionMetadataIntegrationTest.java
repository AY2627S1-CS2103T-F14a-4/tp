package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.InteractionOutcome;
import seedu.address.model.person.InteractionType;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class InteractionMetadataIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private LogicManager logic;
    private JsonClientBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        storage = new JsonClientBookStorage(temporaryFolder.resolve("clientbook.json"));
        storage.saveClientBook(model.getClientBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_optionalMetadataAndLegacySyntax_persistInStableChronologicalOrder() throws Exception {
        logic.execute("note 1 d/2000-01-02 t/call o/unsuccessful n/No answer");
        logic.execute("note 1 d/2000-01-01 n/Legacy syntax");
        logic.execute("note 1 d/2000-01-02 t/message n/Sent proposal");
        logic.execute("note 1 d/2000-01-02 o/pending n/Awaiting response");
        List<InteractionNote> expected = List.of(new InteractionNote(LocalDate.of(2000, 1, 1), "Legacy syntax"),
                new InteractionNote(LocalDate.of(2000, 1, 2), "No answer", InteractionType.CALL,
                        InteractionOutcome.UNSUCCESSFUL),
                new InteractionNote(LocalDate.of(2000, 1, 2), "Sent proposal", InteractionType.MESSAGE,
                        InteractionOutcome.UNSPECIFIED),
                new InteractionNote(LocalDate.of(2000, 1, 2), "Awaiting response", InteractionType.UNSPECIFIED,
                        InteractionOutcome.PENDING));
        assertEquals(expected, notes());
        Model reopened = new ModelManager(storage.readClientBook().orElseThrow(), new UserPrefs());
        assertEquals(expected, reopened.getClientBook().getPersonList().getFirst().getInteractionNotes());
        assertEquals(model.getClientBook(), reopened.getClientBook());
    }

    @Test
    public void execute_noteEditsAndDeletion_preserveMetadataWhileReorderingAndRemovingSelectedNote() throws Exception {
        logic.execute("note 1 d/2000-01-01 t/CALL o/UNSUCCESSFUL n/No answer");
        logic.execute("note 1 d/2000-01-02 t/MEETING o/SUCCESSFUL n/Met client");
        InteractionNote meeting = notes().get(1);
        logic.execute("noteedit 1 1 d/2000-01-03");
        assertEquals(meeting, notes().getFirst());
        assertEquals(new InteractionNote(LocalDate.of(2000, 1, 3), "No answer", InteractionType.CALL,
                InteractionOutcome.UNSUCCESSFUL), notes().get(1));
        logic.execute("noteedit 1 2 n/Left voicemail");
        InteractionNote edited = new InteractionNote(LocalDate.of(2000, 1, 3), "Left voicemail", InteractionType.CALL,
                InteractionOutcome.UNSUCCESSFUL);
        assertEquals(List.of(meeting, edited), notes());
        logic.execute("notedelete 1 1");
        assertEquals(List.of(edited), notes());
        assertEquals(model.getClientBook(), storage.readClientBook().orElseThrow());
    }

    @Test
    public void execute_contactMutationsAndBackupRestore_preserveMetadata() throws Exception {
        logic.execute("note 1 d/2000-01-01 t/OTHER o/PENDING n/Follow up later");
        List<InteractionNote> expected = List.copyOf(notes());
        for (String command : List.of("edit 1 p/81234567", "info 1 fn/Retirement", "info 1 fn/-",
                "contact 1 ch/Email tm/Morning", "contact 1 ch/- tm/-", "tag 1 priority",
                "next 1 a/Call d/9999-12-31", "done 1", "reschedule 1 d/9999-12-31")) {
            logic.execute(command);
            assertEquals(expected, notes(), command);
            assertEquals(model.getClientBook(), storage.readClientBook().orElseThrow(), command);
        }
        logic.execute("tagdelete 1 priority");
        logic.execute("yes");
        assertEquals(expected, notes());
        logic.execute("backup");
        logic.execute("notedelete 1 1");
        assertTrue(notes().isEmpty());
        logic.execute("restore");
        assertEquals(expected, notes());
        assertEquals(model.getClientBook(), storage.readClientBook().orElseThrow());
    }

    @Test
    public void execute_filteredIndices_targetOriginalContactAndRejectHiddenContact() throws Exception {
        logic.execute("find Alice Carl");
        logic.execute("note 3 d/2000-01-01 t/MEETING o/SUCCESSFUL n/Reviewed plan");
        String saved = Files.readString(storage.getClientBookFilePath());
        assertThrows(CommandException.class, () ->
                logic.execute("note 2 d/2000-01-01 t/CALL o/PENDING n/Hidden contact"));
        assertTrue(notes().isEmpty());
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(InteractionType.MEETING,
                model.getFilteredPersonList().get(1).getInteractionNotes().getFirst().getType());
        assertEquals(saved, Files.readString(storage.getClientBookFilePath()));
        logic.execute("noteedit 3 1 n/Updated plan");
        assertEquals(InteractionOutcome.SUCCESSFUL,
                model.getFilteredPersonList().get(1).getInteractionNotes().getFirst().getOutcome());
        logic.execute("notedelete 3 1");
        assertTrue(model.getFilteredPersonList().get(1).getInteractionNotes().isEmpty());
    }

    @Test
    public void execute_duplicateContentAndInvalidMetadata_leaveFileAndModelUnchanged() throws Exception {
        logic.execute("note 1 d/2000-01-01 t/CALL o/UNSUCCESSFUL n/No answer");
        List<InteractionNote> expected = List.copyOf(notes());
        String saved = Files.readString(storage.getClientBookFilePath());
        for (String metadata : List.of("", "t/MEETING o/SUCCESSFUL", "t/call o/unsuccessful")) {
            CommandException error = assertThrows(CommandException.class, () ->
                    logic.execute("note 1 d/2000-01-01 " + metadata + " n/ No answer "));
            assertEquals(NoteCommand.MESSAGE_DUPLICATE_NOTE, error.getMessage());
        }
        for (String metadata : List.of("t/PHONE", "o/FAILED", "t/", "o/", "t/CALL t/OTHER")) {
            assertThrows(ParseException.class, () -> logic.execute("note 1 d/2000-01-01 " + metadata + " n/Other"));
        }
        assertThrows(CommandException.class, () ->
                logic.execute("note 999 d/2000-01-01 t/CALL o/PENDING n/Text"));
        assertThrows(CommandException.class, () ->
                logic.execute("note 1 d/" + LocalDate.now().plusDays(1) + " t/CALL o/PENDING n/Future"));
        assertEquals(expected, notes());
        assertEquals(saved, Files.readString(storage.getClientBookFilePath()));
        // Date-and-text rules still allow a different date, different text case, and another contact.
        logic.execute("note 1 d/2000-01-02 t/CALL o/PENDING n/No answer");
        logic.execute("note 1 d/2000-01-01 t/CALL o/PENDING n/no answer");
        logic.execute("note 2 d/2000-01-01 t/CALL o/PENDING n/No answer");
        assertEquals(3, notes().size());
    }

    @Test
    public void execute_noteEdit_existingDuplicateBehaviourIsUnchanged() throws Exception {
        logic.execute("note 1 d/2000-01-01 t/CALL o/PENDING n/First");
        logic.execute("note 1 d/2000-01-01 t/MEETING o/SUCCESSFUL n/Second");
        logic.execute("noteedit 1 2 n/First");
        assertEquals(2, notes().size());
        assertEquals(InteractionType.MEETING, notes().get(1).getType());
        assertEquals(InteractionOutcome.SUCCESSFUL, notes().get(1).getOutcome());
    }

    private List<InteractionNote> notes() {
        return model.getClientBook().getPersonList().getFirst().getInteractionNotes();
    }

    @Test
    public void storage_legacyFileWithNotes_loadsAndSavesWithoutLosingHistory() throws Exception {
        Path legacyPath = temporaryFolder.resolve("legacy.json");
        Files.writeString(legacyPath, "{\"persons\":[{\"name\":\"Legacy Client\",\"phone\":\"81234567\","
                + "\"email\":\"legacy@example.com\",\"interactionNotes\":[{\"date\":\"2000-01-01\","
                + "\"text\":\"Old note\"},{\"date\":\"2000-01-02\",\"text\":\"Old note\"}]}]}");
        JsonClientBookStorage legacyStorage = new JsonClientBookStorage(legacyPath);
        Model loaded = new ModelManager(legacyStorage.readClientBook().orElseThrow(), new UserPrefs());
        List<InteractionNote> expected = List.of(new InteractionNote(LocalDate.of(2000, 1, 1), "Old note"),
                new InteractionNote(LocalDate.of(2000, 1, 2), "Old note"));
        assertEquals(expected, loaded.getClientBook().getPersonList().getFirst().getInteractionNotes());
        legacyStorage.saveClientBook(loaded.getClientBook());
        assertEquals(loaded.getClientBook(), legacyStorage.readClientBook().orElseThrow());
    }
}
