package de.fhdo.ui;

import de.fhdo.service.LogManager;
import de.fhdo.util.MenuHelper;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import static de.fhdo.util.MenuHelper.*;

public class LogMenu extends Menu {
    private final Scanner scanner = MenuHelper.getScanner();
    private final LogManager logManager = LogManager.getInstance();

    @Override
    public void show() {
        while (true) {
            System.out.print("""
                    === Log Management ===
                    1. Search Logs by Name
                    2. Search Logs by Date
                    3. Archive Old Logs
                    4. Delete Old Logs
                    0. Return to Main Menu
                    
                    Please select an option (0-4): 
                    """);

            int choice = getValidChoice(0, 4);
            if (choice == 0) {
                break;
            }

            switch (choice) {
                case 1 -> searchLogsByName();
                case 2 -> searchLogsByDate();
                case 3 -> archiveLogs();
                case 4 -> deleteLog();
            }

            waitForEnter();
        }
    }

    private void searchLogsByName() {
        System.out.println("Enter device name: ");
        String name = scanner.nextLine().trim();

        List<Path> logFiles = logManager.searchLogs(name);
        openLog(logFiles);
    }

    private void searchLogsByDate() {
        System.out.println("Enter search date (YYYYMMDD): ");

        try {
            String dateStr = LocalDate.parse(scanner.nextLine().trim(), logManager.DATE_FORMAT).format(logManager.DATE_FORMAT);
            List<Path> logFiles = logManager.searchLogs(dateStr);

            openLog(logFiles);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYYMMDD");
        }
    }

    private void openLog(List<Path> logFiles) {
        if (logFiles.isEmpty()) {
            System.out.println("No log files found");
            return;
        }

        System.out.println("Found the following log files:");
        for (int i = 0; i < logFiles.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, logFiles.get(i));
        }

        System.out.println("\nPlease select the log file number to view (1-" + logFiles.size() + "): ");
        int choice = getValidChoice(1, logFiles.size());

        List<String> logContent = logManager.readLogFile(logFiles.get(choice - 1));
        System.out.println("Log content:");
        logContent.forEach(System.out::println);
    }

    private void archiveLogs() {
        System.out.println("Archive logs before date (YYYYMMDD): ");

        try {
            LocalDate date = LocalDate.parse(scanner.nextLine().trim(), logManager.DATE_FORMAT);

            logManager.archiveLogs(date);
            System.out.println("Logs archived successfully!");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYYMMDD");
        }
    }

    private void deleteLog() {
        System.out.print("\nDelete logs before date (YYYYMMDD): ");

        try {
            LocalDate date = LocalDate.parse(scanner.nextLine().trim(), logManager.DATE_FORMAT);
            logManager.deleteLogs(date);
            System.out.println("Logs deleted successfully!");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYYMMDD");
        } catch (RuntimeException e) {
            System.out.println("Error deleting logs: " + e.getMessage());
        }
    }
}
