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

/** Adds one or more tags to a person in the currently displayed list. */
public class TagCommand extends Command {
    public static final String COMMAND_WORD = "tag";
    public static final String MESSAGE_USAGE = "Parameters: INDEX TAG [MORE_TAGS...]";
    public static final String MESSAGE_SUCCESS = "Added tags to %s: %s";
    public static final String MESSAGE_DUPLICATE_TAGS = "This contact already has the following tag(s): %s";
    public static final String MESSAGE_DUPLICATE_INPUT_TAGS = "Duplicate tags provided: %s";

    private final Index index;
    private final Set<Tag> tags;

    /** Creates a command targeting the displayed index. */
    public TagCommand(Index index, Set<Tag> tags) {
        requireAllNonNull(index, tags);
        this.index = index;
        this.tags = Set.copyOf(tags);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        Person personToTag = getVisiblePersonAtIndex(model, index,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        Set<Tag> duplicateTags = new HashSet<>(tags);
        duplicateTags.retainAll(personToTag.getTags());
        if (!duplicateTags.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_TAGS, formatTags(duplicateTags)));
        }

        Set<Tag> updatedTags = new HashSet<>(personToTag.getTags());
        updatedTags.addAll(tags);
        model.setPerson(personToTag, personToTag.withTags(updatedTags));
        return new CommandResult(String.format(MESSAGE_SUCCESS, personToTag.getName(), formatTags(tags)));
    }

    private static String formatTags(Set<Tag> tags) {
        return tags.stream().map(tag -> tag.tagName).sorted().toList().toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TagCommand otherCommand
                && index.equals(otherCommand.index) && tags.equals(otherCommand.tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("tags", tags).toString();
    }
}
