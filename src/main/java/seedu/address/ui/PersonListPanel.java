package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final ObservableList<Person> allPersons;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        this(personList, personList);
    }

    /**
     * Creates a {@code PersonListPanel} that displays {@code personList} with indexes from {@code allPersons}.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableList<Person> allPersons) {
        super(FXML);
        this.allPersons = allPersons;
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        allPersons.addListener((ListChangeListener<Person>) change -> personListView.refresh());
    }

    /** Selects and scrolls to the first displayed person, or clears selection when the list is empty. */
    void selectFirstPerson() {
        if (personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().clearSelection();
            return;
        }
        selectAndScrollTo(0);
    }

    /** Selects and scrolls to the next displayed person, stopping at the final person. */
    void selectNextPerson() {
        int selectedIndex = personListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            selectFirstPerson();
            return;
        }
        selectAndScrollTo(Math.min(selectedIndex + 1, personListView.getItems().size() - 1));
    }

    /** Selects and scrolls to the previous displayed person, stopping at the first person. */
    void selectPreviousPerson() {
        int selectedIndex = personListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            selectFirstPerson();
            return;
        }
        selectAndScrollTo(Math.max(selectedIndex - 1, 0));
    }

    /** Returns whether a displayed person is currently selected. */
    boolean hasSelectedPerson() {
        return personListView.getSelectionModel().getSelectedIndex() >= 0;
    }

    private void selectAndScrollTo(int index) {
        personListView.getSelectionModel().select(index);
        personListView.scrollTo(index);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, allPersons.indexOf(person) + 1).getRoot());
            }
        }
    }

}
