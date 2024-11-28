package de.fhdo.gui;

import de.fhdo.model.Device;
import de.fhdo.service.DeviceManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import  java.util.List;
import java.awt.*;
import java.util.UUID;
import java.util.Vector;

public class DevicePanel extends JPanel {
    private final DeviceManager deviceManager;
    private JTable deviceTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton removeButton;
    private Timer updateTimer;

    public DevicePanel(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        setLayout(new BorderLayout());
        initializeComponents();
        layoutComponents();
        updateDeviceTable();
        setupUpdateTimer();
    }

    private void initializeComponents() {
        // Initialize table
        String[] columnNames = {"Name", "Type", "Power", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deviceTable = new JTable(tableModel);
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize buttons
        addButton = new JButton("Add Device");
        removeButton = new JButton("Remove Device");

        // Add button listeners
        addButton.addActionListener(e -> showAddDeviceDialog());
        removeButton.addActionListener(e -> removeSelectedDevice());
    }

    private void layoutComponents() {
        // Table panel
        JScrollPane scrollPane = new JScrollPane(deviceTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showAddDeviceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Device", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
        JTextField nameField = new JTextField(20);
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        // Type combo box
        JComboBox<Device.DeviceType> typeCombo = new JComboBox<>(Device.DeviceType.values());
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        dialog.add(typeCombo, gbc);

        // Power field
        JSpinner powerSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1));
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Power:"), gbc);
        gbc.gridx = 1;
        dialog.add(powerSpinner, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a device name", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Device device = Device.builder()
                    .id(UUID.randomUUID().toString())
                    .name(nameField.getText().trim())
                    .type((Device.DeviceType) typeCombo.getSelectedItem())
                    .power((Double) powerSpinner.getValue())
                    .isActive(false)
                    .build();

            deviceManager.addDevice(device);
            updateDeviceTable();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void removeSelectedDevice() {
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a device to remove",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove this device?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Device device = deviceManager.getAllDevices().get(selectedRow);
            deviceManager.removeDevice(device.getId());
            updateDeviceTable();
        }
    }

    public void updateDeviceTable() {
        int selectedRow = deviceTable.getSelectedRow();
        String selectDeviceId = null;
        if(selectedRow >= 0) {
            selectDeviceId = deviceManager.getAllDevices().get(selectedRow).getId();
        }

        tableModel.setRowCount(0);
        List<Device> devices = deviceManager.getAllDevices();
        for (int i = 0; i < devices.size(); i++) {
            Device device = devices.get(i);
            Vector<Object> row = new Vector<>();
            row.add(device.getName());
            row.add(device.getType());
            row.add(device.getPower());
            row.add(device.isActive() ? "Active" : "Inactive");
            tableModel.addRow(row);

            if (selectDeviceId != null && device.getId().equals(selectDeviceId)) {
                deviceTable.setRowSelectionInterval(i, i);
            }
        }
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(1000, e -> updateDeviceTable());
        updateTimer.start();
    }
}