package de.fhdo.gui;

import de.fhdo.service.LogManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Vector;

public class LogPanel extends JPanel {
    private final LogManager logManager;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton resetButton;
    private JButton searchByNameButton;
    private JButton searchByDateButton;
    private JButton archiveButton;
    private JButton deleteButton;
    private JTextArea logContentArea;
    private Timer updateTimer;

    public LogPanel(LogManager logManager) {
        this.logManager = logManager;
        setLayout(new BorderLayout());
        initializeComponents();
        layoutComponents();
        loadAllLogs();
        setupUpdateTimer();
    }

    private void initializeComponents() {
        // Initialize table
        String[] columnNames = {"Log File", "Category", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logTable = new JTable(tableModel);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logTable.getSelectionModel().addListSelectionListener(e -> showSelectedLogContent());


        // Initialize search components
        searchField = new JTextField(20);
        resetButton = new JButton("Reset");
        searchByNameButton = new JButton("By Name");
        searchByDateButton = new JButton("By Date");
        
        // Initialize action buttons
        archiveButton = new JButton("Archive Old Logs");
        deleteButton = new JButton("Delete Old Logs");

        // Initialize log content area
        logContentArea = new JTextArea();
        logContentArea.setEditable(false);
        logContentArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Add button listeners
        resetButton.addActionListener(e -> resetSearch());
        searchByNameButton.addActionListener(e -> searchLogsByName());
        searchByDateButton.addActionListener(e -> searchLogsByDate());
        archiveButton.addActionListener(e -> archiveOldLogs());
        deleteButton.addActionListener(e -> deleteOldLogs());
    }

    private void layoutComponents() {
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(resetButton);
        searchPanel.add(searchByNameButton);
        searchPanel.add(searchByDateButton);

        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(archiveButton);
        actionPanel.add(deleteButton);

        // Top panel combining search and action panels
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Split pane for table and log content
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(logTable),
                new JScrollPane(logContentArea));
        splitPane.setResizeWeight(0.5);

        // Add components to main panel
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void searchLogsByName() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a search term",
                    "Search Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Path> logFiles = logManager.searchLogs(searchTerm);
        updateLogTable(logFiles);
    }

    private void loadAllLogs() {
        List<Path> allLogs = logManager.getAllLogFiles();
        updateLogTable(allLogs);
    }

    private void resetSearch() {
        searchField.setText("");
        loadAllLogs();
    }

    private void searchLogsByDate() {
        String dateStr = searchField.getText().trim();
        try {
            LocalDate date = LocalDate.parse(dateStr, logManager.DATE_FORMAT);
            List<Path> logFiles = logManager.searchLogs(date.format(logManager.DATE_FORMAT));
            updateLogTable(logFiles);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYYMMDD",
                    "Date Format Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void archiveOldLogs() {
        String dateStr = JOptionPane.showInputDialog(this,
                "Enter date (YYYYMMDD) to archive logs before:",
                "Archive Logs",
                JOptionPane.QUESTION_MESSAGE);

        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateStr.trim(), logManager.DATE_FORMAT);
                logManager.archiveLogs(date);
                JOptionPane.showMessageDialog(this,
                        "Logs archived successfully",
                        "Archive Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshLogTable();
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid date format. Please use YYYYMMDD",
                        "Date Format Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteOldLogs() {
        String dateStr = JOptionPane.showInputDialog(this,
                "Enter date (YYYYMMDD) to delete logs before:",
                "Delete Logs",
                JOptionPane.WARNING_MESSAGE);

        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateStr.trim(), logManager.DATE_FORMAT);
                
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete all logs before " + date + "?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    logManager.deleteLogs(date);
                    JOptionPane.showMessageDialog(this,
                            "Logs deleted successfully",
                            "Deletion Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshLogTable();
                }
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid date format. Please use YYYYMMDD",
                        "Date Format Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSelectedLogContent() {
        int selectedRow = logTable.getSelectedRow();
        if (selectedRow >= 0) {
            String pathStr = (String) ("logs" + "/" + tableModel.getValueAt(selectedRow, 1) + "/" + tableModel.getValueAt(selectedRow, 0));
            Path logPath = Paths.get(pathStr);
            List<String> content = logManager.readLogFile(logPath);
            logContentArea.setText(String.join("\n", content));
            logContentArea.setCaretPosition(0);
        }
    }

    private void updateLogTable(List<Path> logFiles) {
        tableModel.setRowCount(0);
        for (Path logFile : logFiles) {
            Vector<Object> row = new Vector<>();
            row.add(logFile.getFileName());
            row.add(logFile.getParent().getFileName());
            
            // Extract date from filename
            String fileName = logFile.getFileName().toString();
            String dateStr = fileName.substring(fileName.length() - 12, fileName.length() - 4);

            row.add(dateStr);
            tableModel.addRow(row);
        }
    }

    private void refreshLogTable() {
        // Refresh with current search term
        if (!searchField.getText().trim().isEmpty()) {
            searchLogsByName();
        }
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(1000, e -> loadAllLogs());
        updateTimer.start();
    }
}