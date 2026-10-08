package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/** An immutable dated interaction note. Dates need not be recent or unique. */
public class InteractionNote {
    public static final String MESSAGE_INVALID_DATE = "Interaction date must be a valid date in yyyy-MM-dd format.";
    public static final String MESSAGE_BLANK = "Interaction note cannot be empty.";

    private final LocalDate date;
    private final String text;

    /** Creates a note with nonblank text, preserving its content and case. */
    public InteractionNote(LocalDate date, String text) {
        requireAllNonNull(date, text);
        checkArgument(!text.isBlank(), MESSAGE_BLANK);
        this.date = date;
        this.text = text.strip();
    }

    /** Parses a real calendar date in the documented four-digit-year format. */
    public static LocalDate parseDate(String value) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException(MESSAGE_INVALID_DATE);
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(MESSAGE_INVALID_DATE, e);
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof InteractionNote note
                && date.equals(note.date) && text.equals(note.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, text);
    }

    @Override
    public String toString() {
        return date + ": " + text;
    }
}
