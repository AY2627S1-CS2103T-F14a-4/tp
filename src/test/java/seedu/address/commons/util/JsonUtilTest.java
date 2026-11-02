package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.storage.JsonSerializableClientBook;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path testFolder;

    /** Ensures malformed JSON contents are omitted from default logs. */
    @Test
    public void readJsonFile_malformedJson_defaultLogsDoNotContainFileContents() throws IOException {
        Path file = testFolder.resolve("malformed.json");
        Files.writeString(file, "{\"persons\":[{\"name\": Alice Tan}]}");
        List<String> messages = new ArrayList<>();
        Logger logger = LogsCenter.getLogger(JsonUtil.class).getParent();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (Level.INFO.equals(record.getLevel()) || Level.WARNING.equals(record.getLevel())) {
                    messages.add(record.getMessage());
                }
            }

            @Override
            public void flush() {
                // No buffering.
            }

            @Override
            public void close() {
                // No resources to close.
            }
        };
        logger.addHandler(handler);
        try {
            assertThrows(DataLoadingException.class,
                    () -> JsonUtil.readJsonFile(file, JsonSerializableClientBook.class));
            assertTrue(messages.stream().anyMatch(message -> message.startsWith("Error reading from jsonFile file "
                    + file + ": JsonParseException at line 1, column ")));
            assertTrue(messages.stream().noneMatch(message -> message.contains("Alice")));
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
