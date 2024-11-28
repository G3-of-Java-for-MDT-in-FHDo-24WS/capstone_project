package de.fhdo.gui;

import de.fhdo.model.Battery;
import de.fhdo.model.Device;
import de.fhdo.service.DeviceManager;
import de.fhdo.service.EnergyManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class BatteryPanel extends JPanel {
    private final EnergyManager energyManager;
    private final DeviceManager deviceManager;
    private JTable batteryTable;
    private DefaultTableModel tableModel;
    private JButton startChargingButton;
    private JButton stopChargingButton;
    private JButton powerDeviceButton;
    private JButton stopPowerButton;
    private Timer updateTimer;

    public BatteryPanel(EnergyManager energyManager) {
        this.energyManager = energyManager;
        this.deviceManager = DeviceManager.getInstance();
        setLayout(new BorderLayout());
        initializeComponents();
        layoutComponents();
        setupUpdateTimer();
        updateBatteryTable();
    }

    private void initializeComponents() {
        String[] columnNames = {"Name", "Capacity", "Current Charge", "Charging Rate", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        batteryTable = new JTable(tableModel);
        batteryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        startChargingButton = new JButton("Start Charging");
        stopChargingButton = new JButton("Stop Charging");
        powerDeviceButton = new JButton("Power Device");
        stopPowerButton = new JButton("Stop Power Supply");

        startChargingButton.addActionListener(e -> startCharging());
        stopChargingButton.addActionListener(e -> stopCharging());
        powerDeviceButton.addActionListener(e -> powerDevice());
        stopPowerButton.addActionListener(e -> stopPowerSupply());
    }

    private void layoutComponents() {
        JScrollPane scrollPane = new JScrollPane(batteryTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startChargingButton);
        buttonPanel.add(stopChargingButton);
        buttonPanel.add(powerDeviceButton);
        buttonPanel.add(stopPowerButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(1000, e -> updateBatteryTable());
        updateTimer.start();
    }

    private void startCharging() {
        int selectedRow = batteryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a battery to charge",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Battery battery = energyManager.getAllBatteries().get(selectedRow);
        if (battery.isCharging()) {
            JOptionPane.showMessageDialog(this,
                    "Battery is already charging",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        energyManager.startCharging(battery.getId());
        updateBatteryTable();
    }

    private void stopCharging() {
        int selectedRow = batteryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a battery to stop charging",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Battery battery = energyManager.getAllBatteries().get(selectedRow);
        if (!battery.isCharging()) {
            JOptionPane.showMessageDialog(this,
                    "Battery is not charging",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        energyManager.stopCharging(battery.getId());
        updateBatteryTable();
    }

    private void powerDevice() {
        int selectedRow = batteryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a battery first",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Battery battery = energyManager.getAllBatteries().get(selectedRow);
        List<Device> inactiveDevices = deviceManager.getDevicesByState(false);

        if (inactiveDevices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No inactive devices available",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Device", true);
        dialog.setLayout(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        inactiveDevices.forEach(device -> listModel.addElement(device.getName()));
        JList<String> deviceList = new JList<>(listModel);
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            int deviceIndex = deviceList.getSelectedIndex();
            if (deviceIndex != -1) {
                Device selectedDevice = inactiveDevices.get(deviceIndex);
                energyManager.startPower(selectedDevice.getId(), battery.getId());
                dialog.dispose();
                updateBatteryTable();
            }
        });

        dialog.add(new JScrollPane(deviceList), BorderLayout.CENTER);
        dialog.add(selectButton, BorderLayout.SOUTH);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void stopPowerSupply() {
        List<Device> activeDevices = deviceManager.getDevicesByState(true);
        if (activeDevices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No active devices found",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Device", true);
        dialog.setLayout(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        activeDevices.forEach(device -> listModel.addElement(device.getName()));
        JList<String> deviceList = new JList<>(listModel);
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            int deviceIndex = deviceList.getSelectedIndex();
            if (deviceIndex != -1) {
                Device selectedDevice = activeDevices.get(deviceIndex);
                String batteryId = energyManager.getAllBatteries().get(0).getId();
                energyManager.stopPowerDevice(selectedDevice.getId(), batteryId);
                dialog.dispose();
                updateBatteryTable();
            }
        });

        dialog.add(new JScrollPane(deviceList), BorderLayout.CENTER);
        dialog.add(selectButton, BorderLayout.SOUTH);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void updateBatteryTable() {
        int selectedRow = batteryTable.getSelectedRow();
        String selectedBatteryId = null;
        if (selectedRow >= 0) {
            selectedBatteryId = energyManager.getAllBatteries().get(selectedRow).getId();
        }

        tableModel.setRowCount(0);
        List<Battery> batteries = energyManager.getAllBatteries();
        for (int i = 0; i < batteries.size(); i++) {
            Battery battery = batteries.get(i);
            Vector<Object> row = new Vector<>();
            row.add(battery.getName());
            row.add(battery.getCapacity());
            row.add(String.format("%.2f", battery.getCurrentCharge()));
            row.add(battery.getMaxChargeRate());
            row.add(battery.isCharging() ? "Charging" : "Not Charging");
            tableModel.addRow(row);

            if (selectedBatteryId != null && battery.getId().equals(selectedBatteryId)) {
                batteryTable.setRowSelectionInterval(i, i);
            }
        }
    }
}