package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

/** Contains JavaFX integration tests for command feedback and find-result navigation. */
public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startToolkit() throws Exception {
        JavaFxTestUtil.startToolkit();
    }

    @Test
    public void executeCommand_invalidCommand_displaysCommandAndError() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                Path clientBookPath = temporaryFolder.resolve("clientbook.json");
                Path userPrefsPath = temporaryFolder.resolve("userprefs.json");
                Storage storage = new StorageManager(new JsonClientBookStorage(clientBookPath),
                        new JsonUserPrefsStorage(userPrefsPath));
                Logic logic = new LogicManager(new ModelManager(), storage);
                MainWindow mainWindow = new MainWindow(stage, logic, clientBookPath);
                mainWindow.fillInnerParts();

                Scene scene = stage.getScene();
                TextField commandTextField = (TextField) scene.lookup("#commandTextField");
                commandTextField.setText("foo");
                commandTextField.fireEvent(new ActionEvent());

                TextArea resultDisplay = (TextArea) scene.lookup("#resultDisplay");
                assertEquals("Command: foo\n" + MESSAGE_UNKNOWN_COMMAND, resultDisplay.getText());
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            } finally {
                stage.close();
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void executeFind_selectsFirstResultAndScopesArrowKeyNavigation() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                Path clientBookPath = temporaryFolder.resolve("clientbook.json");
                Path userPrefsPath = temporaryFolder.resolve("userprefs.json");
                Storage storage = new StorageManager(new JsonClientBookStorage(clientBookPath),
                        new JsonUserPrefsStorage(userPrefsPath));
                Logic logic = new LogicManager(new ModelManager(getTypicalClientBook(), new UserPrefs()), storage);
                MainWindow mainWindow = new MainWindow(stage, logic, clientBookPath);
                mainWindow.fillInnerParts();

                Scene scene = stage.getScene();
                TextField commandTextField = (TextField) scene.lookup("#commandTextField");
                ListView<?> personListView = (ListView<?>) scene.lookup("#personListView");
                assertEquals(-1, personListView.getSelectionModel().getSelectedIndex());

                // A selection made outside find must not activate arrow-key navigation.
                personListView.getSelectionModel().select(0);
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                personListView.getSelectionModel().clearSelection();

                // A successful find selects its first result and enables unmodified arrow keys.
                commandTextField.setText("find e");
                commandTextField.fireEvent(new ActionEvent());

                assertTrue(personListView.getItems().size() > 1);
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                assertEquals(1, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.UP));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());

                // Modified arrow keys remain available for normal text-field behavior.
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, true, false, false, false));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, false, true, false, false));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, false, false, true, false));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, false, false, false, true));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());

                // Either a failed command or a later non-find command ends find navigation.
                commandTextField.setText("foo");
                commandTextField.fireEvent(new ActionEvent());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());

                commandTextField.setText("find e");
                commandTextField.fireEvent(new ActionEvent());
                commandTextField.setText("list");
                commandTextField.fireEvent(new ActionEvent());
                personListView.getSelectionModel().select(0);
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());

                // An empty find result leaves no selection for arrow keys to move.
                commandTextField.setText("find Nobody");
                commandTextField.fireEvent(new ActionEvent());
                assertEquals(0, personListView.getItems().size());
                assertEquals(-1, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                commandTextField.fireEvent(keyPressed(KeyCode.UP));
                assertEquals(-1, personListView.getSelectionModel().getSelectedIndex());
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            } finally {
                stage.close();
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void executeFind_navigateToOffscreenResult_scrollsItIntoView() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                Path clientBookPath = temporaryFolder.resolve("clientbook.json");
                Path userPrefsPath = temporaryFolder.resolve("userprefs.json");
                Storage storage = new StorageManager(new JsonClientBookStorage(clientBookPath),
                        new JsonUserPrefsStorage(userPrefsPath));
                Logic logic = new LogicManager(new ModelManager(getTypicalClientBook(), new UserPrefs()), storage);
                MainWindow mainWindow = new MainWindow(stage, logic, clientBookPath);
                mainWindow.fillInnerParts();

                Scene scene = stage.getScene();
                scene.getRoot().resize(600, 400);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                TextField commandTextField = (TextField) scene.lookup("#commandTextField");
                ListView<?> personListView = (ListView<?>) scene.lookup("#personListView");
                VirtualFlow<?> virtualFlow = (VirtualFlow<?>) personListView.lookup(".virtual-flow");

                commandTextField.setText("find t/friend");
                commandTextField.fireEvent(new ActionEvent());
                assertEquals(3, personListView.getItems().size());
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());

                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                assertEquals(2, personListView.getSelectionModel().getSelectedIndex());

                Platform.runLater(() -> {
                    try {
                        scene.getRoot().layout();
                        assertCellIsFullyVisible(virtualFlow, 2);
                        checked.complete(null);
                    } catch (Throwable error) {
                        checked.completeExceptionally(error);
                    } finally {
                        stage.close();
                    }
                });
            } catch (Throwable error) {
                checked.completeExceptionally(error);
                stage.close();
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    /** Asserts that the requested list cell is fully contained in the visible list viewport. */
    private void assertCellIsFullyVisible(VirtualFlow<?> virtualFlow, int index) {
        IndexedCell<?> cell = virtualFlow.getVisibleCell(index);
        assertNotNull(cell);
        Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
        Bounds viewportBounds = virtualFlow.localToScene(virtualFlow.getBoundsInLocal());
        assertTrue(cellBounds.getMinY() >= viewportBounds.getMinY());
        assertTrue(cellBounds.getMaxY() <= viewportBounds.getMaxY());
    }

    /** Creates an unmodified key-press event. */
    private KeyEvent keyPressed(KeyCode keyCode) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false);
    }

    /** Creates a key-press event with explicit modifier states. */
    private KeyEvent keyPressed(KeyCode keyCode, boolean isShiftDown, boolean isControlDown,
                                boolean isAltDown, boolean isMetaDown) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                isShiftDown, isControlDown, isAltDown, isMetaDown);
    }
}
