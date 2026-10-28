package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** An immutable labelled yearly date, independent of a person's one-off next action. */
public class RecurringReminder {
    public static final String MESSAGE_LABEL_CONSTRAINTS =
            "Reminder label must contain 1 to 50 characters, without control characters or slashes.";
    public static final String MESSAGE_INVALID_DATE = "Yearly date must be a valid date in MM-dd format.";

    private final String label;
    private final MonthDay date;

    /** Creates a yearly reminder after stripping surrounding label whitespace. */
    public RecurringReminder(String label, MonthDay date) {
        requireAllNonNull(label, date);
        String trimmed = label.strip();
        checkArgument(!trimmed.isEmpty() && trimmed.length() <= 50 && !trimmed.contains("/")
                && trimmed.chars().noneMatch(Character::isISOControl), MESSAGE_LABEL_CONSTRAINTS);
        this.label = trimmed;
        this.date = date;
    }

    /** Parses exactly two month digits and two day digits; February 29 is supported. */
    public static MonthDay parseDate(String value) {
        if (value == null || !value.matches("[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException(MESSAGE_INVALID_DATE);
        }
        try {
            return MonthDay.parse("--" + value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(MESSAGE_INVALID_DATE, exception);
        }
    }

    public String getLabel() {
        return label;
    }

    public MonthDay getDate() {
        return date;
    }

    /** Returns the next occurrence including today; February 29 becomes February 28 in non-leap years. */
    public LocalDate nextOccurrence(LocalDate today) {
        requireAllNonNull(today);
        LocalDate occurrence = date.atYear(today.getYear());
        return occurrence.isBefore(today) ? date.atYear(today.getYear() + 1) : occurrence;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RecurringReminder reminder
                && label.equals(reminder.label) && date.equals(reminder.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("label", label).add("date", date).toString();
    }
}
