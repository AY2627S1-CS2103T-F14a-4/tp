package seedu.address.model.person;

/** Represents a contact's relationship category. */
public enum Relationship {
    CLIENT("Client"),
    PROSPECT("Prospect");

    public static final String MESSAGE_CONSTRAINTS = "Relationship must be either Client or Prospect.";
    private final String displayName;

    Relationship(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the matching relationship, ignoring case. */
    public static Relationship fromString(String value) {
        for (Relationship relationship : values()) {
            if (relationship.displayName.equalsIgnoreCase(value.trim())) {
                return relationship;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
