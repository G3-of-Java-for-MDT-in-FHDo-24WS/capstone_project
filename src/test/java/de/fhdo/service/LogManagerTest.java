package de.fhdo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.ZipFile;
import static org.junit.jupiter.api.Assertions.*;

public class LogManagerTest {
    private LogManager logManager;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        logManager = LogManager.getInstance();
        clearTestLogs();
    }

    private void clearTestLogs() {
        try {
            Files.createDirectories(Path.of("logs/device"));
            Files.createDirectories(Path.of("logs/energy"));
            Files.createDirectories(Path.of("logs/battery"));
            Files.createDirectories(Path.of("logs/system"));
            Files.createDirectories(Path.of("logs/archive"));
        } catch (Exception e) {
            fail("Failed to setup test directories");
        }
    }

    @Test
    void testGetInstance() {
        LogManager instance1 = LogManager.getInstance();
        LogManager instance2 = LogManager.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testLogEvent() {
        String testMessage = "Test log message";
        logManager.logEvent(LogManager.Category.SYSTEM, "test", testMessage);
        
        String date = LocalDate.now().format(logManager.DATE_FORMAT);
        Path logFile = Path.of("logs/system/test_" + date + ".log");
        
        assertTrue(Files.exists(logFile));
        List<String> logs = logManager.readLogFile(logFile);
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).contains(testMessage));
    }

    @Test
    void testArchiveLogs() {
        LocalDate testDate = LocalDate.now().minusDays(2);
        String oldDate = testDate.format(logManager.DATE_FORMAT);
        Path testLogFile = Path.of("logs/system/test_" + oldDate + ".log");
        
        try {
            Files.writeString(testLogFile, "Test log content");
            logManager.archiveLogs(LocalDate.now());
            
            Path archiveFile = Path.of("logs/archive/logs_before_" + 
                LocalDate.now().format(logManager.DATE_FORMAT) + ".zip");
            assertTrue(Files.exists(archiveFile));
            
            try (ZipFile zip = new ZipFile(archiveFile.toFile())) {
                assertNotNull(zip.getEntry("system/test_" + oldDate + ".log"));
            }
        } catch (Exception e) {
            fail("Archive test failed: " + e.getMessage());
        }
    }

    @Test
    void testDeleteLogs() {
        LocalDate testDate = LocalDate.now().minusDays(2);
        String oldDate = testDate.format(logManager.DATE_FORMAT);
        Path testLogFile = Path.of("logs/system/test_" + oldDate + ".log");
        
        try {
            Files.writeString(testLogFile, "Test log content");
            assertTrue(Files.exists(testLogFile));
            
            logManager.deleteLogs(LocalDate.now());
            assertFalse(Files.exists(testLogFile));
        } catch (Exception e) {
            fail("Delete test failed: " + e.getMessage());
        }
    }

    @Test
    void testSearchLogs() {
        try {
            String keyword = "searchtest";
            Path testLogFile = Path.of("logs/system/" + keyword + "_" +
                LocalDate.now().format(logManager.DATE_FORMAT)  + ".log");
            Files.writeString(testLogFile, "Test log content");

            List<Path> results = logManager.searchLogs(keyword);
            assertFalse(results.isEmpty());
            assertTrue(results.get(0).toString().contains(keyword));
        } catch (Exception e) {
            fail("Search test failed: " + e.getMessage());
        }
    }

    @Test
    void testReadLogFile() {
        try {
            String testContent = "Test log content\nSecond line";
            Path testLogFile = Path.of("logs/system/test_" + 
                LocalDate.now().format(logManager.DATE_FORMAT) + ".log");
            Files.writeString(testLogFile, testContent);

            List<String> lines = logManager.readLogFile(testLogFile);
            assertEquals(2, lines.size());
            assertEquals("Test log content", lines.get(0));
            assertEquals("Second line", lines.get(1));
        } catch (Exception e) {
            fail("Read test failed: " + e.getMessage());
        }
    }

    @Test
    void testClearAllLogs() {
        try {
            Path testLogFile = Path.of("logs/system/test_" + 
                LocalDate.now().format(logManager.DATE_FORMAT) + ".log");
            Files.writeString(testLogFile, "Test log content");
            assertTrue(Files.exists(testLogFile));

            logManager.clearAllLogs();
            assertFalse(Files.exists(testLogFile));
        } catch (Exception e) {
            fail("Clear all test failed: " + e.getMessage());
        }
    }
} 