package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.ClientBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.NextActionDuePredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;
import seedu.address.testutil.PersonBuilder;

public class RescheduleCommandTest {
    private static final LocalDate DATE = LocalDate.of(2000, 1, 1);
    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
    private final ClientBookParser parser = new ClientBookParser();

    @Test
    public void execute_dateOnly_preservesDescriptionCompletionAndAllOtherFields() throws Exception {
        for (boolean completed : new boolean[] {false, true}) {
            Person original = new PersonBuilder(model.getFilteredPersonList().getFirst())
                    .withRelationship(Relationship.CLIENT).withFinancialNeed("Retirement")
                    .withPriority("High").withAreaOfInterest("Insurance").withTags("friend")
                    .withPreferredContactChannel("WhatsApp").withPreferredContactTiming("Evening")
                    .withInteractionNotes(new InteractionNote(DATE, "Discussed plans"))
                    .withNextAction(new NextAction("Call", DATE.plusDays(1), completed)).build();
            model.setPerson(model.getFilteredPersonList().getFirst(), original);
            Command command = parser.parseCommand("reschedule 1 d/2000-01-01");
            assertEquals(String.format(RescheduleCommand.MESSAGE_SUCCESS, original.getName().fullName),
                    command.execute(model, null).getFeedbackToUser());
            assertEquals(original.withNextAction(new NextAction("Call", DATE, completed)),
                    model.getFilteredPersonList().getFirst());
        }
    }

    @Test
    public void execute_optionalDescription_updatesExistingAction() throws Exception {
        Person original = model.getFilteredPersonList().getFirst().withNextAction(new NextAction("Call", DATE, false));
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        parser.parseCommand("reschedule 1 a/Email proposal d/2026-10-30").execute(model, null);
        assertEquals(original.withNextAction(new NextAction("Email proposal", LocalDate.of(2026, 10, 30), false)),
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndPreservesFilter() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1).withNextAction(new NextAction("Call", DATE, false));
        model.setPerson(model.getFilteredPersonList().get(1), second);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new RescheduleCommand(INDEX_FIRST_PERSON, DATE.plusDays(1), null).execute(model, null);
        assertEquals(first, model.getClientBook().getPersonList().getFirst());
        assertEquals(second.withNextAction(new NextAction("Call", DATE.plusDays(1), false)),
                model.getFilteredPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
        assertCommandFailure(new RescheduleCommand(INDEX_SECOND_PERSON, DATE, null), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_rescheduledFutureAction_disappearsFromDueView() throws Exception {
        Person original = model.getFilteredPersonList().getFirst().withNextAction(new NextAction("Call", DATE, false));
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        model.updateFilteredPersonList(new NextActionDuePredicate(DATE));
        new RescheduleCommand(INDEX_FIRST_PERSON, DATE.plusDays(1), null).execute(model, null);
        assertEquals(0, model.getFilteredPersonList().size());
        assertEquals(original.withNextAction(new NextAction("Call", DATE.plusDays(1), false)),
                model.getClientBook().getPersonList().getFirst());
    }

    @Test
    public void execute_invalidIndexOrMissingFollowup_doesNotChangeModel() {
        assertCommandFailure(new RescheduleCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1),
                DATE, null), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null), model,
                String.format(RescheduleCommand.MESSAGE_NO_NEXT_ACTION,
                        model.getFilteredPersonList().getFirst().getName().fullName));
    }

    @Test
    public void equalsAndHashCode_compareIndexDateAndOptionalAction() {
        RescheduleCommand command = new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null);
        RescheduleCommand equivalent = new RescheduleCommand(Index.fromOneBased(1), DATE, null);
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, new RescheduleCommand(INDEX_SECOND_PERSON, DATE, null));
        assertNotEquals(command, new RescheduleCommand(INDEX_FIRST_PERSON, DATE.plusDays(1), null));
        assertNotEquals(command, new RescheduleCommand(INDEX_FIRST_PERSON, DATE, "Call"));
        assertNotEquals(command, null);
    }
}
