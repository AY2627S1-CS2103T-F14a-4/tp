package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_mixedClientsAndProspects_showsEverything() {
        Person client = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), Relationship.CLIENT);
        Person prospect = new Person(BENSON.getName(), BENSON.getPhone(), BENSON.getEmail(), Relationship.PROSPECT);
        model = new ModelManager();
        model.addPerson(client);
        model.addPerson(prospect);
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        model.updateFilteredPersonList(person -> person.getRelationship() == Relationship.CLIENT);

        assertCommandSuccess(new ListCommand(), model, "Listed all clients and prospects.", expectedModel);
        assertEquals(List.of(client, prospect), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAddressBook_success() {
        model = new ModelManager();
        expectedModel = new ModelManager();

        assertCommandSuccess(new ListCommand(), model, "Listed all clients and prospects.", expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }
}
