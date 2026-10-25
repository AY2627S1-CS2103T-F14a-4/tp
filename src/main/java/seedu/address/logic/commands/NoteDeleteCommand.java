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
import seedu.address.storage.Storage;

/** Deletes a dated interaction note belonging to a displayed contact. */
public class NoteDeleteCommand extends Command {
    public static final String COMMAND_WORD = "notedelete";
    public static final String MESSAGE_USAGE = "notedelete CONTACT_INDEX NOTE_INDEX";
    public static final String MESSAGE_SUCCESS = "Deleted interaction note %d for %s (%s): %s";
    public static final String MESSAGE_INVALID_NOTE_INDEX = "The interaction note index provided is invalid.";

    private static final int EXCERPT_LENGTH = 80;

    private final Index contactIndex;
    private final Index noteIndex;

    /** Creates a command targeting a note's position in a displayed contact's history. */
    public NoteDeleteCommand(Index contactIndex, Index noteIndex) {
        requireAllNonNull(contactIndex, noteIndex);
        this.contactIndex = contactIndex;
        this.noteIndex = noteIndex;
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireAllNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (contactIndex.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = displayedPersons.get(contactIndex.getZeroBased());
        List<InteractionNote> notes = new ArrayList<>(original.getInteractionNotes());
        if (noteIndex.getZeroBased() >= notes.size()) {
            throw new CommandException(MESSAGE_INVALID_NOTE_INDEX);
        }
        InteractionNote deletedNote = notes.remove(noteIndex.getZeroBased());
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getTags(),
                original.getRelationship(), original.getFinancialNeed().orElse(null),
                original.getPriority().orElse(null), original.getAreaOfInterest().orElse(null), notes,
                original.getNextAction().orElse(null),
                original.getPreferredContactChannel().orElse(null), original.getPreferredContactTiming().orElse(null));
        model.setPerson(original, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, noteIndex.getOneBased(), original.getName().fullName,
                deletedNote.getDate(), excerpt(deletedNote.getText())));
    }

    /** Formats feedback without changing stored text or splitting supplementary Unicode characters. */
    private static String excerpt(String text) {
        String singleLine = text.replaceAll("(?U)\\s+", " ").strip();
        if (singleLine.codePointCount(0, singleLine.length()) <= EXCERPT_LENGTH) {
            return singleLine;
        }
        return singleLine.substring(0, singleLine.offsetByCodePoints(0, EXCERPT_LENGTH)) + "…";
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteDeleteCommand command
                && contactIndex.equals(command.contactIndex) && noteIndex.equals(command.noteIndex);
    }
}
