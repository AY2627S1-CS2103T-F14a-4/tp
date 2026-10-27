package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ResultDisplayTest {

    @Test
    public void formatCommandFeedback_commandAndFeedbackProvided_formatsBoth() {
        String commandText = "  LiSt  ";
        String feedbackToUser = "Listed all persons";

        assertEquals("Command:   LiSt  " + System.lineSeparator() + "Listed all persons",
                ResultDisplay.formatCommandFeedback(commandText, feedbackToUser));
    }
}
