package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.format.DateTimeParseException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.InteractionOutcome;
import seedu.address.model.person.InteractionType;

public class JsonAdaptedInteractionNoteTest {
    @Test
    public void json_metadataRoundTripAndCanonicalValues() throws Exception {
        InteractionNote note = new JsonAdaptedInteractionNote("2000-01-01", "Called", "cAlL", "unsuccessful")
                .toModelType();
        assertEquals(InteractionType.CALL, note.getType());
        assertEquals(InteractionOutcome.UNSUCCESSFUL, note.getOutcome());
        String json = JsonUtil.toJsonString(new JsonAdaptedInteractionNote(note));
        JsonNode fields = JsonUtil.fromJsonString(json, JsonNode.class);
        assertEquals("CALL", fields.get("type").asText());
        assertEquals("UNSUCCESSFUL", fields.get("outcome").asText());
        assertEquals(note, JsonUtil.fromJsonString(json, JsonAdaptedInteractionNote.class).toModelType());
    }

    @Test
    public void json_legacyMissingNullAndPartialMetadata_loadWithoutInventingDetails() throws Exception {
        for (String fields : List.of("", ",\"type\":null,\"outcome\":null")) {
            String json = "{\"date\":\"2000-01-01\",\"text\":\"Called\"" + fields + "}";
            assertEquals(new InteractionNote(java.time.LocalDate.of(2000, 1, 1), "Called"),
                    JsonUtil.fromJsonString(json, JsonAdaptedInteractionNote.class).toModelType());
        }
        InteractionNote partial = new JsonAdaptedInteractionNote("2000-01-01", "Called", "MESSAGE", null)
                .toModelType();
        assertEquals(InteractionType.MESSAGE, partial.getType());
        assertEquals(InteractionOutcome.UNSPECIFIED, partial.getOutcome());
        assertEquals(InteractionType.UNSPECIFIED,
                new JsonAdaptedInteractionNote("2000-01-01", "Called", null, "PENDING").toModelType().getType());
    }

    @Test
    public void toModelType_invalidMetadata_rejectedWithoutSilentlyDiscardingIt() {
        for (String type : List.of("", "PHONE", "CALL MEETING")) {
            assertThrows(IllegalValueException.class, InteractionType.MESSAGE_CONSTRAINTS, () ->
                    new JsonAdaptedInteractionNote("2000-01-01", "Called", type, null).toModelType());
        }
        for (String outcome : List.of("", "FAILURE", "PENDING SUCCESSFUL")) {
            assertThrows(IllegalValueException.class, InteractionOutcome.MESSAGE_CONSTRAINTS, () ->
                    new JsonAdaptedInteractionNote("2000-01-01", "Called", null, outcome).toModelType());
        }
    }

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
