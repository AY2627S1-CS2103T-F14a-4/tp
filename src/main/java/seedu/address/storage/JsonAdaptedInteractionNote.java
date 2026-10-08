package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.InteractionNote;

/** JSON representation of a dated interaction note. */
class JsonAdaptedInteractionNote {
    private final String date;
    private final String text;

    /** Creates an adapter from stored JSON fields. */
    @JsonCreator
    public JsonAdaptedInteractionNote(@JsonProperty("date") String date, @JsonProperty("text") String text) {
        this.date = date;
        this.text = text;
    }

    /** Creates an adapter from a model note. */
    public JsonAdaptedInteractionNote(InteractionNote note) {
        this(note.getDate().toString(), note.getText());
    }

    /** Converts stored data using the same validation as command input. */
    public InteractionNote toModelType() throws IllegalValueException {
        if (text == null) {
            throw new IllegalValueException(InteractionNote.MESSAGE_BLANK);
        }
        try {
            return new InteractionNote(InteractionNote.parseDate(date), text);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}
