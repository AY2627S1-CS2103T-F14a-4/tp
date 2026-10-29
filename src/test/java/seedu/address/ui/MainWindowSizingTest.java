package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests result sizing without starting JavaFX or relying on a window manager. */
public class MainWindowSizingTest {

    @Test
    public void resultDisplayMinHeight_normalAndLargerWindows_returnsPreferredHeight() {
        assertEquals(140, MainWindow.resultDisplayMinHeight(600));
        assertEquals(140, MainWindow.resultDisplayMinHeight(800));
    }

    @Test
    public void resultDisplayMinHeight_compactWindows_returnsCompactMinimum() {
        assertEquals(50, MainWindow.resultDisplayMinHeight(340));
        assertEquals(50, MainWindow.resultDisplayMinHeight(510));
        assertEquals(50, MainWindow.resultDisplayMinHeight(1));
    }

    @Test
    public void resultDisplayMinHeight_intermediateWindows_relaxesMinimumHeight() {
        assertEquals(80, MainWindow.resultDisplayMinHeight(540));
        assertEquals(120, MainWindow.resultDisplayMinHeight(580));
        assertEquals(139.5, MainWindow.resultDisplayMinHeight(599.5));
    }

    @Test
    public void resultDisplayMinHeight_uninitialisedHeight_returnsPreferredHeight() {
        assertEquals(140, MainWindow.resultDisplayMinHeight(Double.NaN));
        assertEquals(140, MainWindow.resultDisplayMinHeight(0));
        assertEquals(140, MainWindow.resultDisplayMinHeight(-1));
        assertEquals(140, MainWindow.resultDisplayMinHeight(Double.POSITIVE_INFINITY));
        assertEquals(140, MainWindow.resultDisplayMinHeight(Double.NEGATIVE_INFINITY));
    }
}
