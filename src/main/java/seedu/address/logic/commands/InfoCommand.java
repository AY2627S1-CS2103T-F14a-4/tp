package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.AreaOfInterest;
import seedu.address.model.person.FinancialNeed;
import seedu.address.model.person.Person;
import seedu.address.model.person.Priority;
import seedu.address.storage.Storage;

/** Updates financial information for a person in the displayed list. */
public class InfoCommand extends Command {
    public static final String COMMAND_WORD = "info";
    public static final String MESSAGE_USAGE = "info INDEX [/fn VALUE|-] [/pr VALUE|-] [/ai VALUE|-]";
    public static final String MESSAGE_SUCCESS = "Client information for %s has been updated successfully.";
    public static final String MESSAGE_INVALID_INDEX = "No client exists at index %d.";

    private final Index index;
    private final InfoDescriptor descriptor;

    /** Creates a command with a defensive copy of the supplied fields. */
    public InfoCommand(Index index, InfoDescriptor descriptor) {
        requireAllNonNull(index, descriptor);
        this.index = index;
        this.descriptor = new InfoDescriptor(descriptor);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (index.getZeroBased() >= persons.size()) {
            throw new CommandException(String.format(MESSAGE_INVALID_INDEX, index.getOneBased()));
        }
        Person original = persons.get(index.getZeroBased());
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(),
                descriptor.isFinancialNeedSpecified() ? descriptor.getFinancialNeed().orElse(null)
                        : original.getFinancialNeed().orElse(null),
                descriptor.isPrioritySpecified() ? descriptor.getPriority().orElse(null)
                        : original.getPriority().orElse(null),
                descriptor.isAreaOfInterestSpecified() ? descriptor.getAreaOfInterest().orElse(null)
                        : original.getAreaOfInterest().orElse(null),
                original.getInteractionNotes(), original.getNextAction().orElse(null),
                original.getPreferredContactChannel().orElse(null),
                original.getPreferredContactTiming().orElse(null));
        model.setPerson(original, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof InfoCommand command
                && index.equals(command.index) && descriptor.equals(command.descriptor);
    }

    /**
     * Stores each field's supplied state separately from its value.
     * Unspecified fields are unchanged; specified null values explicitly clear fields.
     */
    public static class InfoDescriptor {
        private FinancialNeed financialNeed;
        private boolean financialNeedSpecified;
        private Priority priority;
        private boolean prioritySpecified;
        private AreaOfInterest areaOfInterest;
        private boolean areaOfInterestSpecified;

        public InfoDescriptor() {}

        /** Copies the supplied fields. */
        public InfoDescriptor(InfoDescriptor source) {
            requireNonNull(source);
            financialNeed = source.financialNeed;
            financialNeedSpecified = source.financialNeedSpecified;
            priority = source.priority;
            prioritySpecified = source.prioritySpecified;
            areaOfInterest = source.areaOfInterest;
            areaOfInterestSpecified = source.areaOfInterestSpecified;
        }

        /** Specifies a replacement value, or null to explicitly clear the field. */
        public void setFinancialNeed(FinancialNeed financialNeed) {
            this.financialNeed = financialNeed;
            financialNeedSpecified = true;
        }

        public boolean isFinancialNeedSpecified() {
            return financialNeedSpecified;
        }

        public Optional<FinancialNeed> getFinancialNeed() {
            return Optional.ofNullable(financialNeed);
        }

        /** Specifies a replacement value, or null to explicitly clear the field. */
        public void setPriority(Priority priority) {
            this.priority = priority;
            prioritySpecified = true;
        }

        public boolean isPrioritySpecified() {
            return prioritySpecified;
        }

        public Optional<Priority> getPriority() {
            return Optional.ofNullable(priority);
        }

        /** Specifies a replacement value, or null to explicitly clear the field. */
        public void setAreaOfInterest(AreaOfInterest areaOfInterest) {
            this.areaOfInterest = areaOfInterest;
            areaOfInterestSpecified = true;
        }

        public boolean isAreaOfInterestSpecified() {
            return areaOfInterestSpecified;
        }

        public Optional<AreaOfInterest> getAreaOfInterest() {
            return Optional.ofNullable(areaOfInterest);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            return other instanceof InfoDescriptor descriptor
                    && financialNeedSpecified == descriptor.financialNeedSpecified
                    && Objects.equals(financialNeed, descriptor.financialNeed)
                    && prioritySpecified == descriptor.prioritySpecified
                    && Objects.equals(priority, descriptor.priority)
                    && areaOfInterestSpecified == descriptor.areaOfInterestSpecified
                    && Objects.equals(areaOfInterest, descriptor.areaOfInterest);
        }

        @Override
        public int hashCode() {
            return Objects.hash(financialNeed, priority, areaOfInterest,
                    financialNeedSpecified, prioritySpecified, areaOfInterestSpecified);
        }
    }
}
