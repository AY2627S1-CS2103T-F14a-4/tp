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
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startToolkit() throws Exception {
        JavaFxTestUtil.startToolkit();
    }

    @Test
    public void layout_normalWindow_keepsPreferredResultHeight() throws Exception {
        assertResultDisplayLayout(600, false);
    }

    @Test
    public void layout_minimumWindow_shrinksResultAndRestoresItOnResize() throws Exception {
        assertResultDisplayLayout(340, true);
    }

    private void assertResultDisplayLayout(double windowHeight, boolean expectShrink) throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                Path clientBookPath = temporaryFolder.resolve("clientbook.json");
                Storage storage = new StorageManager(new JsonClientBookStorage(clientBookPath),
                        new JsonUserPrefsStorage(temporaryFolder.resolve("userprefs.json")));
                Logic logic = new LogicManager(new ModelManager(getTypicalClientBook(), new UserPrefs()), storage);
                MainWindow mainWindow = new MainWindow(stage, logic, clientBookPath);
                mainWindow.fillInnerParts();

                Scene scene = stage.getScene();
                Region resultPane = (Region) scene.lookup("#resultDisplayPlaceholder");
                assertEquals(600, new UserPrefs().getGuiSettings().getWindowHeight());
                assertEquals(340, stage.getMinHeight());
                stage.setHeight(windowHeight);
                // Reserve space for native decorations without depending on the OS window manager.
                scene.getRoot().resize(740, windowHeight - 40);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                if (expectShrink) {
                    assertTrue(resultPane.getHeight() >= 50);
                    assertTrue(resultPane.getHeight() < 140);
                } else {
                    assertEquals(140, resultPane.getHeight(), 0.01);
                }
                Region commandPane = (Region) scene.lookup("#commandBoxPlaceholder");
                Region statusPane = (Region) scene.lookup("#statusbarPlaceholder");
                assertTrue(commandPane.getHeight() > 0);
                assertTrue(statusPane.getHeight() > 0);
                assertTrue(statusPane.getBoundsInParent().getMaxY() <= scene.getRoot().getLayoutBounds().getHeight());

                stage.setHeight(600);
                scene.getRoot().resize(740, 560);
                scene.getRoot().layout();
                assertEquals(140, resultPane.getHeight(), 0.01);
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

                personListView.getSelectionModel().select(0);
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                personListView.getSelectionModel().clearSelection();

                commandTextField.setText("find e");
                commandTextField.fireEvent(new ActionEvent());

                assertTrue(personListView.getItems().size() > 1);
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN));
                assertEquals(1, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.UP));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());

                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, true, false, false, false));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, false, true, false, false));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, false, false, true, false));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, false, false, false, true));
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());

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

    private void assertCellIsFullyVisible(VirtualFlow<?> virtualFlow, int index) {
        IndexedCell<?> cell = virtualFlow.getVisibleCell(index);
        assertNotNull(cell);
        Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
        Bounds viewportBounds = virtualFlow.localToScene(virtualFlow.getBoundsInLocal());
        assertTrue(cellBounds.getMinY() >= viewportBounds.getMinY());
        assertTrue(cellBounds.getMaxY() <= viewportBounds.getMaxY());
    }

    private KeyEvent keyPressed(KeyCode keyCode) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false);
    }

    private KeyEvent keyPressed(KeyCode keyCode, boolean isShiftDown, boolean isControlDown,
                                boolean isAltDown, boolean isMetaDown) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                isShiftDown, isControlDown, isAltDown, isMetaDown);
    }
}
