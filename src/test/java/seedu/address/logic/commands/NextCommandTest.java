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

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.ClientBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.Relationship;
import seedu.address.testutil.PersonBuilder;

public class NextCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2000, 1, 1);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2000-01-01T04:00:00Z"), ZoneOffset.UTC);
    private static final NextAction ACTION = new NextAction("Call", TODAY, false);
    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
    private final ClientBookParser parser = new ClientBookParser();

    @Test
    public void execute_newAction_preservesAllOtherFields() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().getFirst())
                .withRelationship(Relationship.CLIENT).withFinancialNeed("Retirement")
                .withPriority("High").withAreaOfInterest("Insurance").withTags("friend")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2000, 1, 1), "Discussed plans")).build();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        assertEquals(String.format(NextCommand.MESSAGE_SUCCESS, original.getName().fullName),
                new NextCommand(INDEX_FIRST_PERSON, ACTION, CLOCK).execute(model, null).getFeedbackToUser());
        assertEquals(new PersonBuilder(original).withNextAction(ACTION).build(),
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_replaceCompletedOrPendingAction_resetsCompletion() throws Exception {
        for (boolean completed : new boolean[] {false, true}) {
            Person original = model.getFilteredPersonList().getFirst();
            model.setPerson(original, original.withNextAction(new NextAction("Email", TODAY.minusDays(1), completed)));
            new NextCommand(INDEX_FIRST_PERSON, ACTION, CLOCK).execute(model, null);
            assertEquals(ACTION, model.getFilteredPersonList().getFirst().getNextAction().orElseThrow());
            assertFalse(model.getFilteredPersonList().getFirst().getNextAction().orElseThrow().isCompleted());
        }
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new NextCommand(INDEX_SECOND_PERSON, ACTION, CLOCK).execute(model, null);
        assertEquals(first, model.getClientBook().getPersonList().getFirst());
        assertEquals(second.withNextAction(ACTION), model.getFilteredPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
        assertCommandFailure(new NextCommand(INDEX_FIRST_PERSON, ACTION, CLOCK), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndex_doesNotChangeModel() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new NextCommand(invalidIndex, ACTION, CLOCK), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new NextCommand(INDEX_FIRST_PERSON, ACTION, CLOCK), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_yesterday_newOrReplacementActionRejectedWithoutMutation() {
        NextAction yesterday = new NextAction("Email", TODAY.minusDays(1), false);
        assertCommandFailure(new NextCommand(INDEX_FIRST_PERSON, yesterday, CLOCK), model,
                NextCommand.MESSAGE_PAST_DATE);
        Person original = model.getFilteredPersonList().getFirst().withNextAction(ACTION.markCompleted());
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new NextCommand(INDEX_FIRST_PERSON, yesterday, CLOCK), model,
                NextCommand.MESSAGE_PAST_DATE);
        assertEquals(original, model.getFilteredPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_tomorrow_acceptsFutureAction() throws Exception {
        NextAction tomorrow = new NextAction("Email", TODAY.plusDays(1), false);
        new NextCommand(INDEX_FIRST_PERSON, tomorrow, CLOCK).execute(model, null);
        assertEquals(tomorrow, model.getFilteredPersonList().getFirst().getNextAction().orElseThrow());
    }

    @Test
    public void execute_localDateBoundary_usesClockZoneAtExecution() throws Exception {
        Clock utc = Clock.fixed(Instant.parse("2000-01-01T17:00:00Z"), ZoneOffset.UTC);
        Clock singapore = utc.withZone(ZoneId.of("Asia/Singapore"));
        assertCommandFailure(new NextCommand(INDEX_FIRST_PERSON, ACTION, singapore), model,
                NextCommand.MESSAGE_PAST_DATE);
        new NextCommand(INDEX_FIRST_PERSON, ACTION, utc).execute(model, null);
        assertEquals(ACTION, model.getFilteredPersonList().getFirst().getNextAction().orElseThrow());
    }

    @Test
    public void execute_editInfoAndNote_preserveFollowup() throws Exception {
        for (boolean completed : new boolean[] {false, true}) {
            NextAction action = new NextAction("Call", ACTION.getDueDate(), completed);
            Person original = model.getFilteredPersonList().getFirst();
            model.setPerson(original, original.withNextAction(action));
            for (String command : new String[] {"edit 1 p/81234567", "info 1 fn/Retirement",
                "note 1 d/2000-01-01 n/Discussed plans with completed follow-up: " + completed}) {
                parser.parseCommand(command).execute(model, null);
                assertEquals(action, model.getFilteredPersonList().getFirst().getNextAction().orElseThrow());
            }
        }
    }

    @Test
    public void equals_comparesIndexActionAndClock() {
        NextCommand command = new NextCommand(INDEX_FIRST_PERSON, ACTION, CLOCK);
        assertEquals(command, new NextCommand(INDEX_FIRST_PERSON, ACTION, CLOCK));
        assertNotEquals(command, new NextCommand(INDEX_SECOND_PERSON, ACTION, CLOCK));
        assertNotEquals(command, new NextCommand(INDEX_FIRST_PERSON,
                new NextAction("Email", ACTION.getDueDate(), false), CLOCK));
        assertNotEquals(command, new NextCommand(INDEX_FIRST_PERSON, ACTION,
                CLOCK.withZone(ZoneId.of("Asia/Singapore"))));
        assertNotEquals(command, null);
    }

    @Test
    public void toStringMethod() {
        NextCommand nextCommand = new NextCommand(INDEX_FIRST_PERSON, ACTION, CLOCK);
        String expected = NextCommand.class.getCanonicalName()
                + "{index=" + INDEX_FIRST_PERSON + ", nextAction=" + ACTION + ", clock=" + CLOCK + "}";
        assertEquals(expected, nextCommand.toString());
    }

}
