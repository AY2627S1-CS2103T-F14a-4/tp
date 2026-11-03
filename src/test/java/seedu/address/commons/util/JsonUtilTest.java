package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
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
            assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, Object.class));
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

    /** Checks the class-only diagnostic when reading fails before JSON parsing. */
    @Test
    public void readJsonFile_directory_logsOnlyExceptionClass() throws IOException {
        Path directory = Files.createDirectory(testFolder.resolve("directory"));
        List<String> messages = new ArrayList<>();
        Logger logger = LogsCenter.getLogger(JsonUtil.class).getParent();
        Handler handler = captureDefaultLogs(messages);
        logger.addHandler(handler);
        try {
            DataLoadingException exception = assertThrows(DataLoadingException.class, () ->
                    JsonUtil.readJsonFile(directory, Object.class));
            IOException cause = assertInstanceOf(IOException.class, exception.getCause());
            assertFalse(cause instanceof JsonProcessingException);
            assertTrue(messages.contains("Error reading from jsonFile file " + directory + ": "
                    + cause.getClass().getSimpleName()));
            assertTrue(messages.stream().noneMatch(message -> message.contains(" at line ")));
        } finally {
            logger.removeHandler(handler);
        }
    }

    /** Checks that JSON exceptions with no location retain their cause without logging raw tokens. */
    @Test
    public void readJsonFile_jsonExceptionWithoutLocation_logsOnlyExceptionClass() throws IOException {
        Path file = testFolder.resolve("locationless.json");
        Files.writeString(file, "{}");
        List<String> messages = new ArrayList<>();
        Logger logger = LogsCenter.getLogger(JsonUtil.class).getParent();
        Handler handler = captureDefaultLogs(messages);
        logger.addHandler(handler);
        try {
            DataLoadingException exception = assertThrows(DataLoadingException.class, () ->
                    JsonUtil.readJsonFile(file, LocationlessJsonValue.class));
            JsonParseException cause = assertInstanceOf(JsonParseException.class, exception.getCause());
            assertNull(cause.getLocation());
            assertTrue(cause.getMessage().contains(LocationlessJsonDeserializer.PRIVATE_TOKEN));
            assertTrue(messages.contains("Error reading from jsonFile file " + file + ": JsonParseException"));
            assertTrue(messages.stream().noneMatch(message ->
                    message.contains(LocationlessJsonDeserializer.PRIVATE_TOKEN) || message.contains(" at line ")));
        } finally {
            logger.removeHandler(handler);
        }
    }

    /** Creates a handler for default-level diagnostics without changing logger configuration. */
    private static Handler captureDefaultLogs(List<String> messages) {
        return new Handler() {
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
    }

    /** Test value whose deserializer reports a JSON error without a source location. */
    @JsonDeserialize(using = LocationlessJsonDeserializer.class)
    public static class LocationlessJsonValue {
    }

    /** Produces a locationless parsing error through the normal file-reading entry point. */
    public static class LocationlessJsonDeserializer extends JsonDeserializer<LocationlessJsonValue> {
        private static final String PRIVATE_TOKEN = "PRIVATE_CLIENT_TOKEN_7391";

        @Override
        public LocationlessJsonValue deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            throw new JsonParseException(PRIVATE_TOKEN, (JsonLocation) null);
        }
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
