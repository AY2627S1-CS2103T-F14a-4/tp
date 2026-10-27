package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalClientBook;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class TagCommandTest {
    private final Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());

    @Test
    public void execute_newTags_addsTagsAndPreservesFields() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Set<Tag> tagsToAdd = Set.of(new Tag("friend"), new Tag("priority"));

        new TagCommand(INDEX_FIRST_PERSON, tagsToAdd).execute(model, null);

        Person expected = new PersonBuilder(original).withTags("friends", "friend", "priority").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_existingTag_failure() {
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("FRIENDS")));

        assertCommandFailure(command, model,
                String.format(TagCommand.MESSAGE_DUPLICATE_TAGS, "[FRIENDS]"));
    }

    @Test
    public void execute_newAndExistingTags_failureWithoutAddingNewTag() {
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON,
                Set.of(new Tag("priority"), new Tag("friends")));

        assertCommandFailure(command, model,
                String.format(TagCommand.MESSAGE_DUPLICATE_TAGS, "[friends]"));
    }

    @Test
    public void execute_filteredIndex_updatesDisplayedPersonOnly() throws Exception {
        Person first = model.getFilteredPersonList().get(0);
        Person target = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new TagCommand(INDEX_SECOND_PERSON, Set.of(new Tag("priority"))).execute(model, null);

        assertEquals(first, model.getClientBook().getPersonList().get(0));
        assertEquals(new PersonBuilder(target).withTags("owesMoney", "friends", "priority").build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_invalidDisplayedIndex_failure() {
        assertCommandFailure(new TagCommand(INDEX_SECOND_PERSON, Set.of(new Tag("priority"))),
                new ModelManager(), Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
