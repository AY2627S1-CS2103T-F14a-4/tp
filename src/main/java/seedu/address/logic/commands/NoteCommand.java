package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/** Appends a dated note to a person in the currently displayed list. */
public class NoteCommand extends Command {
    public static final String COMMAND_WORD = "note";
    public static final String MESSAGE_USAGE = "note INDEX d/yyyy-MM-dd n/NOTE";
    public static final String MESSAGE_SUCCESS = "Added interaction note for %s.";
    public static final String MESSAGE_DUPLICATE_NOTE = "This interaction note already exists for this contact.";
    public static final String MESSAGE_FUTURE_DATE = "Interaction date cannot be in the future.";

    private final Index index;
    private final InteractionNote note;

    /** Creates a command targeting the displayed index. */
    public NoteCommand(Index index, InteractionNote note) {
        requireAllNonNull(index, note);
        this.index = index;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);

        if (note.getDate().isAfter(LocalDate.now())) {
            throw new CommandException(MESSAGE_FUTURE_DATE);
        }

        Person original = getVisiblePersonAtIndex(model, index,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        if (original.getInteractionNotes().contains(note)) {
            throw new CommandException(MESSAGE_DUPLICATE_NOTE);
        }
        List<InteractionNote> notes = new ArrayList<>(original.getInteractionNotes());
        notes.add(note);
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(), original.getFinancialNeed().orElse(null),
                original.getPriority().orElse(null), original.getAreaOfInterest().orElse(null), notes,
                original.getNextAction().orElse(null),
                original.getPreferredContactChannel().orElse(null), original.getPreferredContactTiming().orElse(null));
        model.setPerson(original, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteCommand command
                && index.equals(command.index) && note.equals(command.note);
    }
}
