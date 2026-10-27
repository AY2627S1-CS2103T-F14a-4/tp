package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.format.DateTimeParseException;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.InteractionNote;

public class JsonAdaptedInteractionNoteTest {
    @Test
    public void toModelType_yearZero_rejected() {
        JsonAdaptedInteractionNote note = new JsonAdaptedInteractionNote("0000-01-01", "Called");
        assertThrows(IllegalValueException.class, InteractionNote.MESSAGE_INVALID_DATE, note::toModelType);
    }

    @Test
    public void toModelType_boundaryYears_roundTrip() throws Exception {
        for (String date : List.of("0001-01-01", "9999-12-31")) {
            InteractionNote note = new JsonAdaptedInteractionNote(date, "Called").toModelType();
            assertEquals(date, note.getDate().toString());
            assertEquals(note, new JsonAdaptedInteractionNote(note).toModelType());
        }
    }

    @Test
    public void toModelType_invalidDate_preservesValidationCause() {
        JsonAdaptedInteractionNote note = new JsonAdaptedInteractionNote("2026-02-30", "Called");

        assertThrows(IllegalValueException.class, InteractionNote.MESSAGE_INVALID_DATE, () -> {
            try {
                note.toModelType();
            } catch (IllegalValueException e) {
                IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, e.getCause());
                assertEquals(InteractionNote.MESSAGE_INVALID_DATE, cause.getMessage());
                assertInstanceOf(DateTimeParseException.class, cause.getCause());
                throw e;
            }
        });
    }
}
