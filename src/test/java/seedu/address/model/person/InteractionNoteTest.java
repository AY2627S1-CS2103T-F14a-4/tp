package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class InteractionNoteTest {
    @Test
    public void parseDate_realDatesOnly() {
        assertEquals(LocalDate.of(2000, 2, 29), InteractionNote.parseDate("2000-02-29"));
        for (String invalid : List.of("2026-02-30", "2026-13-01", "2026-2-01", "06-10-2026", "")) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                    InteractionNote.parseDate(invalid));
            assertEquals(InteractionNote.MESSAGE_INVALID_DATE, error.getMessage());
        }
    }

    @Test
    public void text_realisticContentPreserved_blankRejected() {
        String text = "客户's plan — £100; https://example.com/a/b n/example d/2026-01-01";
        InteractionNote note = new InteractionNote(LocalDate.of(2000, 1, 1), "  " + text + "  ");
        assertEquals(text, note.getText());
        assertEquals(note, new InteractionNote(note.getDate(), text));
        assertNotEquals(note, new InteractionNote(note.getDate(), text.toUpperCase()));
        assertThrows(IllegalArgumentException.class, () -> new InteractionNote(note.getDate(), " \t "));
    }

    @Test
    public void person_historyIsImmutableAndIncludedInEquality() {
        InteractionNote note = new InteractionNote(LocalDate.of(2000, 1, 1), "Discussed retirement");
        List<InteractionNote> source = new ArrayList<>(List.of(note, note));
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getTags(),
                ALICE.getRelationship(), null, null, null, source);
        source.clear();
        assertEquals(List.of(note, note), person.getInteractionNotes());
        assertThrows(UnsupportedOperationException.class, () -> person.getInteractionNotes().clear());
        assertEquals(person, new PersonBuilder(person).build());
        assertNotEquals(ALICE, person);
    }
}
