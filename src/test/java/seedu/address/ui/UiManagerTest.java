package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests UI manager construction without starting the JavaFX toolkit. */
public class UiManagerTest {

    private final Path dataFilePath = Path.of("data", "clientbook.json");
    private final Logic logic = new LogicManager(new ModelManager(),
            new StorageManager(new JsonClientBookStorage(dataFilePath),
                    new JsonUserPrefsStorage(Path.of("preferences.json"))));

    @Test
    public void constructor_withoutWarning_storesEmptyWarning() {
        UiManager uiManager = new UiManager(logic, dataFilePath);

        assertNotNull(uiManager);
        assertEquals("", uiManager.getStartupWarning());
    }

    @Test
    public void constructor_withWarning_preservesWarning() {
        String warning = "Warning: data file could not be read.\nA backup was saved.";
        UiManager uiManager = new UiManager(logic, dataFilePath, warning);

        assertNotNull(uiManager);
        assertEquals(warning, uiManager.getStartupWarning());
    }
}
