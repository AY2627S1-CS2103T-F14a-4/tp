package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

public class ClientBookTest {

    private final ClientBook clientBook = new ClientBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), clientBook.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> clientBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyClientBook_replacesData() {
        ClientBook newData = getTypicalClientBook();
        clientBook.resetData(newData);
        assertEquals(newData, clientBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        ClientBookStub newData = new ClientBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> clientBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> clientBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInClientBook_returnsFalse() {
        assertFalse(clientBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInClientBook_returnsTrue() {
        clientBook.addPerson(ALICE);
        assertTrue(clientBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInClientBook_returnsTrue() {
        clientBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(clientBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> clientBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = ClientBook.class.getCanonicalName() + "{persons=" + clientBook.getPersonList() + "}";
        assertEquals(expected, clientBook.toString());
    }

    /**
     * A stub ReadOnlyClientBook whose persons list can violate interface constraints.
     */
    private static class ClientBookStub implements ReadOnlyClientBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        ClientBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }
    }

}
