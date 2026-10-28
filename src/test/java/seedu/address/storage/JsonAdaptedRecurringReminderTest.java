package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecurringReminder;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedRecurringReminderTest {
    private static final String OLD_JSON =
            "{\"name\":\"Amy Bee\",\"phone\":\"85355255\",\"email\":\"amy@gmail.com\"}";

    @Test
    public void jsonRoundTrip_preservesMultipleRemindersAndEveryOptionalField() throws Exception {
        Person source = new PersonBuilder().withFinancialNeed("Retirement").withPriority("High")
                .withAreaOfInterest("Insurance").withPreferredContactChannel("Email")
                .withPreferredContactTiming("Morning").withTags("friend")
                .withInteractionNotes(new InteractionNote(LocalDate.of(2026, 1, 1), "Discussed plans"))
                .withNextAction(new NextAction("Call", LocalDate.of(2026, 1, 2), true))
                .withRecurringReminders(new RecurringReminder("Birthday", MonthDay.of(2, 29)),
                        new RecurringReminder("Renewal", MonthDay.of(12, 31))).build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(source));
        assertEquals(source, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
        assertEquals(source.getRecurringReminders().getFirst(), new JsonAdaptedRecurringReminder(
                source.getRecurringReminders().getFirst()).toModelType());
    }

    @Test
    public void missingNullOrEmptyReminderList_loadsOldData() throws Exception {
        for (String suffix : List.of("", ",\"recurringReminders\":null", ",\"recurringReminders\":[]")) {
            String json = OLD_JSON.substring(0, OLD_JSON.length() - 1) + suffix + "}";
            assertTrue(JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType()
                    .getRecurringReminders().isEmpty());
        }
    }

    @Test
    public void invalidDatesLabelsOrMissingFields_rejectsAdapter() {
        for (JsonAdaptedRecurringReminder reminder : List.of(
                new JsonAdaptedRecurringReminder(null, "01-01"),
                new JsonAdaptedRecurringReminder("Birthday", null),
                new JsonAdaptedRecurringReminder("Birthday", "02-30"),
                new JsonAdaptedRecurringReminder("Birthday", "2026-01-01"),
                new JsonAdaptedRecurringReminder("Birthday", "1-01"),
                new JsonAdaptedRecurringReminder(" ", "01-01"),
                new JsonAdaptedRecurringReminder("a".repeat(51), "01-01"))) {
            assertThrows(IllegalValueException.class, reminder::toModelType);
        }
    }

    @Test
    public void invalidNestedReminderOrDuplicateLabel_rejectsPerson() throws Exception {
        for (String value : List.of("[null]", "[{}]", "[{\"label\":\"Birthday\",\"date\":\"02-30\"}]",
                "[{\"label\":\"Birthday\",\"date\":\"01-01\"},{\"label\":\"Birthday\",\"date\":\"02-02\"}]")) {
            String json = OLD_JSON.substring(0, OLD_JSON.length() - 1) + ",\"recurringReminders\":" + value + "}";
            JsonAdaptedPerson adapted = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
            assertThrows(IllegalValueException.class, adapted::toModelType);
        }
    }
}
