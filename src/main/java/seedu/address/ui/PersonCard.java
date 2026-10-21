package seedu.address.ui;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label relationship;
    @FXML
    private FlowPane tags;
    @FXML
    private Label financialNeed;
    @FXML
    private Label priority;
    @FXML
    private Label areaOfInterest;
    @FXML
    private VBox interactionNotes;
    @FXML
    private Label nextAction;
    @FXML
    private Label preferredContactChannel;
    @FXML
    private Label preferredContactTiming;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        relationship.setText(person.getRelationship().toString());
        setOptionalInformation(financialNeed, "Financial need: ", person.getFinancialNeed().map(value -> value.value));
        setOptionalInformation(priority, "Priority: ", person.getPriority().map(value -> value.value));
        setOptionalInformation(areaOfInterest, "Area of interest: ",
                person.getAreaOfInterest().map(value -> value.value));
        setOptionalInformation(preferredContactChannel, "Preferred contact channel: ",
                person.getPreferredContactChannel().map(value -> value.value));
        setOptionalInformation(preferredContactTiming, "Preferred contact timing: ",
                person.getPreferredContactTiming().map(value -> value.value));
        setOptionalInformation(nextAction, "", person.getNextAction().map(action -> {
            String status = action.isCompleted() ? "Completed"
                    : action.getDueDate().isBefore(LocalDate.now()) ? "Overdue" : "Pending";
            nextAction.getStyleClass().add("followup-" + status.toLowerCase(Locale.ROOT));
            return status + " | Due " + action.getDueDate() + ": " + action.getAction();
        }));
        interactionNotes.setVisible(!person.getInteractionNotes().isEmpty());
        interactionNotes.setManaged(!person.getInteractionNotes().isEmpty());
        for (int i = 0; i < person.getInteractionNotes().size(); i++) {
            Label label = new Label((i + 1) + ". " + person.getInteractionNotes().get(i));
            label.getStyleClass().add("cell_small_label");
            label.setWrapText(true);
            // Let the parent supply the width during measurement, before the card is laid out.
            // Binding to the card's actual width makes the first wrapped-height measurement stale.
            label.setMinWidth(0);
            label.setPrefWidth(0);
            label.setMaxWidth(Double.MAX_VALUE);
            interactionNotes.getChildren().add(label);
        }
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }

    /**
     * Displays optional information without reserving layout space when it is absent.
     */
    private void setOptionalInformation(Label label, String prefix, Optional<String> value) {
        label.setText(value.map(text -> prefix + text).orElse(""));
        label.setVisible(value.isPresent());
        label.setManaged(value.isPresent());
    }

}
