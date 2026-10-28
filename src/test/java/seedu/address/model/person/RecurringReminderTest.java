package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class RecurringReminderTest {
    private static final RecurringReminder BIRTHDAY = new RecurringReminder("Birthday", MonthDay.of(2, 29));

    @Test
    public void nextOccurrence_handlesTodayRolloverAndLeapYears() {
        assertEquals(LocalDate.of(2026, 2, 28), BIRTHDAY.nextOccurrence(LocalDate.of(2026, 2, 28)));
        assertEquals(LocalDate.of(2027, 2, 28), BIRTHDAY.nextOccurrence(LocalDate.of(2026, 3, 1)));
        assertEquals(LocalDate.of(2028, 2, 29), BIRTHDAY.nextOccurrence(LocalDate.of(2028, 2, 28)));
        assertEquals(LocalDate.of(2028, 2, 29), BIRTHDAY.nextOccurrence(LocalDate.of(2027, 12, 31)));
    }

    @Test
    public void dateAndLabelValidation_enforcesYearlyFormatAndShortLabels() {
        assertEquals(MonthDay.of(2, 29), RecurringReminder.parseDate("02-29"));
        assertEquals("Birthday", new RecurringReminder(" Birthday ", MonthDay.of(1, 1)).getLabel());
        for (String date : List.of("2-29", "2026-02-28", "02-30", "13-01", "00-01", "01-00", "")) {
            assertThrows(IllegalArgumentException.class, () -> RecurringReminder.parseDate(date));
        }
        assertThrows(IllegalArgumentException.class, () -> RecurringReminder.parseDate(null));
        for (String label : List.of(" ", "a".repeat(51), "A/B", "A\nB", "A\tB")) {
            assertThrows(IllegalArgumentException.class, () -> new RecurringReminder(label, MonthDay.of(1, 1)));
        }
        assertEquals(50, new RecurringReminder("a".repeat(50), MonthDay.of(1, 1)).getLabel().length());
        assertThrows(NullPointerException.class, () -> new RecurringReminder(null, MonthDay.of(1, 1)));
        assertThrows(NullPointerException.class, () -> new RecurringReminder("Birthday", null));
    }

    @Test
    public void personReminderList_isImmutableAndCopiesPreserveUnrelatedFields() {
        Person original = new PersonBuilder().withFinancialNeed("Retirement").withPriority("High")
                .withAreaOfInterest("Insurance").withPreferredContactChannel("Email")
                .withPreferredContactTiming("Morning").withTags("friend").build();
        Person updated = original.withRecurringReminder(BIRTHDAY);
        assertTrue(original.getRecurringReminders().isEmpty());
        assertEquals(List.of(BIRTHDAY), updated.getRecurringReminders());
        assertThrows(UnsupportedOperationException.class, () -> updated.getRecurringReminders().clear());
        assertEquals(updated, new PersonBuilder(original).withRecurringReminders(BIRTHDAY).build());
        RecurringReminder replacement = new RecurringReminder("Birthday", MonthDay.of(3, 1));
        assertEquals(List.of(replacement), updated.withRecurringReminder(replacement).getRecurringReminders());
        assertEquals(List.of(replacement), updated.withRecurringReminder(
                new RecurringReminder("birthday", MonthDay.of(3, 1))).getRecurringReminders());
        assertEquals(original, updated.withoutRecurringReminder("bIRTHDAY"));
        assertEquals(List.of(BIRTHDAY), updated.getRecurringReminders());
        assertThrows(IllegalArgumentException.class, () -> updated.withoutRecurringReminder("Renewal"));
        List<RecurringReminder> input = new ArrayList<>(List.of(BIRTHDAY));
        Person copied = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(), null, null, null, List.of(), null, null, null, input);
        assertThrows(IllegalArgumentException.class, () -> new Person(original.getName(), original.getPhone(),
                original.getEmail(), original.getTags(), original.getRelationship(), null, null, null, List.of(),
                null, null, null, List.of(BIRTHDAY, BIRTHDAY)));
        input.clear();
        assertEquals(List.of(BIRTHDAY), copied.getRecurringReminders());
        assertNotEquals(original, updated);
        assertEquals(updated.hashCode(), new PersonBuilder(updated).build().hashCode());
    }

    @Test
    public void personConstructor_rejectsCaseVariantLabelsUsingLocaleIndependentMatching() {
        for (String[] labels : List.of(new String[] {"Birthday", "birthday"}, new String[] {"İ", "i"},
                new String[] {"Σ", "ς"})) {
            RecurringReminder first = new RecurringReminder(labels[0], MonthDay.of(1, 1));
            RecurringReminder second = new RecurringReminder(labels[1], MonthDay.of(2, 2));
            assertThrows(IllegalArgumentException.class, () -> new PersonBuilder()
                    .withRecurringReminders(first, second).build());
            Person person = new PersonBuilder().withRecurringReminders(first).build();
            Person updated = person.withRecurringReminder(second);
            assertEquals(List.of(new RecurringReminder(labels[0], second.getDate())), updated.getRecurringReminders());
            assertTrue(updated.withoutRecurringReminder(labels[1]).getRecurringReminders().isEmpty());
        }
    }

    @Test
    public void duePredicate_includesThirtyDayBoundaryTodayAndNewYearOnly() {
        LocalDate today = LocalDate.of(2026, 12, 20);
        for (int offset : new int[] {0, 1, 30}) {
            Person person = new PersonBuilder().build().withRecurringReminder(
                    new RecurringReminder("Renewal", MonthDay.from(today.plusDays(offset))));
            assertTrue(new NextActionDuePredicate(today).test(person));
        }
        for (int offset : new int[] {-1, 31}) {
            Person person = new PersonBuilder().build().withRecurringReminder(
                    new RecurringReminder("Renewal", MonthDay.from(today.plusDays(offset))));
            assertFalse(new NextActionDuePredicate(today).test(person));
        }
        Person leap = new PersonBuilder().build().withRecurringReminder(BIRTHDAY);
        assertTrue(new NextActionDuePredicate(LocalDate.of(2026, 2, 28)).test(leap));
        assertFalse(new NextActionDuePredicate(LocalDate.of(2026, 3, 1)).test(leap));
    }
}
