package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
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
                StackPane resultDisplayPlaceholder = (StackPane) scene.lookup("#resultDisplayPlaceholder");
                assertFalse(resultDisplayPlaceholder.isVisible());
                assertFalse(resultDisplayPlaceholder.isManaged());

                commandTextField.setText("foo");
                commandTextField.fireEvent(new ActionEvent());

                TextArea resultDisplay = (TextArea) scene.lookup("#resultDisplay");
                assertEquals("Command: foo\n" + MESSAGE_UNKNOWN_COMMAND, resultDisplay.getText());
                assertTrue(resultDisplayPlaceholder.isVisible());
                assertTrue(resultDisplayPlaceholder.isManaged());
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

                scene.getRoot().resize(600, 400);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                VirtualFlow<?> virtualFlow = (VirtualFlow<?>) personListView.lookup(".virtual-flow");
                commandTextField.fireEvent(keyPressed(KeyCode.DOWN, true));
                scene.getRoot().layout();
                assertTrue(virtualFlow.getPosition() > 0);
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

    private KeyEvent keyPressed(KeyCode keyCode) {
        return keyPressed(keyCode, false);
    }

    private KeyEvent keyPressed(KeyCode keyCode, boolean isShiftDown) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, isShiftDown, false, false, false);
    }
}
