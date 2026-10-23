package seedu.address.testutil;

import seedu.address.model.ClientBook;
import seedu.address.model.person.Person;

/**
 * A utility class to help with building ClientBook objects.
 * Example usage: <br>
 *     {@code ClientBook ab = new ClientBookBuilder().withPerson("John", "Doe").build();}
 */
public class ClientBookBuilder {

    private ClientBook clientBook;

    public ClientBookBuilder() {
        clientBook = new ClientBook();
    }

    public ClientBookBuilder(ClientBook clientBook) {
        this.clientBook = clientBook;
    }

    /**
     * Adds a new {@code Person} to the {@code ClientBook} that we are building.
     */
    public ClientBookBuilder withPerson(Person person) {
        clientBook.addPerson(person);
        return this;
    }

    public ClientBook build() {
        return clientBook;
    }
}
