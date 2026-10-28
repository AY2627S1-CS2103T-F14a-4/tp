package seedu.address.model.person;

import java.util.Locale;

/** The recorded result of an interaction, without inferring a result when none was supplied. */
public enum InteractionOutcome {
    UNSPECIFIED, SUCCESSFUL, UNSUCCESSFUL, PENDING;

    public static final String MESSAGE_CONSTRAINTS =
            "Interaction outcome must be SUCCESSFUL, UNSUCCESSFUL, PENDING, or UNSPECIFIED.";

    /** Parses a case-insensitive value without depending on the computer's locale. */
    public static InteractionOutcome parse(String value) {
        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
    }
}
