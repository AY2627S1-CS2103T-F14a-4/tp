package seedu.address.model.person;

import java.util.Locale;

/** The kind of interaction; UNSPECIFIED retains the absence of historical information. */
public enum InteractionType {
    UNSPECIFIED, MEETING, CALL, MESSAGE, OTHER;

    public static final String MESSAGE_CONSTRAINTS =
            "Interaction type must be MEETING, CALL, MESSAGE, OTHER, or UNSPECIFIED.";

    /** Parses a case-insensitive value without depending on the computer's locale. */
    public static InteractionType parse(String value) {
        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
    }
}
