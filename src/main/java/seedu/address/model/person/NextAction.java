package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** An immutable follow-up, with a description, due date and completion status. */
public class NextAction {
    public static final String MESSAGE_ACTION_CONSTRAINTS = "Next action must contain 1 to 200 characters.";
    public static final String MESSAGE_INVALID_DATE = "Due date must be a valid date in yyyy-MM-dd format.";

    private final String action;
    private final LocalDate dueDate;
    private final boolean completed;

    /** Creates a follow-up after stripping surrounding whitespace from its description. */
    public NextAction(String action, LocalDate dueDate, boolean completed) {
        requireAllNonNull(action, dueDate);
        String trimmedAction = action.strip();
        checkArgument(!trimmedAction.isEmpty() && trimmedAction.length() <= 200, MESSAGE_ACTION_CONSTRAINTS);
        checkArgument(dueDate.getYear() >= 1 && dueDate.getYear() <= 9999, MESSAGE_INVALID_DATE);
        this.action = trimmedAction;
        this.dueDate = dueDate;
        this.completed = completed;
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
