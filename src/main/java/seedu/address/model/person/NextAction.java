package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** An immutable follow-up, with a description, due date and completion status. */
public class NextAction {
    public static final int MAX_ACTION_LENGTH = 200;
    public static final int MAX_DUE_YEAR = 9999;
    public static final String MESSAGE_ACTION_CONSTRAINTS = "Next action must contain 1 to 200 characters.";
    public static final String MESSAGE_INVALID_DATE = "Due date must be a valid date in yyyy-MM-dd format.";

    private final String action;
    private final LocalDate dueDate;
    private final boolean completed;

    /** Creates a follow-up after stripping surrounding whitespace from its description. */
    public NextAction(String action, LocalDate dueDate, boolean completed) {
        requireAllNonNull(action, dueDate);
        String normalizedAction = parseAction(action);
        checkArgument(dueDate.getYear() >= 1 && dueDate.getYear() <= MAX_DUE_YEAR, MESSAGE_INVALID_DATE);
        this.action = normalizedAction;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    /** Validates and strips surrounding whitespace from a follow-up description. */
    public static String parseAction(String action) {
        requireAllNonNull(action);
        String trimmedAction = action.strip();
        checkArgument(!trimmedAction.isEmpty() && trimmedAction.length() <= MAX_ACTION_LENGTH,
                MESSAGE_ACTION_CONSTRAINTS);
        return trimmedAction;
    }

    /** Parses a valid date in years 0001 to 9999 with exactly two month/day digits. */
    public static LocalDate parseDate(String value) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException(MESSAGE_INVALID_DATE);
        }
        try {
            LocalDate date = LocalDate.parse(value);
            checkArgument(date.getYear() >= 1, MESSAGE_INVALID_DATE);
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(MESSAGE_INVALID_DATE, e);
        }
    }

    public String getAction() {
        return action;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    /** Returns a completed copy with the same description and due date. */
    public NextAction markCompleted() {
        return new NextAction(action, dueDate, true);
    }

    /** Returns a pending copy with a new date, preserving an omitted description. */
    public NextAction reschedule(LocalDate dueDate, String action) {
        return new NextAction(action == null ? this.action : action, dueDate, false);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NextAction nextAction
                && action.equals(nextAction.action) && dueDate.equals(nextAction.dueDate)
                && completed == nextAction.completed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, dueDate, completed);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("action", action)
                .add("dueDate", dueDate)
                .add("completed", completed)
                .toString();
    }

}
