package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final String COMMAND_PREFIX = "Command: ";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Displays the command entered by the user followed by its execution feedback.
     */
    public void setFeedbackToUser(String commandText, String feedbackToUser) {
        requireNonNull(commandText);
        requireNonNull(feedbackToUser);
        resultDisplay.setText(formatCommandFeedback(commandText, feedbackToUser));
    }

    static String formatCommandFeedback(String commandText, String feedbackToUser) {
        requireNonNull(commandText);
        requireNonNull(feedbackToUser);
        return COMMAND_PREFIX + commandText + System.lineSeparator() + feedbackToUser;
    }

}
