package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

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
    public void constructor_resultDisplayWrapsText() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                ResultDisplay display = new ResultDisplay();
                TextArea textArea = (TextArea) display.getRoot().lookup("#resultDisplay");
                assertTrue(textArea.isWrapText());
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }
}
