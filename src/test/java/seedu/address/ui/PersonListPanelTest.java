package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.StackPane;

public class PersonListPanelTest {

    @BeforeAll
    public static void startToolkit() throws Exception {
        JavaFxTestUtil.startToolkit();
    }

    @Test
    public void selectionNavigation_selectsWithinBoundsAndScrollsIntoView() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(getTypicalPersons()));
                StackPane root = new StackPane(panel.getRoot());
                new Scene(root, 600, 200);
                root.applyCss();
                root.layout();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");

                assertEquals(-1, list.getSelectionModel().getSelectedIndex());
                panel.selectFirstPerson();
                assertEquals(0, list.getSelectionModel().getSelectedIndex());
                panel.selectPreviousPerson();
                assertEquals(0, list.getSelectionModel().getSelectedIndex());
                panel.selectNextPerson();
                assertEquals(1, list.getSelectionModel().getSelectedIndex());

                int finalIndex = list.getItems().size() - 1;
                for (int i = 1; i < finalIndex; i++) {
                    panel.selectNextPerson();
                }
                panel.selectNextPerson();
                root.layout();

                assertEquals(finalIndex, list.getSelectionModel().getSelectedIndex());
                assertTrue(isCellRendered(list, finalIndex));
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void selectionNavigation_emptyList_remainsUnselected() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList());
                ListView<?> list = (ListView<?>) panel.getRoot().lookup("#personListView");

                panel.selectFirstPerson();
                panel.selectNextPerson();
                panel.selectPreviousPerson();

                assertEquals(-1, list.getSelectionModel().getSelectedIndex());
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void pixelScrolling_movesIncrementallyWithoutChangingSelection() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(getTypicalPersons()));
                StackPane root = new StackPane(panel.getRoot());
                new Scene(root, 600, 200);
                root.applyCss();
                root.layout();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                VirtualFlow<?> virtualFlow = (VirtualFlow<?>) list.lookup(".virtual-flow");
                int initiallyVisibleIndex = virtualFlow.getFirstVisibleCell().getIndex();
                list.getSelectionModel().select(1);

                panel.scrollDown();
                root.layout();

                double firstScrollPosition = virtualFlow.getPosition();
                assertTrue(firstScrollPosition > 0);
                assertEquals(initiallyVisibleIndex, virtualFlow.getFirstVisibleCell().getIndex());
                assertEquals(1, list.getSelectionModel().getSelectedIndex());

                panel.scrollDown();
                root.layout();
                double secondScrollPosition = virtualFlow.getPosition();
                assertTrue(secondScrollPosition > firstScrollPosition);

                panel.scrollUp();
                root.layout();
                assertTrue(virtualFlow.getPosition() < secondScrollPosition);
                assertEquals(1, list.getSelectionModel().getSelectedIndex());
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    private boolean isCellRendered(ListView<?> list, int index) {
        return list.lookupAll(".list-cell").stream()
                .filter(ListCell.class::isInstance)
                .map(ListCell.class::cast)
                .anyMatch(cell -> cell.getIndex() == index && cell.isVisible());
    }
}
