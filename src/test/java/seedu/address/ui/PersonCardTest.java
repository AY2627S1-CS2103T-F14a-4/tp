package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.model.person.InteractionNote;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    @BeforeAll
    public static void startToolkit() throws Exception {
        CompletableFuture<Void> started = new CompletableFuture<>();
        Platform.startup(() -> started.complete(null));
        started.get(10, TimeUnit.SECONDS);
        Platform.setImplicitExit(false);
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
}
