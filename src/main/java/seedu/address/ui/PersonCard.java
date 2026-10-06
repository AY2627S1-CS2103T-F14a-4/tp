package seedu.address.ui;

import java.util.Comparator;
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
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
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
        interactionNotes.setVisible(!person.getInteractionNotes().isEmpty());
        interactionNotes.setManaged(!person.getInteractionNotes().isEmpty());
        person.getInteractionNotes().forEach(note -> {
            Label label = new Label(note.toString());
            label.getStyleClass().add("cell_small_label");
            label.setWrapText(true);
            label.prefWidthProperty().bind(cardPane.widthProperty().subtract(30));
            interactionNotes.getChildren().add(label);
        });
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
