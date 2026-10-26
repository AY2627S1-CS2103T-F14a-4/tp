package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ClientBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT =
            "Changes cannot be saved. Please try again. If the problem persists, ensure that the data folder and "
                    + "clientbook.json file are accessible and writable.";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT = FILE_OPS_ERROR_FORMAT;

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final ClientBookParser clientBookParser;

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

        Command command = clientBookParser.parseCommand(commandText);
        Model temporaryModel = new ModelManager(model.getClientBook(), model.getUserPrefs());
        temporaryModel.updateFilteredPersonList(model.getFilteredPersonPredicate());
        CommandResult commandResult = command.execute(temporaryModel, storage);

        if (!commandResult.isModelSaved()
                && !temporaryModel.getClientBook().equals(model.getClientBook())) {
            try {
                storage.saveClientBook(temporaryModel.getClientBook());
            } catch (AccessDeniedException e) {
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT,
                                                         e.getMessage()), e);
            } catch (IOException ioe) {
                throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
        }

        commitModel(temporaryModel);
        return commandResult;
    }

    /** Replaces the visible model with a command result that has been successfully persisted. */
    private void commitModel(Model temporaryModel) {
        model.setClientBook(temporaryModel.getClientBook());
        model.updateFilteredPersonList(temporaryModel.getFilteredPersonPredicate());
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
