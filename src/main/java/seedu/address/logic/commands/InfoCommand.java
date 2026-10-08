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

/** Updates financial information for a person in the displayed list. */
public class InfoCommand extends Command {
    public static final String COMMAND_WORD = "info";
    public static final String MESSAGE_USAGE = "info INDEX [/fn FINANCIAL_NEED] [/pr PRIORITY] [/ai AREA_OF_INTEREST]";
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
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (index.getZeroBased() >= persons.size()) {
            throw new CommandException(String.format(MESSAGE_INVALID_INDEX, index.getOneBased()));
        }
        Person original = persons.get(index.getZeroBased());
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(),
                descriptor.getFinancialNeed().orElse(original.getFinancialNeed().orElse(null)),
                descriptor.getPriority().orElse(original.getPriority().orElse(null)),
                descriptor.getAreaOfInterest().orElse(original.getAreaOfInterest().orElse(null)),
                original.getInteractionNotes());
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

    /** Stores supplied fields; null means that the corresponding field is unspecified. */
    public static class InfoDescriptor {
        private FinancialNeed financialNeed;
        private Priority priority;
        private AreaOfInterest areaOfInterest;

        public InfoDescriptor() {}

        /** Copies the supplied fields. */
        public InfoDescriptor(InfoDescriptor source) {
            requireNonNull(source);
            financialNeed = source.financialNeed;
            priority = source.priority;
            areaOfInterest = source.areaOfInterest;
        }

        public void setFinancialNeed(FinancialNeed financialNeed) {
            this.financialNeed = financialNeed;
        }

        public Optional<FinancialNeed> getFinancialNeed() {
            return Optional.ofNullable(financialNeed);
        }

        public void setPriority(Priority priority) {
            this.priority = priority;
        }

        public Optional<Priority> getPriority() {
            return Optional.ofNullable(priority);
        }

        public void setAreaOfInterest(AreaOfInterest areaOfInterest) {
            this.areaOfInterest = areaOfInterest;
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
                    && Objects.equals(financialNeed, descriptor.financialNeed)
                    && Objects.equals(priority, descriptor.priority)
                    && Objects.equals(areaOfInterest, descriptor.areaOfInterest);
        }

        @Override
        public int hashCode() {
            return Objects.hash(financialNeed, priority, areaOfInterest);
        }
    }
}
