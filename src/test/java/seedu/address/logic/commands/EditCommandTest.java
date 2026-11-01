package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.ClientBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());

    @Test
    public void execute_normalEdits_preserveFinancialInformation() {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withFinancialNeed("Retirement").withPriority("High").withAreaOfInterest("Insurance").build();
        model.setPerson(model.getFilteredPersonList().get(0), original);
        EditPersonDescriptor[] descriptors = {
            new EditPersonDescriptorBuilder().withName("Updated Name").build(),
            new EditPersonDescriptorBuilder().withPhone("81234567").build(),
            new EditPersonDescriptorBuilder().withEmail("updated@example.com").build(),
            new EditPersonDescriptorBuilder().withRelationship(Relationship.CLIENT).build()
        };
        Person[] expectedPersons = {
            new PersonBuilder(original).withName("Updated Name").build(),
            new PersonBuilder(original).withPhone("81234567").build(),
            new PersonBuilder(original).withEmail("updated@example.com").build(),
            new PersonBuilder(original).withRelationship(Relationship.CLIENT).build()
        };
        for (int i = 0; i < descriptors.length; i++) {
            model.setPerson(model.getFilteredPersonList().get(0), original);
            Model expectedModel = new ModelManager(new ClientBook(model.getClientBook()), new UserPrefs());
            expectedModel.setPerson(original, expectedPersons[i]);
            String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                    Messages.format(expectedPersons[i]));
            assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptors[i]), model,
                    expectedMessage, expectedModel);
            Person edited = model.getFilteredPersonList().get(0);
            assertEquals(original.getFinancialNeed(), edited.getFinancialNeed());
            assertEquals(original.getPriority(), edited.getPriority());
            assertEquals(original.getAreaOfInterest(), edited.getAreaOfInterest());
        }
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person replacementDetails = new PersonBuilder().build();
        Person editedPerson = new Person(replacementDetails.getName(), replacementDetails.getPhone(),
                replacementDetails.getEmail(), personToEdit.getTags(), replacementDetails.getRelationship(),
                personToEdit.getFinancialNeed().orElse(null), personToEdit.getPriority().orElse(null),
                personToEdit.getAreaOfInterest().orElse(null), personToEdit.getInteractionNotes());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(replacementDetails).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new ClientBook(model.getClientBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new ClientBook(model.getClientBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new ClientBook(model.getClientBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().getFirst();
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new ClientBook(model.getClientBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(personInFilteredList, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_preservesFilter() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withPhone("93456789").build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone("93456789").build());
        Model expectedModel = new ModelManager(new ClientBook(model.getClientBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(editedPerson, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_duplicateEmailAndPhoneUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_EMAIL_AND_PHONE);
    }

    @Test
    public void execute_duplicateNameUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_NAME);
    }

    @Test
    public void execute_duplicateNameDifferingOnlyInCaseOrSpacingUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditCommand differentCaseCommand = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder()
                        .withName(firstPerson.getName().fullName.toUpperCase(Locale.ROOT)).build());
        EditCommand extraSpacingCommand = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName(firstPerson.getName().fullName.replace(" ", "  ")).build());

        assertCommandFailure(differentCaseCommand, model, EditCommand.MESSAGE_DUPLICATE_NAME);
        assertCommandFailure(extraSpacingCommand, model, EditCommand.MESSAGE_DUPLICATE_NAME);
    }

    @Test
    public void execute_duplicatePhoneUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone(firstPerson.getPhone().value).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PHONE);
    }

    @Test
    public void execute_duplicatePhoneWithDifferentFormattingUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String phone = firstPerson.getPhone().value;
        String formattedPhone = phone.substring(0, 4) + " " + phone.substring(4);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(formattedPhone).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PHONE);
    }

    @Test
    public void execute_duplicatePhoneWithMultipleNumbersInDifferentOrdersUnfilteredList_failure() {
        Person phoneOwner = new PersonBuilder().withPhone("home: 92849584 ph: 02848594")
                .withEmail("phone.owner@example.com").build();
        model.addPerson(phoneOwner);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone("ph: 0284 8594 home: 9284 9584").build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PHONE);
    }

    @Test
    public void execute_duplicateEmailUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withEmail(firstPerson.getEmail().value).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void execute_duplicateEmailWithDifferentDomainCasingUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String email = firstPerson.getEmail().value;
        int atSignIndex = email.indexOf('@');
        String emailWithUppercaseDomain = email.substring(0, atSignIndex + 1)
                + email.substring(atSignIndex + 1).toUpperCase(Locale.ROOT);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withEmail(emailWithUppercaseDomain).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void execute_duplicateEmailAndPhoneFilteredList_failure() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        // edit person in filtered list into a duplicate in client book
        Person personInList = model.getClientBook().getPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_EMAIL_AND_PHONE);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of client book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of client book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getClientBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
