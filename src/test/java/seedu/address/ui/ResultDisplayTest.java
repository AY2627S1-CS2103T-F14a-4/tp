package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seedu.address.MainApp;

public class ResultDisplayTest {

    @BeforeAll
    public static void startToolkit() throws Exception {
        JavaFxTestUtil.startToolkit();
    }

    @Test
    public void formatCommandFeedback_commandAndFeedbackProvided_formatsBoth() {
        String commandText = "  LiSt  ";
        String feedbackToUser = "Listed all persons";

        assertEquals("Command:   LiSt  " + System.lineSeparator() + "Listed all persons",
                ResultDisplay.formatCommandFeedback(commandText, feedbackToUser));
    }

    @Test
    public void calculateDisplayedRowCount_clampsBetweenTwoAndFourRows() {
        assertEquals(2, ResultDisplay.calculateDisplayedRowCount("Command: list\nListed all persons"));
        assertEquals(3, ResultDisplay.calculateDisplayedRowCount("Command: test\nLine 1\nLine 2"));
        assertEquals(4, ResultDisplay.calculateDisplayedRowCount("Command: test\nLine 1\nLine 2\nLine 3"));
        assertEquals(4, ResultDisplay.calculateDisplayedRowCount("Command: test\nLine 1\nLine 2\nLine 3\nLine 4"));
    }

    @Test
    public void setFeedbackToUser_initiallyHiddenThenShowsWithCalculatedRows() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                ResultDisplay display = new ResultDisplay();
                TextArea textArea = (TextArea) display.getRoot().lookup("#resultDisplay");
                Region filler = new Region();
                VBox layout = new VBox(display.getRoot(), filler);
                VBox.setVgrow(filler, Priority.ALWAYS);
                Scene scene = new Scene(layout, 600, 400);
                scene.getStylesheets().add(Objects.requireNonNull(
                        MainApp.class.getResource("/view/DarkTheme.css")).toExternalForm());

                assertFalse(display.getRoot().isVisible());
                assertFalse(display.getRoot().isManaged());

                display.setFeedbackToUser("list", "Listed all persons");
                assertTrue(display.getRoot().isVisible());
                assertTrue(display.getRoot().isManaged());
                assertEquals(2, textArea.getPrefRowCount());
                layout.applyCss();
                layout.layout();
                runAfterLayoutUpdates(() -> {
                    try {
                        layout.layout();
                        double twoRowHeight = display.getRoot().getHeight();
                        assertFalse(getVerticalScrollBar(textArea).isVisible());

                        display.setFeedbackToUser("test", "Line 1\nLine 2\nLine 3");
                        assertEquals(4, textArea.getPrefRowCount());
                        layout.layout();
                        runAfterLayoutUpdates(() -> {
                            try {
                                layout.layout();
                                double fourRowHeight = display.getRoot().getHeight();
                                assertTrue(fourRowHeight > twoRowHeight);
                                assertFalse(getVerticalScrollBar(textArea).isVisible());

                                display.setFeedbackToUser("test", "Line 1\nLine 2\nLine 3\nLine 4");
                                layout.layout();
                                runAfterLayoutUpdates(() -> {
                                    try {
                                        layout.layout();
                                        assertEquals(fourRowHeight, display.getRoot().getHeight());
                                        assertTrue(getVerticalScrollBar(textArea).isVisible());
                                        assertViewportFitsFourRowsButNotFive(textArea);
                                        assertEquals("Command: test\nLine 1\nLine 2\nLine 3\nLine 4",
                                                textArea.getText());
                                        checked.complete(null);
                                    } catch (Throwable error) {
                                        checked.completeExceptionally(error);
                                    }
                                });
                            } catch (Throwable error) {
                                checked.completeExceptionally(error);
                            }
                        });
                    } catch (Throwable error) {
                        checked.completeExceptionally(error);
                    }
                });
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    private void runAfterLayoutUpdates(Runnable task) {
        Platform.runLater(() -> Platform.runLater(task));
    }

    private ScrollBar getVerticalScrollBar(TextArea textArea) {
        return textArea.lookupAll(".scroll-bar").stream()
                .filter(ScrollBar.class::isInstance)
                .map(ScrollBar.class::cast)
                .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL)
                .findFirst()
                .orElseThrow();
    }

    private void assertViewportFitsFourRowsButNotFive(TextArea textArea) {
        ScrollPane scrollPane = (ScrollPane) textArea.lookup(".scroll-pane");
        Region content = (Region) textArea.lookup(".content");
        String fourRows = String.join("\n", textArea.getParagraphs().subList(0, 4));
        double fourRowContentHeight = measureRenderedTextHeight(textArea, fourRows)
                + content.getInsets().getTop() + content.getInsets().getBottom();
        double fiveRowContentHeight = measureRenderedTextHeight(textArea, textArea.getText())
                + content.getInsets().getTop() + content.getInsets().getBottom();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        assertEquals(5, textArea.getParagraphs().size());
        assertTrue(viewportHeight >= fourRowContentHeight);
        assertTrue(viewportHeight <= Math.ceil(fourRowContentHeight) + 14.0);
        assertTrue(viewportHeight < fiveRowContentHeight);
    }

    private double measureRenderedTextHeight(TextArea textArea, String text) {
        Text renderedText = new Text(text);
        renderedText.setFont(textArea.getFont());
        renderedText.setTextOrigin(VPos.TOP);
        return renderedText.getLayoutBounds().getHeight();
    }
}
