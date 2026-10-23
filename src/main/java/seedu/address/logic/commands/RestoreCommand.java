package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;

/** Replaces current records with the records held in a JSON backup file. */
public class RestoreCommand extends Command {
    public static final String COMMAND_WORD = "restore";
    public static final String MESSAGE_USAGE = "restore";
    public static final String MESSAGE_SUCCESS = "Records restored from %s.";
    public static final String MESSAGE_FILE_NOT_FOUND = "Backup file does not exist: %s";
    public static final String MESSAGE_FAILURE = "Could not restore backup: %s";
    public static final Path BACKUP_FILE_PATH = BackupCommand.BACKUP_FILE_PATH;

    private final Path backupFilePath;

    /** Creates a command that restores records from the default backup file. */
    public RestoreCommand() {
        this(BACKUP_FILE_PATH);
    }

    RestoreCommand(Path backupFilePath) {
        this.backupFilePath = requireNonNull(backupFilePath);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!Files.isRegularFile(backupFilePath)) {
            throw new CommandException(String.format(MESSAGE_FILE_NOT_FOUND, backupFilePath));
        }
        try {
            ReadOnlyAddressBook restoredAddressBook = new JsonAddressBookStorage(backupFilePath)
                    .readAddressBook().orElseThrow(() -> new CommandException(
                            String.format(MESSAGE_FAILURE, "The backup file is empty.")));
            model.setAddressBook(restoredAddressBook);
            model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
            return new CommandResult(String.format(MESSAGE_SUCCESS, backupFilePath));
        } catch (DataLoadingException exception) {
            throw new CommandException(String.format(MESSAGE_FAILURE, exception.getCause().getMessage()), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RestoreCommand command
                && backupFilePath.equals(command.backupFilePath);
    }
}
