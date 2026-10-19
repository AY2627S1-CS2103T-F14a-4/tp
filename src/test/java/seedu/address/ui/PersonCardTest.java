package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    @Test
    public void nextAction_hiddenWhenAbsent_showsStatusDateAndWrappedDescription() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                Label absent = (Label) new PersonCard(ALICE, 1).getRoot().lookup("#nextAction");
                assertFalse(absent.isVisible());
                assertFalse(absent.isManaged());
                LocalDate today = LocalDate.now();
                LocalDate[] dates = {today.minusDays(1), today, today.plusDays(1), today.minusDays(1)};
                String[] statuses = {"Overdue", "Pending", "Pending", "Completed"};
                for (int i = 0; i < dates.length; i++) {
                    NextAction action = new NextAction("A".repeat(200), dates[i], i == 3);
                    Region card = new PersonCard(ALICE.withNextAction(action), 1).getRoot();
                    Label label = (Label) card.lookup("#nextAction");
                    assertTrue(label.isVisible());
                    assertTrue(label.isManaged());
                    assertTrue(label.isWrapText());
                    assertEquals(statuses[i] + " | Due " + dates[i] + ": " + action.getAction(), label.getText());
                    StackPane root = new StackPane(card);
                    Scene scene = new Scene(root, 400, 600);
                    scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
                    root.applyCss();
                    root.layout();
                    assertTrue(label.getHeight() > 30, "Maximum-length action must wrap in a narrow card");
                    assertTrue(label.getBoundsInParent().getMaxX() <= card.getWidth());
                }
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    /** Starts the real toolkit; headless Linux runs need a virtual display such as Xvfb. */
    @BeforeAll
    public static void startToolkit() throws Exception {
        CompletableFuture<Void> started = new CompletableFuture<>();
        Platform.startup(() -> started.complete(null));
        started.get(10, TimeUnit.SECONDS);
        Platform.setImplicitExit(false);
    }

    @Test
    public void financialInformation_hiddenWhenAbsent() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                Region card = new PersonCard(new PersonBuilder().build(), 1).getRoot();
                for (String field : new String[] {"financialNeed", "priority", "areaOfInterest"}) {
                    Label label = (Label) card.lookup("#" + field);
                    assertFalse(label.isVisible());
                    assertFalse(label.isManaged());
                }
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void financialInformation_longValuesWrap() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                String longValue = "Retirement planning ".repeat(10).strip();
                // Exercise both word wrapping and a maximum-length value without spaces.
                for (String value : new String[] {longValue, "A".repeat(200)}) {
                    assertFinancialInformationWraps(layoutFinancialCards(value), value);
                }
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void interactionNotes_hiddenWhenEmpty_visibleAndWrappedWhenPresent() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                VBox empty = (VBox) new PersonCard(ALICE, 1).getRoot().lookup("#interactionNotes");
                assertFalse(empty.isVisible());
                assertFalse(empty.isManaged());
                InteractionNote note = new InteractionNote(LocalDate.of(2000, 1, 1), "客户's plan / retirement");
                PersonCard card = new PersonCard(new PersonBuilder(ALICE).withInteractionNotes(note, note).build(), 1);
                VBox notes = (VBox) card.getRoot().lookup("#interactionNotes");
                assertTrue(notes.isVisible());
                assertTrue(notes.isManaged());
                assertEquals(3, notes.getChildren().size());
                Label first = (Label) notes.getChildren().get(1);
                assertEquals(note.toString(), first.getText());
                assertTrue(first.isWrapText());
                assertEquals(note.toString(), ((Label) notes.getChildren().get(2)).getText());
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void interactionNotes_preferredHeightUsesAvailableWidthBeforeFirstLayout() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                InteractionNote note = new InteractionNote(LocalDate.of(2026, 10, 6),
                        "Discussed retirement planning and investment options.");
                Region card = new PersonCard(new PersonBuilder(ALICE).withInteractionNotes(note).build(), 1).getRoot();
                StackPane root = new StackPane(card);
                Scene scene = new Scene(root, 600, 600);
                scene.getStylesheets().add(PersonCardTest.class.getResource("/view/DarkTheme.css").toExternalForm());
                root.applyCss();
                double beforeLayout = card.prefHeight(600);
                card.resize(600, beforeLayout);
                double afterResize = card.prefHeight(600);
                assertEquals(afterResize, beforeLayout, 1,
                        "A new card must be measured correctly before it receives its first actual width");
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void interactionNotes_listUpdatesResizeImmediatelyAndScrollNaturally() throws Exception {
        CompletableFuture<Void> checked = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
                PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
                StackPane root = new StackPane(panel.getRoot());
                Scene scene = new Scene(root, 600, 600);
                scene.getStylesheets().add(PersonCardTest.class.getResource("/view/DarkTheme.css").toExternalForm());
                root.applyCss();
                root.layout();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                double compactHeight = displayedCell(list, 1).getHeight();

                new NoteCommand(INDEX_FIRST_PERSON, new InteractionNote(LocalDate.of(2026, 10, 6),
                        "Discussed retirement planning and investment options.")).execute(model);
                root.layout();
                ListCell<?> first = displayedCell(list, 0);
                double firstNoteHeight = first.getHeight();
                assertEquals(first.prefHeight(first.getWidth()), firstNoteHeight, 1);
                assertEquals(compactHeight, displayedCell(list, 1).getHeight(), 1);
                assertTrue(displayedCell(list, 1).getBoundsInParent().getMaxY() < list.getHeight());

                new NoteCommand(INDEX_FIRST_PERSON,
                        new InteractionNote(LocalDate.of(2026, 10, 6), "Second note")).execute(model);
                root.layout();
                first = displayedCell(list, 0);
                assertTrue(first.getHeight() > firstNoteHeight);
                assertEquals(first.prefHeight(first.getWidth()), first.getHeight(), 1);

                double shortNotesHeight = first.getHeight();
                new NoteCommand(INDEX_FIRST_PERSON, new InteractionNote(LocalDate.of(2026, 10, 6),
                        "Long wrapped interaction note with realistic detail. ".repeat(80))).execute(model);
                root.layout();
                first = displayedCell(list, 0);
                assertTrue(first.getHeight() > shortNotesHeight);
                assertEquals(first.prefHeight(first.getWidth()), first.getHeight(), 1);
                list.scrollTo(2);
                root.layout();
                assertEquals(compactHeight, displayedCell(list, 2).getHeight(), 1);
                list.scrollTo(0);
                root.layout();
                first = displayedCell(list, 0);
                assertEquals(first.prefHeight(first.getWidth()), first.getHeight(), 1);
                assertEquals(3, model.getFilteredPersonList().get(0).getInteractionNotes().size());
                checked.complete(null);
            } catch (Throwable error) {
                checked.completeExceptionally(error);
            }
        });
        checked.get(10, TimeUnit.SECONDS);
    }

    private ListView<?> layoutFinancialCards(String value) {
        PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(
                new PersonBuilder().withFinancialNeed("Short").withPriority("Short")
                        .withAreaOfInterest("Short").build(),
                new PersonBuilder().withFinancialNeed(value).withPriority(value)
                        .withAreaOfInterest(value).build()));
        StackPane root = new StackPane(panel.getRoot());
        Scene scene = new Scene(root, 300, 600);
        scene.getStylesheets().add(PersonCardTest.class.getResource("/view/DarkTheme.css")
                .toExternalForm());
        root.applyCss();
        root.layout();
        ListView<?> list = (ListView<?>) root.lookup("#personListView");
        return list;
    }

    private void assertFinancialInformationWraps(ListView<?> list, String value) {
        ListCell<?> shortCell = displayedCell(list, 0);
        ListCell<?> longCell = displayedCell(list, 1);
        Region shortCard = (Region) shortCell.getGraphic();
        Region card = (Region) longCell.getGraphic();
        assertEquals(shortCard.getWidth(), card.getWidth(), 1,
                "Both cards must be compared at the same available width");
        assertTrue(card.getWidth() <= list.getWidth());
        assertTrue(card.getHeight() > shortCard.getHeight(),
                "Long financial information must increase the laid-out card height");
        assertTrue(longCell.getHeight() > shortCell.getHeight(),
                "The list cell must grow to accommodate the taller card");
        Bounds cardInCell = longCell.sceneToLocal(card.localToScene(card.getBoundsInLocal()));
        assertTrue(cardInCell.getMaxY() <= longCell.getHeight() + 1,
                "The card must fit vertically inside its list cell");
        for (String field : new String[] {"financialNeed", "priority", "areaOfInterest"}) {
            Label label = (Label) card.lookup("#" + field);
            Label shortLabel = (Label) shortCard.lookup("#" + field);
            assertTrue(label.isVisible());
            assertTrue(label.isManaged());
            assertTrue(label.isWrapText());
            assertTrue(label.getText().endsWith(value));
            assertEquals(shortLabel.getWidth(), label.getWidth(), 1);
            assertTrue(label.getHeight() > shortLabel.getHeight(),
                    field + " must wrap to more lines than its short-value control");
            Bounds labelInCard = card.sceneToLocal(label.localToScene(label.getBoundsInLocal()));
            assertTrue(labelInCard.getMinX() >= -1);
            assertTrue(labelInCard.getMaxX() <= card.getWidth() + 1,
                    field + " must stay within the card width");
            assertTrue(labelInCard.getMinY() >= -1);
            assertTrue(labelInCard.getMaxY() <= card.getHeight() + 1,
                    field + " must fit vertically inside the card");
        }
    }

    private ListCell<?> displayedCell(ListView<?> list, int index) {
        return list.lookupAll(".list-cell").stream().filter(ListCell.class::isInstance)
                .map(node -> (ListCell<?>) node).filter(cell -> cell.getIndex() == index && !cell.isEmpty())
                .findFirst().orElseThrow();
    }
}
