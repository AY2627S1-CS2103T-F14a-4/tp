package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.Storage;

/** Deletes all or selected tags from a person in the currently displayed list. */
public class TagDeleteCommand extends Command implements ConfirmableCommand {
    public static final String COMMAND_WORD = "tagdelete";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes all or selected tags from the person identified by the index in the displayed list.\n"
            + "Parameters: INDEX [TAG...]\n"
            + "Examples: " + COMMAND_WORD + " 1, " + COMMAND_WORD + " 1 friend referral";
    public static final String MESSAGE_CONFIRMATION = "Are you sure you want to delete this?\n"
            + "Contact: %s\n"
            + "Tags to delete: %s\n"
            + "Enter Y/Yes to confirm or N/No to cancel.";
    public static final String MESSAGE_INVALID_CONFIRMATION = ConfirmableCommand.MESSAGE_INVALID_CONFIRMATION;
    public static final String MESSAGE_CANCELLED = "Tag deletion cancelled.";
    public static final String MESSAGE_DELETE_ALL_SUCCESS = "Deleted all tags from %s.";
    public static final String MESSAGE_DELETE_SELECTED_SUCCESS = "Deleted tags from %s: %s";
    public static final String MESSAGE_NO_TAGS = "This contact has no tags to delete.";
    public static final String MESSAGE_TAGS_NOT_FOUND = "This contact does not have the following tag(s): %s";

    private final Index index;
    private final Set<Tag> tagsToDelete;

    /** Creates a command that deletes all tags when {@code tagsToDelete} is empty. */
    public TagDeleteCommand(Index index, Set<Tag> tagsToDelete) {
        requireAllNonNull(index, tagsToDelete);
        this.index = index;
        this.tagsToDelete = Set.copyOf(tagsToDelete);
    }

    /** Returns a confirmation message describing the tags that will be deleted without changing the model. */
    @Override
    public String getConfirmationMessage(Model model) throws CommandException {
        Person person = getValidatedPerson(model);
        Set<Tag> actualTagsToDelete = new HashSet<>(person.getTags());
        if (!tagsToDelete.isEmpty()) {
            actualTagsToDelete.retainAll(tagsToDelete);
        }
        return String.format(MESSAGE_CONFIRMATION, person.getName(), formatTags(actualTagsToDelete));
    }

    @Override
    public String getCancellationMessage() {
        return MESSAGE_CANCELLED;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        Person personToUpdate = getValidatedPerson(model);
        Set<Tag> updatedTags = new HashSet<>(personToUpdate.getTags());
        if (tagsToDelete.isEmpty()) {
            updatedTags.clear();
        } else {
            updatedTags.removeAll(tagsToDelete);
        }
        model.setPerson(personToUpdate, personToUpdate.withTags(updatedTags));

        String successMessage = tagsToDelete.isEmpty()
                ? String.format(MESSAGE_DELETE_ALL_SUCCESS, personToUpdate.getName())
                : String.format(MESSAGE_DELETE_SELECTED_SUCCESS, personToUpdate.getName(), formatTags(tagsToDelete));
        return new CommandResult(successMessage);
    }

    private Person getValidatedPerson(Model model) throws CommandException {
        requireAllNonNull(model);
        Person person = getVisiblePersonAtIndex(model, index,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        if (person.getTags().isEmpty()) {
            throw new CommandException(MESSAGE_NO_TAGS);
        }

        Set<Tag> missingTags = new HashSet<>(tagsToDelete);
        missingTags.removeAll(person.getTags());
        if (!missingTags.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_TAGS_NOT_FOUND, formatTags(missingTags)));
        }
        return person;
    }

    private static String formatTags(Set<Tag> tags) {
        return tags.stream().map(tag -> tag.tagName).sorted().toList().toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TagDeleteCommand otherCommand
                && index.equals(otherCommand.index) && tagsToDelete.equals(otherCommand.tagsToDelete);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("tagsToDelete", tagsToDelete).toString();
    }
}
