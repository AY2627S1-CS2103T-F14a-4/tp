package seedu.address.logic.commands;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/** Edits the text of a dated note belonging to a displayed person. */
public class NoteEditCommand extends Command {
    public static final String COMMAND_WORD = "noteedit";
    public static final String MESSAGE_USAGE = "noteedit CONTACT_INDEX NOTE_INDEX n/NEW_NOTE_CONTENT";
    public static final String MESSAGE_SUCCESS = "Updated interaction note %d for %s.";
    public static final String MESSAGE_INVALID_NOTE_INDEX = "The interaction note index provided is invalid.";

    private final Index contactIndex;
    private final Index noteIndex;
    private final String text;

    /** Creates a command that preserves the selected note's date. */
    public NoteEditCommand(Index contactIndex, Index noteIndex, String text) {
        requireAllNonNull(contactIndex, noteIndex, text);
        checkArgument(!text.isBlank(), InteractionNote.MESSAGE_BLANK);
        this.contactIndex = contactIndex;
        this.noteIndex = noteIndex;
        this.text = text.strip();
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        Person original = getVisiblePersonAtIndex(model, contactIndex,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        List<InteractionNote> notes = new ArrayList<>(original.getInteractionNotes());
        if (noteIndex.getZeroBased() >= notes.size()) {
            throw new CommandException(MESSAGE_INVALID_NOTE_INDEX);
        }
        InteractionNote originalNote = notes.get(noteIndex.getZeroBased());
        notes.set(noteIndex.getZeroBased(), new InteractionNote(originalNote.getDate(), text));
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(), original.getFinancialNeed().orElse(null),
                original.getPriority().orElse(null), original.getAreaOfInterest().orElse(null), notes,
                original.getNextAction().orElse(null),
                original.getPreferredContactChannel().orElse(null), original.getPreferredContactTiming().orElse(null));
        model.setPerson(original, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, noteIndex.getOneBased(), original.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteEditCommand command
                && contactIndex.equals(command.contactIndex) && noteIndex.equals(command.noteIndex)
                && text.equals(command.text);
    }
}
