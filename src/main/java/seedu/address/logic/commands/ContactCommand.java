package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PreferredContactChannel;
import seedu.address.model.person.PreferredContactTiming;
import seedu.address.storage.Storage;

/** Updates contact preferences for a person in the currently displayed list. */
public class ContactCommand extends Command {
    public static final String COMMAND_WORD = "contact";
    public static final String MESSAGE_USAGE = "contact INDEX [ch/CHANNEL] [tm/TIMING]";
    public static final String MESSAGE_NO_FIELDS = "At least one of ch/ or tm/ is required.";
    public static final String MESSAGE_SUCCESS = "Updated contact preferences for %s.";

    private final Index index;
    private final PreferredContactChannel channel;
    private final PreferredContactTiming timing;

    /** Creates a command; null preferences mean leave the corresponding field unchanged. */
    public ContactCommand(Index index, PreferredContactChannel channel, PreferredContactTiming timing) {
        requireNonNull(index);
        checkArgument(channel != null || timing != null, MESSAGE_NO_FIELDS);
        this.index = index;
        this.channel = channel;
        this.timing = timing;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (index.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = persons.get(index.getZeroBased());
        Person updated = original.withContactPreferences(channel, timing);
        model.setPerson(original, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ContactCommand command
                && index.equals(command.index) && Objects.equals(channel, command.channel)
                && Objects.equals(timing, command.timing);
    }
}
