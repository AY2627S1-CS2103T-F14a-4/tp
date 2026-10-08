package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same phone, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withEmail(VALID_EMAIL_BOB)
                .withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same email, all other attributes different -> returns true
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different email and phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void constructors_withoutFinancialInformation_fieldsAbsent() {
        Person[] persons = {
            new PersonBuilder().build(),
            new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getTags()),
            new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getTags(), Relationship.CLIENT),
            new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), Relationship.CLIENT)
        };
        for (Person person : persons) {
            assertTrue(person.getFinancialNeed().isEmpty());
            assertTrue(person.getPriority().isEmpty());
            assertTrue(person.getAreaOfInterest().isEmpty());
        }
    }

    @Test
    public void financialInformation_fieldsIndependentlyOptional() {
        Person needOnly = new PersonBuilder().withFinancialNeed("Retirement").build();
        assertEquals(Optional.of(new FinancialNeed("Retirement")), needOnly.getFinancialNeed());
        assertTrue(needOnly.getPriority().isEmpty());
        assertTrue(needOnly.getAreaOfInterest().isEmpty());

        Person priorityOnly = new PersonBuilder().withPriority("High").build();
        assertEquals(Optional.of(new Priority("High")), priorityOnly.getPriority());
        assertTrue(priorityOnly.getFinancialNeed().isEmpty());
        assertTrue(priorityOnly.getAreaOfInterest().isEmpty());

        Person interestOnly = new PersonBuilder().withAreaOfInterest("Insurance").build();
        assertEquals(Optional.of(new AreaOfInterest("Insurance")), interestOnly.getAreaOfInterest());
        assertTrue(interestOnly.getFinancialNeed().isEmpty());
        assertTrue(interestOnly.getPriority().isEmpty());
    }

    @Test
    public void personBuilder_copy_preservesFinancialInformationAndRelationship() {
        Person original = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getTags(),
                Relationship.CLIENT, new FinancialNeed("Retirement"), new Priority("High"),
                new AreaOfInterest("Insurance"));
        Person copy = new PersonBuilder(original).build();
        assertEquals(original.getFinancialNeed(), copy.getFinancialNeed());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.getAreaOfInterest(), copy.getAreaOfInterest());
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
        assertEquals(ALICE, new PersonBuilder(ALICE).build());
    }

    @Test
    public void equals_differentFinancialInformation_returnsFalse() {
        Person person = new PersonBuilder(ALICE).withFinancialNeed("Retirement").withPriority("High")
                .withAreaOfInterest("Insurance").build();
        Person[] differentPersons = {
            new PersonBuilder(person).withFinancialNeed("Education").build(),
            new PersonBuilder(person).withPriority("Low").build(),
            new PersonBuilder(person).withAreaOfInterest("Investments").build(),
            new PersonBuilder(ALICE).withPriority("High").withAreaOfInterest("Insurance").build(),
            new PersonBuilder(ALICE).withFinancialNeed("Retirement").withAreaOfInterest("Insurance").build(),
            new PersonBuilder(ALICE).withFinancialNeed("Retirement").withPriority("High").build()
        };
        for (Person different : differentPersons) {
            assertFalse(person.equals(different));
            assertFalse(different.equals(person));
            assertTrue(person.isSamePerson(different));
        }
    }

    @Test
    public void toString_financialInformationPresent_includesValues() {
        Person person = new PersonBuilder().withFinancialNeed("Retirement").withPriority("High")
                .withAreaOfInterest("Insurance").build();
        assertTrue(person.toString().contains("financialNeed=Retirement"));
        assertTrue(person.toString().contains("priority=High"));
        assertTrue(person.toString().contains("areaOfInterest=Insurance"));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", relationship=" + ALICE.getRelationship() + ", tags="
                + ALICE.getTags() + ", financialNeed=null, priority=null, areaOfInterest=null, interactionNotes=[]}";
        assertEquals(expected, ALICE.toString());
    }
}
