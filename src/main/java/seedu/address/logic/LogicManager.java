package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Locale;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.TagDeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ClientBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final ClientBookParser clientBookParser;
    private TagDeleteCommand pendingTagDeleteCommand;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        clientBookParser = new ClientBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");
        if (pendingTagDeleteCommand != null) {
            return handleTagDeleteConfirmation(commandText);
        }

        Command command = clientBookParser.parseCommand(commandText);
        if (command instanceof TagDeleteCommand tagDeleteCommand) {
            String confirmationMessage = tagDeleteCommand.getConfirmationMessage(model);
            pendingTagDeleteCommand = tagDeleteCommand;
            return new CommandResult(confirmationMessage);
        }
        return executeCommand(command);
    }

    private CommandResult handleTagDeleteConfirmation(String commandText) throws CommandException {
        String response = commandText.strip().toLowerCase(Locale.ROOT);
        if (response.equals("n") || response.equals("no")) {
            pendingTagDeleteCommand = null;
            return new CommandResult(TagDeleteCommand.MESSAGE_CANCELLED);
        }
        if (!response.equals("y") && !response.equals("yes")) {
            throw new CommandException(TagDeleteCommand.MESSAGE_INVALID_CONFIRMATION);
        }

        TagDeleteCommand confirmedCommand = pendingTagDeleteCommand;
        pendingTagDeleteCommand = null;
        return executeCommand(confirmedCommand);
    }

    private CommandResult executeCommand(Command command) throws CommandException {
        CommandResult commandResult = command.execute(model, storage);

        if (!commandResult.isModelSaved()) {
            try {
                storage.saveClientBook(model.getClientBook());
            } catch (AccessDeniedException e) {
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
            } catch (IOException ioe) {
                throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
        }

        return commandResult;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
