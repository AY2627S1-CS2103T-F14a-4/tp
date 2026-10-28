package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.RecurringReminder;

/** JSON representation of a labelled yearly date. */
class JsonAdaptedRecurringReminder {
    private final String label;
    private final String date;

    /** Retains raw JSON fields for validation when converting to the model. */
    @JsonCreator
    public JsonAdaptedRecurringReminder(@JsonProperty("label") String label, @JsonProperty("date") String date) {
        this.label = label;
        this.date = date;
    }

    /** Converts a model reminder into portable month/day JSON fields. */
    public JsonAdaptedRecurringReminder(RecurringReminder source) {
        this(source.getLabel(), source.getDate().toString().substring(2));
    }

    /** Validates saved labels and dates before creating the model value. */
    public RecurringReminder toModelType() throws IllegalValueException {
        if (label == null) {
            throw new IllegalValueException(RecurringReminder.MESSAGE_LABEL_CONSTRAINTS);
        }
        try {
            return new RecurringReminder(label, RecurringReminder.parseDate(date));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage(), exception);
        }
    }
}
