package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** The command has already persisted the model. */
    private final boolean modelSaved;

    /** The first displayed person should be selected. */
    private final boolean selectFirstResult;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, false, false);
    }

    /**
     * Constructs a {@code CommandResult} with the specified display and persistence state.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean modelSaved) {
        this(feedbackToUser, showHelp, exit, modelSaved, false);
    }

    /**
     * Constructs a {@code CommandResult} with the specified display, persistence, and selection state.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean modelSaved,
            boolean selectFirstResult) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.modelSaved = modelSaved;
        this.selectFirstResult = selectFirstResult;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    /**
     * Returns whether the command has already saved the current model.
     */
    public boolean isModelSaved() {
        return modelSaved;
    }

    /** Returns whether the first displayed person should be selected. */
    public boolean shouldSelectFirstResult() {
        return selectFirstResult;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && modelSaved == otherCommandResult.modelSaved
                && selectFirstResult == otherCommandResult.selectFirstResult;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, modelSaved, selectFirstResult);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("modelSaved", modelSaved)
                .add("selectFirstResult", selectFirstResult)
                .toString();
    }

}
