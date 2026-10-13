package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;

/** Appends a dated note to a person in the currently displayed list. */
public class NoteCommand extends Command {
    public static final String COMMAND_WORD = "note";
    public static final String MESSAGE_USAGE = "note INDEX d/yyyy-MM-dd n/NOTE";
    public static final String MESSAGE_SUCCESS = "Added interaction note for %s.";

    private final Index index;
    private final InteractionNote note;

    /** Creates a command targeting the displayed index. */
    public NoteCommand(Index index, InteractionNote note) {
        requireAllNonNull(index, note);
        this.index = index;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireAllNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        List<InteractionNote> notes = new ArrayList<>(original.getInteractionNotes());
        notes.add(note);
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(), original.getFinancialNeed().orElse(null),
                original.getPriority().orElse(null), original.getAreaOfInterest().orElse(null), notes);
        model.setPerson(original, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteCommand command
                && index.equals(command.index) && note.equals(command.note);
    }
}
