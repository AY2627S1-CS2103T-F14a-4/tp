package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final String COMMAND_PREFIX = "Command: ";
    private static final int MINIMUM_ROW_COUNT = 2;
    private static final int MAXIMUM_ROW_COUNT = 4;
    private static final double CONTENT_HEIGHT_PADDING = 14.0;

    @FXML
    private TextArea resultDisplay;

    private final double[] preferredHeightsByRowCount = new double[MAXIMUM_ROW_COUNT + 1];

    /** Creates a result display that remains hidden until feedback is available. */
    public ResultDisplay() {
        super(FXML);
        getRoot().setVisible(false);
        getRoot().setManaged(false);
    }

    /**
     * Displays the command entered by the user followed by its execution feedback.
     */
    public void setFeedbackToUser(String commandText, String feedbackToUser) {
        requireNonNull(commandText);
        requireNonNull(feedbackToUser);
        String commandFeedback = formatCommandFeedback(commandText, feedbackToUser);
        getRoot().setManaged(true);
        getRoot().setVisible(true);
        setDisplayedRows(commandFeedback, calculateDisplayedRowCount(commandFeedback));
    }

    static int calculateDisplayedRowCount(String commandFeedback) {
        requireNonNull(commandFeedback);
        int rowCount = commandFeedback.split("\\R", -1).length;
        return Math.clamp(rowCount, MINIMUM_ROW_COUNT, MAXIMUM_ROW_COUNT);
    }

    private void setDisplayedRows(String commandFeedback, int rowCount) {
        String[] rows = commandFeedback.split("\\R", -1);
        String displayedRows = String.join("\n", java.util.Arrays.copyOf(rows, rowCount));
        resultDisplay.setText(displayedRows);
        resultDisplay.setMinHeight(Region.USE_COMPUTED_SIZE);
        resultDisplay.setPrefHeight(Region.USE_COMPUTED_SIZE);
        resultDisplay.setMaxHeight(Region.USE_COMPUTED_SIZE);
        resultDisplay.setPrefRowCount(rowCount);
        resultDisplay.applyCss();

        Region content = (Region) resultDisplay.lookup(".content");
        ScrollPane scrollPane = (ScrollPane) resultDisplay.lookup(".scroll-pane");
        Text renderedRows = new Text(displayedRows);
        renderedRows.setFont(resultDisplay.getFont());
        renderedRows.setTextOrigin(VPos.TOP);
        double viewportHeight = Math.ceil(renderedRows.getLayoutBounds().getHeight()
                + content.getInsets().getTop() + content.getInsets().getBottom() + CONTENT_HEIGHT_PADDING);
        scrollPane.setPrefViewportHeight(viewportHeight);

        double preferredHeight = Math.ceil(viewportHeight
                + resultDisplay.getInsets().getTop() + resultDisplay.getInsets().getBottom()
                + scrollPane.getInsets().getTop() + scrollPane.getInsets().getBottom());
        if (preferredHeightsByRowCount[rowCount] == 0) {
            preferredHeightsByRowCount[rowCount] = preferredHeight;
        } else {
            preferredHeight = preferredHeightsByRowCount[rowCount];
        }
        resultDisplay.setMinHeight(preferredHeight);
        resultDisplay.setPrefHeight(preferredHeight);
        resultDisplay.setMaxHeight(preferredHeight);
        resultDisplay.setText(commandFeedback);
    }

    static String formatCommandFeedback(String commandText, String feedbackToUser) {
        requireNonNull(commandText);
        requireNonNull(feedbackToUser);
        return COMMAND_PREFIX + commandText + System.lineSeparator() + feedbackToUser;
    }

}
