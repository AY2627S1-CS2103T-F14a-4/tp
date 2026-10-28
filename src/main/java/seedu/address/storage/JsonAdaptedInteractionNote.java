package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.InteractionOutcome;
import seedu.address.model.person.InteractionType;

/** JSON representation of a dated interaction note. */
class JsonAdaptedInteractionNote {
    private final String date;
    private final String text;
    private final String type;
    private final String outcome;

    /** Creates an adapter from stored JSON fields. */
    public JsonAdaptedInteractionNote(String date, String text) {
        this(date, text, null, null);
    }

    /** Missing metadata in legacy JSON represents an unspecified type or outcome. */
    @JsonCreator
    public JsonAdaptedInteractionNote(@JsonProperty("date") String date, @JsonProperty("text") String text,
            @JsonProperty("type") String type, @JsonProperty("outcome") String outcome) {
        this.date = date;
        this.text = text;
        this.type = type;
        this.outcome = outcome;
    }

    /** Creates an adapter from a model note. */
    public JsonAdaptedInteractionNote(InteractionNote note) {
        this(note.getDate().toString(), note.getText(), note.getType().name(), note.getOutcome().name());
    }

    /** Converts stored data using the same validation as command input. */
    public InteractionNote toModelType() throws IllegalValueException {
        if (text == null) {
            throw new IllegalValueException(InteractionNote.MESSAGE_BLANK);
        }
        try {
            return new InteractionNote(InteractionNote.parseDate(date), text,
                    type == null ? InteractionType.UNSPECIFIED : InteractionType.parse(type),
                    outcome == null ? InteractionOutcome.UNSPECIFIED : InteractionOutcome.parse(outcome));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }
}
