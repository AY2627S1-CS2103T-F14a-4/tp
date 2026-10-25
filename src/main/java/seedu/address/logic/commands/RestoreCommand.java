package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.storage.Storage;

/** Replaces current records with the records held in a JSON backup file. */
public class RestoreCommand extends Command {
    public static final String COMMAND_WORD = "restore";
    public static final String MESSAGE_USAGE = "restore";
    public static final String MESSAGE_SUCCESS = "Records restored from %s.";
    public static final String MESSAGE_FILE_NOT_FOUND = "Backup file does not exist: %s";
    public static final String MESSAGE_FAILURE = "Could not restore backup: %s";
    private final Path backupFilePath;

    /** Creates a command that restores records from the default backup file. */
    public RestoreCommand() {
        backupFilePath = null;
    }

    RestoreCommand(Path backupFilePath) {
        this.backupFilePath = requireNonNull(backupFilePath);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireNonNull(model);
        requireNonNull(storage);
        Path resolvedBackupFilePath = backupFilePath == null
                ? storage.getClientBookFilePath().resolveSibling(BackupCommand.BACKUP_FILE_NAME) : backupFilePath;
        try {
            ReadOnlyClientBook restoredClientBook = storage.readClientBook(resolvedBackupFilePath)
                    .orElseThrow(() -> new CommandException(
                            String.format(MESSAGE_FILE_NOT_FOUND, resolvedBackupFilePath)));
            storage.saveClientBook(restoredClientBook);
            model.setClientBook(restoredClientBook);
            model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
            return new CommandResult(String.format(MESSAGE_SUCCESS, resolvedBackupFilePath), false, false, true);
        } catch (DataLoadingException exception) {
            throw new CommandException(String.format(MESSAGE_FAILURE, exception.getCause().getMessage()), exception);
        } catch (IOException exception) {
            throw new CommandException(String.format(MESSAGE_FAILURE, exception.getMessage()), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RestoreCommand command
                && java.util.Objects.equals(backupFilePath, command.backupFilePath);
    }
}
