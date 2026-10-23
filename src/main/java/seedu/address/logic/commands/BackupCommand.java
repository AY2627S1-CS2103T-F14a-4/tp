package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.storage.JsonClientBookStorage;

/** Saves a copy of all current records to a JSON backup file. */
public class BackupCommand extends Command {
    public static final String COMMAND_WORD = "backup";
    public static final String MESSAGE_USAGE = "backup";
    public static final String MESSAGE_SUCCESS = "Backup saved to %s.";
    public static final String MESSAGE_FAILURE = "Could not create backup: %s";
    public static final Path BACKUP_FILE_PATH = Path.of("data", "clientbook-backup.json");

    private final Path backupFilePath;

    /** Creates a command that saves records to the default backup file. */
    public BackupCommand() {
        this(BACKUP_FILE_PATH);
    }

    BackupCommand(Path backupFilePath) {
        this.backupFilePath = requireNonNull(backupFilePath);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            new JsonClientBookStorage(backupFilePath).saveClientBook(model.getClientBook());
            return new CommandResult(String.format(MESSAGE_SUCCESS, backupFilePath));
        } catch (IOException exception) {
            throw new CommandException(String.format(MESSAGE_FAILURE, exception.getMessage()), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BackupCommand command
                && backupFilePath.equals(command.backupFilePath);
    }
}
