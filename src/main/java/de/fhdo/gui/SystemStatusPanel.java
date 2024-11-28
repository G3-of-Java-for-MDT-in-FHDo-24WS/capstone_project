package de.fhdo.gui;

import de.fhdo.model.Battery;
import de.fhdo.model.Device;
import de.fhdo.service.DeviceManager;
import de.fhdo.service.EnergyManager;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class SystemStatusPanel extends JPanel {
    private final DeviceManager deviceManager;
    private final EnergyManager energyManager;
    private Timer updateTimer;

    // UI Components
    private JProgressBar powerConsumptionBar;
    private JProgressBar batteryChargeBar;
    private JPanel activeDevicesPanel;
    private JPanel activeBatteriesPanel;

    private final DecimalFormat df = new DecimalFormat("#.##");

    public SystemStatusPanel(DeviceManager deviceManager, EnergyManager energyManager) {
        this.deviceManager = deviceManager;
        this.energyManager = energyManager;

        setLayout(new BorderLayout());
        initializeComponents();
        layoutComponents();
        setupUpdateTimer();
    }

    private void initializeComponents() {
        // Initialize progress bars
        powerConsumptionBar = new JProgressBar(0, 100);
        powerConsumptionBar.setStringPainted(true);

        batteryChargeBar = new JProgressBar(0, 100);
        batteryChargeBar.setStringPainted(true);

        // Initialize list panels
        activeDevicesPanel = new JPanel();
        activeDevicesPanel.setLayout(new BoxLayout(activeDevicesPanel, BoxLayout.Y_AXIS));
        activeDevicesPanel.setBorder(BorderFactory.createTitledBorder("Active Devices"));

        activeBatteriesPanel = new JPanel();
        activeBatteriesPanel.setLayout(new BoxLayout(activeBatteriesPanel, BoxLayout.Y_AXIS));
        activeBatteriesPanel.setBorder(BorderFactory.createTitledBorder("Charging Batteries"));
    }

    private void layoutComponents() {
        // Top panel for status bars
        JPanel statusPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        statusPanel.add(new JLabel("Current Power Consumption:"), gbc);
        gbc.gridx = 1;
        statusPanel.add(powerConsumptionBar, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        statusPanel.add(new JLabel("Current Battery Charge:"), gbc);
        gbc.gridx = 1;
        statusPanel.add(batteryChargeBar, gbc);

        // panel for active devices and batteries
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomPanel.add(activeDevicesPanel);
        bottomPanel.add(activeBatteriesPanel);

        // Add all panels to main panel
        add(statusPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(1000, e -> updateStatus());
        updateTimer.start();
    }

    public void updateStatus() {
        // Update consumption and charge bars
        double totalCapacity = energyManager.getCurrentTotalBatteryCapacity();
        double currentConsumption = deviceManager.getCurrentTotalConsumption();
        double currentCharge = energyManager.getCurrentTotalBatteryCharge();

        if (totalCapacity > 0) {
            int consumptionPercentage = (int) ((currentConsumption / totalCapacity) * 100);
            int chargePercentage = (int) ((currentCharge / totalCapacity) * 100);

            powerConsumptionBar.setValue(consumptionPercentage);
            powerConsumptionBar.setString(df.format(consumptionPercentage) + " / " + "100 %");

            batteryChargeBar.setValue(chargePercentage);
            batteryChargeBar.setString(df.format(chargePercentage) + " / " + "100 %");
        }

        // Update active devices list
        activeDevicesPanel.removeAll();
        for (Device device : deviceManager.getDevicesByState(true)) {
            JLabel deviceLabel = new JLabel(device.getName() + " (" + device.getPower() + " units)");
            activeDevicesPanel.add(deviceLabel);
        }

        // Update active batteries list
        activeBatteriesPanel.removeAll();
        for (Battery battery : energyManager.getBatteriesByState(true)) {
            JLabel batteryLabel = new JLabel(String.format("%s (%.2f/%.2f units)",
                    battery.getName(),
                    battery.getCurrentCharge(),
                    battery.getCapacity()));
            activeBatteriesPanel.add(batteryLabel);
        }

        // Refresh the panel
        revalidate();
        repaint();
    }
}