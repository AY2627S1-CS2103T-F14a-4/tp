package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public static final String MESSAGE_USAGE = "contact INDEX [ch/CHANNEL|-] [tm/TIMING|-]";
    public static final String MESSAGE_NO_FIELDS = "At least one of ch/ or tm/ is required.";
    public static final String MESSAGE_SUCCESS = "Updated contact preferences for %s.";

    private final Index index;
    private final ContactDescriptor descriptor;

    /** Creates a command; null preferences mean leave the corresponding field unchanged. */
    public ContactCommand(Index index, PreferredContactChannel channel, PreferredContactTiming timing) {
        this(index, descriptorForValues(channel, timing));
    }

    /** Creates a command with a defensive copy of the supplied changes. */
    public ContactCommand(Index index, ContactDescriptor descriptor) {
        requireNonNull(index);
        requireNonNull(descriptor);
        checkArgument(descriptor.isChannelSpecified() || descriptor.isTimingSpecified(), MESSAGE_NO_FIELDS);
        this.index = index;
        this.descriptor = new ContactDescriptor(descriptor);
    }

    private static ContactDescriptor descriptorForValues(PreferredContactChannel channel,
            PreferredContactTiming timing) {
        ContactDescriptor descriptor = new ContactDescriptor();
        if (channel != null) {
            descriptor.setChannel(channel);
        }
        if (timing != null) {
            descriptor.setTiming(timing);
        }
        return descriptor;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (index.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = persons.get(index.getZeroBased());
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(), original.getFinancialNeed().orElse(null),
                original.getPriority().orElse(null), original.getAreaOfInterest().orElse(null),
                original.getInteractionNotes(), original.getNextAction().orElse(null),
                descriptor.isChannelSpecified() ? descriptor.getChannel().orElse(null)
                        : original.getPreferredContactChannel().orElse(null),
                descriptor.isTimingSpecified() ? descriptor.getTiming().orElse(null)
                        : original.getPreferredContactTiming().orElse(null));
        model.setPerson(original, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ContactCommand command
                && index.equals(command.index) && descriptor.equals(command.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), descriptor);
    }

    /** Stores omitted, replacement, and explicit clear states separately for each preference. */
    public static class ContactDescriptor {
        private PreferredContactChannel channel;
        private boolean channelSpecified;
        private PreferredContactTiming timing;
        private boolean timingSpecified;

        public ContactDescriptor() {}

        /** Copies the supplied changes; preference values are immutable. */
        public ContactDescriptor(ContactDescriptor source) {
            requireNonNull(source);
            channel = source.channel;
            channelSpecified = source.channelSpecified;
            timing = source.timing;
            timingSpecified = source.timingSpecified;
        }

        /** Specifies a replacement channel, or null to explicitly clear it. */
        public void setChannel(PreferredContactChannel channel) {
            this.channel = channel;
            channelSpecified = true;
        }

        public boolean isChannelSpecified() {
            return channelSpecified;
        }

        public Optional<PreferredContactChannel> getChannel() {
            return Optional.ofNullable(channel);
        }

        /** Specifies replacement timing, or null to explicitly clear it. */
        public void setTiming(PreferredContactTiming timing) {
            this.timing = timing;
            timingSpecified = true;
        }

        public boolean isTimingSpecified() {
            return timingSpecified;
        }

        public Optional<PreferredContactTiming> getTiming() {
            return Optional.ofNullable(timing);
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof ContactDescriptor changes
                    && channelSpecified == changes.channelSpecified && Objects.equals(channel, changes.channel)
                    && timingSpecified == changes.timingSpecified && Objects.equals(timing, changes.timing);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channel, channelSpecified, timing, timingSpecified);
        }
    }
}
