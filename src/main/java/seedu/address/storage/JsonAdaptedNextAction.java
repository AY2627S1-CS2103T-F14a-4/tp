package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.NextAction;

/** JSON representation of a follow-up. */
class JsonAdaptedNextAction {
    private final String action;
    private final String dueDate;
    private final Boolean completed;

    /** Creates an adapter from stored fields, retaining missing values for validation. */
    @JsonCreator
    public JsonAdaptedNextAction(@JsonProperty("action") String action,
            @JsonProperty("dueDate") String dueDate, @JsonProperty("completed") Boolean completed) {
        this.action = action;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    /** Creates an adapter from a model follow-up. */
    public JsonAdaptedNextAction(NextAction source) {
        this(source.getAction(), source.getDueDate().toString(), source.isCompleted());
    }

    /** Validates stored fields before creating the model object. */
    public NextAction toModelType() throws IllegalValueException {
        if (action == null) {
            throw new IllegalValueException(NextAction.MESSAGE_ACTION_CONSTRAINTS);
        }
        if (completed == null) {
            throw new IllegalValueException("Next action completion status is required.");
        }
        try {
            return new NextAction(action, NextAction.parseDate(dueDate), completed);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }
}
