package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.ClientBook;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.person.Person;

/**
 * An Immutable ClientBook that is serializable to JSON format.
 */
@JsonRootName(value = "clientbook")
class JsonSerializableClientBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableClientBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableClientBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        this.persons.addAll(persons);
    }

    /**
     * Converts a given {@code ReadOnlyClientBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableClientBook}.
     */
    public JsonSerializableClientBook(ReadOnlyClientBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this client book into the model's {@code ClientBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public ClientBook toModelType() throws IllegalValueException {
        ClientBook clientBook = new ClientBook();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (clientBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            clientBook.addPerson(person);
        }
        return clientBook;
    }

}
