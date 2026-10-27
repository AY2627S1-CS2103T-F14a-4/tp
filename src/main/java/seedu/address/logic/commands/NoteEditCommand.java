package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/** Edits the date or text of a note belonging to a displayed person. */
public class NoteEditCommand extends Command {
    public static final String COMMAND_WORD = "noteedit";
    public static final String MESSAGE_USAGE = "noteedit CONTACT_INDEX NOTE_INDEX [d/DATE] [n/NEW_NOTE_CONTENT]";
    public static final String MESSAGE_NO_FIELDS = "At least one of d/ or n/ is required.";
    public static final String MESSAGE_SUCCESS = "Updated interaction note %d for %s.";
    public static final String MESSAGE_INVALID_NOTE_INDEX = "The interaction note index provided is invalid.";
    public static final String MESSAGE_FUTURE_DATE = "Interaction date cannot be in the future.";

    private final Index contactIndex;
    private final Index noteIndex;
    private final LocalDate date;
    private final String text;

    /** Creates a command that preserves the selected note's date. */
    public NoteEditCommand(Index contactIndex, Index noteIndex, String text) {
        this(contactIndex, noteIndex, null, requireNonNull(text));
    }

    /** Creates a command; a null date or text preserves that field of the selected note. */
    public NoteEditCommand(Index contactIndex, Index noteIndex, LocalDate date, String text) {
        requireAllNonNull(contactIndex, noteIndex);
        checkArgument(date != null || text != null, MESSAGE_NO_FIELDS);
        checkArgument(text == null || !text.isBlank(), InteractionNote.MESSAGE_BLANK);
        this.contactIndex = contactIndex;
        this.noteIndex = noteIndex;
        this.date = date;
        this.text = text == null ? null : text.strip();
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
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new CommandException(MESSAGE_FUTURE_DATE);
        }
        InteractionNote originalNote = notes.get(noteIndex.getZeroBased());
        notes.set(noteIndex.getZeroBased(), new InteractionNote(date == null ? originalNote.getDate() : date,
                text == null ? originalNote.getText() : text));
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
                && Objects.equals(date, command.date) && Objects.equals(text, command.text);
    }
}
