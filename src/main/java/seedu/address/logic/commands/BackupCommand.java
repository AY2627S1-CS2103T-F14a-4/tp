package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.storage.Storage;

/** Saves a copy of all current records to a JSON backup file. */
public class BackupCommand extends Command {
    public static final String COMMAND_WORD = "backup";
    public static final String MESSAGE_USAGE = "backup";
    public static final String MESSAGE_SUCCESS = "Backup saved to %s.";
    public static final String MESSAGE_FAILURE = "Could not create backup: %s";
    public static final String BACKUP_FILE_NAME = "clientbook-backup.json";

    private final Path backupFilePath;

    /** Creates a command that saves records to the default backup file. */
    public BackupCommand() {
        backupFilePath = null;
    }

    BackupCommand(Path backupFilePath) {
        this.backupFilePath = requireNonNull(backupFilePath);
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireNonNull(model);
        requireNonNull(storage);
        Path resolvedBackupFilePath = backupFilePath == null
                ? storage.getClientBookFilePath().resolveSibling(BACKUP_FILE_NAME) : backupFilePath;
        Path backupDirectory = resolvedBackupFilePath.toAbsolutePath().getParent();
        Path temporaryFilePath = null;
        try {
            Files.createDirectories(backupDirectory);
            temporaryFilePath = Files.createTempFile(backupDirectory, "backup-", ".tmp");
            storage.saveClientBook(model.getClientBook(), temporaryFilePath);
            replaceBackup(temporaryFilePath, resolvedBackupFilePath);
            return new CommandResult(String.format(MESSAGE_SUCCESS, resolvedBackupFilePath));
        } catch (IOException exception) {
            throw new CommandException(String.format(MESSAGE_FAILURE, exception.getMessage()), exception);
        } finally {
            if (temporaryFilePath != null) {
                try {
                    Files.deleteIfExists(temporaryFilePath);
                } catch (IOException ignored) {
                    // The temporary file is cleaned up on a later backup attempt.
                }
            }
        }
    }

    private void replaceBackup(Path temporaryFilePath, Path resolvedBackupFilePath) throws IOException {
        try {
            Files.move(temporaryFilePath, resolvedBackupFilePath, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFilePath, resolvedBackupFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BackupCommand command
                && java.util.Objects.equals(backupFilePath, command.backupFilePath);
    }
}
