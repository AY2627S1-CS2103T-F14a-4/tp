package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.InteractionNote;

public class JsonAdaptedInteractionNoteTest {
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
