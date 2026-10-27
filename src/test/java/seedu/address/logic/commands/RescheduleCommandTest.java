package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

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
    private static final LocalDate DATE = LocalDate.of(2026, 10, 26);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-26T04:00:00Z"), ZoneOffset.UTC);
    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());

    @Test
    public void execute_dateOnly_reopensAndPreservesAllOtherFields() throws Exception {
        for (boolean completed : new boolean[] {false, true}) {
            Person original = new PersonBuilder(model.getFilteredPersonList().getFirst())
                    .withRelationship(Relationship.CLIENT).withFinancialNeed("Retirement")
                    .withPriority("High").withAreaOfInterest("Insurance").withTags("friend")
                    .withPreferredContactChannel("WhatsApp").withPreferredContactTiming("Evening")
                    .withInteractionNotes(new InteractionNote(DATE, "Discussed plans"))
                    .withNextAction(new NextAction("Call", DATE.plusDays(1), completed)).build();
            model.setPerson(model.getFilteredPersonList().getFirst(), original);
            Command command = new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null, CLOCK);
            assertEquals(String.format(RescheduleCommand.MESSAGE_SUCCESS, original.getName().fullName),
                    command.execute(model, null).getFeedbackToUser());
            assertEquals(original.withNextAction(new NextAction("Call", DATE, false)),
                    model.getFilteredPersonList().getFirst());
        }
    }

    @Test
    public void execute_optionalDescription_updatesExistingAction() throws Exception {
        Person original = model.getFilteredPersonList().getFirst().withNextAction(new NextAction("Call", DATE, false));
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        new RescheduleCommand(INDEX_FIRST_PERSON, DATE.plusDays(1), "Email proposal", CLOCK).execute(model, null);
        assertEquals(original.withNextAction(new NextAction("Email proposal", DATE.plusDays(1), false)),
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndPreservesFilter() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1).withNextAction(new NextAction("Call", DATE, false));
        model.setPerson(model.getFilteredPersonList().get(1), second);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new RescheduleCommand(INDEX_SECOND_PERSON, DATE.plusDays(1), null, CLOCK).execute(model, null);
        assertEquals(first, model.getClientBook().getPersonList().getFirst());
        assertEquals(second.withNextAction(new NextAction("Call", DATE.plusDays(1), false)),
                model.getFilteredPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
        assertCommandFailure(new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null, CLOCK), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_findWithNonconsecutiveIndices_targetsOriginalIndexAndRejectsHiddenClient() throws Exception {
        Person first = model.getClientBook().getPersonList().getFirst();
        Person hidden = model.getClientBook().getPersonList().get(1);
        Person third = model.getClientBook().getPersonList().get(2)
                .withNextAction(new NextAction("Call", DATE, true));
        model.setPerson(model.getClientBook().getPersonList().get(2), third);
        new ClientBookParser().parseCommand("find Alice Carl").execute(model, null);
        assertEquals(List.of(first, third), model.getFilteredPersonList());

        new RescheduleCommand(Index.fromOneBased(3), DATE.plusDays(1), null, CLOCK).execute(model, null);

        Person updatedThird = third.withNextAction(new NextAction("Call", DATE.plusDays(1), false));
        assertEquals(List.of(first, updatedThird), model.getFilteredPersonList());
        assertEquals(first, model.getClientBook().getPersonList().getFirst());
        assertEquals(hidden, model.getClientBook().getPersonList().get(1));
        assertEquals(updatedThird, model.getClientBook().getPersonList().get(2));
        assertCommandFailure(new RescheduleCommand(INDEX_SECOND_PERSON, DATE, "Email", CLOCK), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(List.of(first, updatedThird), model.getFilteredPersonList());
    }

    @Test
    public void execute_rescheduledFutureAction_disappearsFromDueView() throws Exception {
        Person original = model.getFilteredPersonList().getFirst().withNextAction(new NextAction("Call", DATE, false));
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        model.updateFilteredPersonList(new NextActionDuePredicate(DATE));
        new RescheduleCommand(INDEX_FIRST_PERSON, DATE.plusDays(1), null, CLOCK).execute(model, null);
        assertEquals(0, model.getFilteredPersonList().size());
        assertEquals(original.withNextAction(new NextAction("Call", DATE.plusDays(1), false)),
                model.getClientBook().getPersonList().getFirst());
    }

    @Test
    public void execute_completedFollowupRescheduledToday_appearsInDueViewAgain() throws Exception {
        Person original = model.getFilteredPersonList().getFirst()
                .withNextAction(new NextAction("Call", DATE.minusDays(1), true));
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        new DueCommand(CLOCK).execute(model, null);
        assertEquals(0, model.getFilteredPersonList().size());
        new ListCommand().execute(model, null);
        new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null, CLOCK).execute(model, null);
        new DueCommand(CLOCK).execute(model, null);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new NextAction("Call", DATE, false),
                model.getFilteredPersonList().getFirst().getNextAction().orElseThrow());
    }

    @Test
    public void execute_completedFollowupRescheduledTomorrow_isPendingAndNotDueYet() throws Exception {
        Person original = model.getFilteredPersonList().getFirst()
                .withNextAction(new NextAction("Call", DATE, true));
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        new RescheduleCommand(INDEX_FIRST_PERSON, DATE.plusDays(1), null, CLOCK).execute(model, null);
        NextAction updated = model.getFilteredPersonList().getFirst().getNextAction().orElseThrow();
        assertFalse(updated.isCompleted());
        assertEquals(DATE.plusDays(1), updated.getDueDate());
        new DueCommand(CLOCK).execute(model, null);
        assertEquals(0, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_pastDate_doesNotChangeModelOrFilter() {
        Person original = model.getFilteredPersonList().getFirst().withNextAction(new NextAction("Call", DATE, true));
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RescheduleCommand(INDEX_FIRST_PERSON, DATE.minusDays(1), "Email", CLOCK),
                model, RescheduleCommand.MESSAGE_PAST_DATE);
        assertEquals(original, model.getFilteredPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_clockZone_usesLocalDateAtExecution() throws Exception {
        Clock utc = Clock.fixed(Instant.parse("2026-10-25T17:00:00Z"), ZoneOffset.UTC);
        Clock singapore = utc.withZone(ZoneId.of("Asia/Singapore"));
        Person original = model.getFilteredPersonList().getFirst().withNextAction(new NextAction("Call", DATE, true));
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        assertCommandFailure(new RescheduleCommand(INDEX_FIRST_PERSON, DATE.minusDays(1), null, singapore),
                model, RescheduleCommand.MESSAGE_PAST_DATE);
        new RescheduleCommand(INDEX_FIRST_PERSON, DATE.minusDays(1), null, utc).execute(model, null);
        assertEquals(new NextAction("Call", DATE.minusDays(1), false),
                model.getFilteredPersonList().getFirst().getNextAction().orElseThrow());
    }

    @Test
    public void execute_invalidIndexOrMissingFollowup_doesNotChangeModel() {
        assertCommandFailure(new RescheduleCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1),
                DATE, null, CLOCK), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null, CLOCK), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null, CLOCK), model,
                String.format(RescheduleCommand.MESSAGE_NO_NEXT_ACTION,
                        model.getFilteredPersonList().getFirst().getName().fullName));
    }

    @Test
    public void equalsAndHashCode_compareAllExecutionFields() {
        RescheduleCommand command = new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null, CLOCK);
        RescheduleCommand equivalent = new RescheduleCommand(Index.fromOneBased(1), DATE, null, CLOCK);
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, new RescheduleCommand(INDEX_SECOND_PERSON, DATE, null, CLOCK));
        assertNotEquals(command, new RescheduleCommand(INDEX_FIRST_PERSON, DATE.plusDays(1), null, CLOCK));
        assertNotEquals(command, new RescheduleCommand(INDEX_FIRST_PERSON, DATE, "Call", CLOCK));
        assertNotEquals(command, new RescheduleCommand(INDEX_FIRST_PERSON, DATE, null,
                CLOCK.withZone(ZoneId.of("Asia/Singapore"))));
        assertNotEquals(command, null);
    }
}
