package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.NextAction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedNextActionTest {
    @Test
    public void toModelType_roundTrip_preservesAllFieldsAndCompletion() throws Exception {
        for (boolean completed : new boolean[] {false, true}) {
            for (LocalDate date : List.of(LocalDate.of(1, 1, 1), LocalDate.of(2000, 1, 1),
                    LocalDate.of(9999, 12, 31))) {
                NextAction action = new NextAction("Call", date, completed);
                Person source = new PersonBuilder().withNextAction(action).build();
                String json = JsonUtil.toJsonString(new JsonAdaptedPerson(source));
                assertEquals(source, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
            }
        }
    }

    @Test
    public void toModelType_oldJsonOrNullFollowup_loadsWithoutFollowup() throws Exception {
        String oldJson = "{\"name\":\"Amy Bee\",\"phone\":\"85355255\",\"email\":\"amy@gmail.com\"}";
        assertTrue(JsonUtil.fromJsonString(oldJson, JsonAdaptedPerson.class).toModelType().getNextAction().isEmpty());
        Person source = new PersonBuilder().build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(source));
        assertEquals(source, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_invalidOrMissingFields_rejectsData() {
        for (JsonAdaptedNextAction action : List.of(
                new JsonAdaptedNextAction("Call", "2026-02-30", false),
                new JsonAdaptedNextAction("Call", "0000-01-01", false),
                new JsonAdaptedNextAction("Call", "0000-02-29", true),
                new JsonAdaptedNextAction("Call", "2026-2-03", false),
                new JsonAdaptedNextAction(" ", "2026-10-19", false),
                new JsonAdaptedNextAction("a".repeat(201), "2026-10-19", false),
                new JsonAdaptedNextAction(null, "2026-10-19", false),
                new JsonAdaptedNextAction("Call", null, false),
                new JsonAdaptedNextAction("Call", "2026-10-19", null))) {
            assertThrows(IllegalValueException.class, action::toModelType);
        }
    }

    @Test
    public void toModelType_invalidNestedFollowup_rejectsPerson() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(new PersonBuilder().build()));
        String invalid = json.replace("\"nextAction\" : null",
                "\"nextAction\" : {\"action\":\"Call\",\"dueDate\":\"2026-02-30\",\"completed\":false}");
        JsonAdaptedPerson adapted = JsonUtil.fromJsonString(invalid, JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, adapted::toModelType);
    }
}
