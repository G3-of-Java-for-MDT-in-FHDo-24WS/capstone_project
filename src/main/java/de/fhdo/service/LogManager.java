package de.fhdo.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class LogManager {
    private static volatile LogManager instance;

    private final Path LOG_DIR = Paths.get("logs");
    private final Path ARCHIVE_DIR = LOG_DIR.resolve("archive");
    public final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Getter
    @AllArgsConstructor
    public enum Category {
        DEVICE("device"), ENERGY("energy"), BATTERY("battery"), SYSTEM("system");

        private final String value;
    }

    private LogManager() {
        initializeDirectories();
    }

    public static LogManager getInstance() {
        if (instance == null) {
            synchronized (LogManager.class) {
                if (instance == null) {
                    instance = new LogManager();
                }
            }
        }
        return instance;
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(LOG_DIR);
            Files.createDirectories(ARCHIVE_DIR);
            for (Category category : Category.values()) {
                Files.createDirectories(LOG_DIR.resolve(category.getValue()));
            }
        } catch (IOException e) {
            log.error("Failed to initialize log directories", e);
        }
    }

    public void logEvent(Category category, String name, String message) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DATE_FORMAT);
        Path logFile = LOG_DIR.resolve(category.getValue()).resolve(String.format("%s_%s.log", name, date));
        Path systemLogFile = LOG_DIR.resolve(Category.SYSTEM.getValue()).resolve("system_" + date + ".log");

        writeToLog(logFile, now, message);
        if (!category.equals(Category.SYSTEM)) {
            writeToLog(systemLogFile, now, String.format("%s: %s", category, message));
        }
    }

    public void archiveLogs(LocalDate beforeDate) {
        Path archivePath = ARCHIVE_DIR.resolve(String.format("logs_before_%s.zip", beforeDate.format(DATE_FORMAT)));
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archivePath.toFile()))) {
            for (Category category : Category.values()) {
                Path categoryDir = LOG_DIR.resolve(category.getValue());
                if (!Files.exists(categoryDir)) continue;

                try (Stream<Path> paths = Files.list(categoryDir)) {
                    paths.filter(path -> isLogFileBeforeDate(path, beforeDate, DATE_FORMAT)).forEach(logFile -> archiveLogFile(logFile, zos));
                }
            }
            log.info("Archived logs before {} to {}", beforeDate, archivePath.getFileName());
        } catch (IOException e) {
            log.error("Failed to archive logs", e);
        }
    }

    public void deleteLogs(LocalDate beforeDate) {
        for (Category category : Category.values()) {
            Path categoryDir = LOG_DIR.resolve(category.getValue());
            if (!Files.exists(categoryDir)) continue;

            try (Stream<Path> paths = Files.list(categoryDir)) {
                paths.filter(path -> isLogFileBeforeDate(path, beforeDate, DATE_FORMAT)).forEach(this::deleteLogFile);
            } catch (IOException e) {
                log.error("Error deleting logs in category: {}", category, e);
            }
        }
    }

    public List<Path> searchLogs(String keyword) {
        List<Path> results = new ArrayList<>();
        for (Category category : Category.values()) {
            Path categoryDir = LOG_DIR.resolve(category.getValue());
            if (!Files.exists(categoryDir)) continue;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(categoryDir, "*" + keyword + "*.log")) {
                stream.forEach(results::add);
            } catch (IOException e) {
                log.error("Error searching logs in category: {}", category, e);
            }
        }
        return results;
    }

    public List<String> readLogFile(Path logFile) {
        try {
            return Files.readAllLines(logFile);
        } catch (IOException e) {
            log.error("Error reading log file: {}", logFile, e);
            return Collections.emptyList();
        }
    }

    public void clearAllLogs() {
        deleteLogs(LocalDate.now().plusDays(1));
    }

    public void writeToLog(Path logFile, LocalDateTime timestamp, String message) {
        try {
            Files.createDirectories(logFile.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(logFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(String.format("[%s] %s%n", timestamp.format(TIME_FORMAT), message));
            }
        } catch (IOException e) {
            log.error("Failed to write to log file: {}", logFile, e);
        }
    }

    public boolean isLogFileBeforeDate(Path logFile, LocalDate beforeDate, DateTimeFormatter dateFormat) {
        try {
            String fileName = logFile.getFileName().toString();
            String dateStr = fileName.substring(fileName.length() - 12, fileName.length() - 4);
            LocalDate logDate = LocalDate.parse(dateStr, dateFormat);
            return logDate.isBefore(beforeDate);
        } catch (Exception e) {
            log.warn("Failed to parse log file date: {}", logFile, e);
            return false;
        }
    }

    public void archiveLogFile(Path logFile, ZipOutputStream zos) {
        try {
            ZipEntry entry = new ZipEntry(logFile.getParent().getFileName() + "/" + logFile.getFileName().toString());
            zos.putNextEntry(entry);
            Files.copy(logFile, zos);
            zos.closeEntry();
            Files.delete(logFile);
        } catch (IOException e) {
            log.error("Failed to archive log file: {}", logFile, e);
        }
    }

    public void deleteLogFile(Path logFile) {
        try {
            Files.delete(logFile);
            log.info("Deleted log file: {}", logFile);
        } catch (IOException e) {
            log.error("Failed to delete log file: {}", logFile, e);
        }
    }

    public List<Path> getAllLogFiles() {
        List<Path> allLogs = new ArrayList<>();
        for (Category category : Category.values()) {
            Path categoryDir = LOG_DIR.resolve(category.getValue());
            try {
                allLogs.addAll(Files.list(categoryDir).collect(Collectors.toList()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return allLogs;
    }
}
