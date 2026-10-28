package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.application.Platform;
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

    /** Creates a result display that remains hidden until feedback is available. */
    public ResultDisplay() {
        super(FXML);
        getRoot().setVisible(false);
        getRoot().setManaged(false);
        resultDisplay.widthProperty().addListener((observable, oldWidth, newWidth) ->
                Platform.runLater(this::resizeToWrappedText));
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
        Platform.runLater(this::resizeToWrappedText);
    }

    static int calculateDisplayedRowCount(String commandFeedback) {
        requireNonNull(commandFeedback);
        int rowCount = commandFeedback.split("\\R", -1).length;
        return Math.clamp(rowCount, MINIMUM_ROW_COUNT, MAXIMUM_ROW_COUNT);
    }

    private void setDisplayedRows(String commandFeedback, int rowCount) {
        String displayedRows = "X\n".repeat(rowCount - 1) + "X";
        resultDisplay.setText(displayedRows);
        resultDisplay.setMinHeight(Region.USE_COMPUTED_SIZE);
        resultDisplay.setPrefHeight(Region.USE_COMPUTED_SIZE);
        resultDisplay.setMaxHeight(Region.USE_COMPUTED_SIZE);
        resultDisplay.setPrefRowCount(rowCount);
        resultDisplay.applyCss();

        Region content = (Region) resultDisplay.lookup(".content");
        ScrollPane scrollPane = (ScrollPane) resultDisplay.lookup(".scroll-pane");
        int logicalRowCount = commandFeedback.split("\\R", -1).length;
        double averageRowHeight = createText(commandFeedback).getLayoutBounds().getHeight() / logicalRowCount;
        double viewportHeight = Math.ceil(averageRowHeight * rowCount
                + content.getInsets().getTop() + content.getInsets().getBottom() + CONTENT_HEIGHT_PADDING);
        scrollPane.setPrefViewportHeight(viewportHeight);

        double preferredHeight = Math.ceil(viewportHeight
                + resultDisplay.getInsets().getTop() + resultDisplay.getInsets().getBottom()
                + scrollPane.getInsets().getTop() + scrollPane.getInsets().getBottom());
        resultDisplay.setMinHeight(preferredHeight);
        resultDisplay.setPrefHeight(preferredHeight);
        resultDisplay.setMaxHeight(preferredHeight);
        resultDisplay.setText(commandFeedback);
    }

    private void resizeToWrappedText() {
        if (resultDisplay.getText().isEmpty() || resultDisplay.getWidth() <= 0) {
            return;
        }
        resultDisplay.applyCss();
        Region content = (Region) resultDisplay.lookup(".content");
        double wrappingWidth = content.getWidth() - content.getInsets().getLeft() - content.getInsets().getRight();
        if (wrappingWidth <= 0) {
            return;
        }

        int logicalRowCount = resultDisplay.getText().split("\\R", -1).length;
        Text unwrappedText = createText(resultDisplay.getText());
        Text wrappedText = createText(resultDisplay.getText());
        wrappedText.setWrappingWidth(wrappingWidth);
        double averageRowHeight = unwrappedText.getLayoutBounds().getHeight() / logicalRowCount;
        double wrappedRowsHeight = wrappedText.getLayoutBounds().getHeight()
                - unwrappedText.getLayoutBounds().getHeight();
        int additionalRowCount = (int) Math.ceil(wrappedRowsHeight / averageRowHeight - 0.01);
        int wrappedRowCount = logicalRowCount + additionalRowCount;
        setDisplayedRows(resultDisplay.getText(), Math.clamp(wrappedRowCount, MINIMUM_ROW_COUNT, MAXIMUM_ROW_COUNT));
    }

    private Text createText(String text) {
        Text renderedText = new Text(text);
        renderedText.setFont(resultDisplay.getFont());
        renderedText.setTextOrigin(VPos.TOP);
        return renderedText;
    }

    static String formatCommandFeedback(String commandText, String feedbackToUser) {
        requireNonNull(commandText);
        requireNonNull(feedbackToUser);
        return COMMAND_PREFIX + commandText + System.lineSeparator() + feedbackToUser;
    }

}
