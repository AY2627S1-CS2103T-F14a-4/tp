package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.AppUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ClientBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "v1.4";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Path.of("preferences.json");
    private static final Path CLIENT_BOOK_FILE_PATH = Path.of("data", "clientbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing ClientBook ]===========================");
        super.init();

        Path applicationDirectory = AppUtil.getApplicationDirectory();
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(
                applicationDirectory.resolve(USER_PREFS_FILE_PATH));
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonClientBookStorage clientBookStorage = new JsonClientBookStorage(
                applicationDirectory.resolve(CLIENT_BOOK_FILE_PATH));
        storage = new StorageManager(clientBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getClientBookFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s client book and {@code userPrefs}. <br>
     * An empty client book will be used if {@code storage}'s client book is not found or cannot be read.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getClientBookFilePath());

        Optional<ReadOnlyClientBook> clientBookOptional;
        ReadOnlyClientBook initialData;
        try {
            clientBookOptional = storage.readClientBook();
            if (clientBookOptional.isEmpty()) {
                logger.info("No data file found at " + storage.getClientBookFilePath()
                        + ". Starting with an empty ClientBook.");
            }
            initialData = clientBookOptional.orElseGet(ClientBook::new);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getClientBookFilePath() + " could not be loaded."
                    + " Will be starting with an empty ClientBook.");
            initialData = new ClientBook();
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting ClientBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping ClientBook ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
