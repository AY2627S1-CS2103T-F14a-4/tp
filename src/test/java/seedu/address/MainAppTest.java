package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ClientBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyClientBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonClientBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

/** Tests startup recovery using real temporary data files. */
public class MainAppTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void initModelManager_corruptedJson_preservesBytesAndWarns() throws Exception {
        Path dataPath = temporaryFolder.resolve("clientbook.json");
        byte[] originalBytes = "{ invalid JSON\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(dataPath, originalBytes);
        MainApp app = new MainApp();
        Storage storage = createStorage(dataPath);

        Model model = app.initModelManager(storage, new UserPrefs());

        assertEquals(new ClientBook(), model.getClientBook());
        List<Path> backups = getBackups();
        assertEquals(1, backups.size());
        Path backupPath = backups.get(0);
        assertTrue(backupPath.getFileName().toString().matches("clientbook\\.json\\.corrupted-\\d{8}-\\d{6}"));
        assertArrayEquals(originalBytes, Files.readAllBytes(backupPath));
        assertArrayEquals(originalBytes, Files.readAllBytes(dataPath));
        assertTrue(app.getStartupWarning().contains("data file " + dataPath + " could not be read."));
        assertTrue(app.getStartupWarning().contains("A backup was saved to " + backupPath));
        assertTrue(app.getStartupWarning().contains("your next change will overwrite the original file."));

        storage.saveClientBook(model.getClientBook());
        assertArrayEquals(originalBytes, Files.readAllBytes(backupPath));
    }

    @Test
    public void initModelManager_missingFile_startsEmptyWithoutBackupOrWarning() throws Exception {
        Path dataPath = temporaryFolder.resolve("clientbook.json");
        MainApp app = new MainApp();

        Model model = app.initModelManager(createStorage(dataPath), new UserPrefs());

        assertEquals(new ClientBook(), model.getClientBook());
        assertTrue(getBackups().isEmpty());
        assertFalse(Files.exists(dataPath));
        assertEquals("", app.getStartupWarning());
    }

    @Test
    public void initModelManager_validFile_loadsWithoutBackupOrWarning() throws Exception {
        Path dataPath = temporaryFolder.resolve("clientbook.json");
        Storage storage = createStorage(dataPath);
        storage.saveClientBook(new ClientBook());
        MainApp app = new MainApp();

        assertEquals(new ClientBook(), app.initModelManager(storage, new UserPrefs()).getClientBook());
        assertTrue(getBackups().isEmpty());
        assertEquals("", app.getStartupWarning());
    }

    @Test
    public void initModelManager_backupFails_startsEmptyAndReportsFailure() throws Exception {
        Path dataPath = temporaryFolder.resolve("clientbook.json");
        Storage storage = new StorageManager(new JsonClientBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
            @Override
            public Optional<ReadOnlyClientBook> readClientBook() throws DataLoadingException {
                throw new DataLoadingException(new IOException("Simulated read failure"));
            }
        };
        MainApp app = new MainApp();

        assertEquals(new ClientBook(), app.initModelManager(storage, new UserPrefs()).getClientBook());
        assertTrue(getBackups().isEmpty());
        assertTrue(app.getStartupWarning().contains("A backup could not be saved to "));
        assertFalse(app.getStartupWarning().contains("A backup was saved"));
        assertTrue(app.getStartupWarning().contains("your next change will overwrite the original file."));
    }

    private Storage createStorage(Path dataPath) {
        return new StorageManager(new JsonClientBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    private List<Path> getBackups() throws IOException {
        try (Stream<Path> files = Files.list(temporaryFolder)) {
            return files.filter(path -> path.getFileName().toString().startsWith("clientbook.json.corrupted-"))
                    .collect(Collectors.toList());
        }
    }
}
