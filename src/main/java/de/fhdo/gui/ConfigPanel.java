package de.fhdo.gui;

import de.fhdo.config.HouseConfig;
import de.fhdo.model.Battery;
import de.fhdo.model.Device;
import de.fhdo.model.Energy;
import de.fhdo.service.DeviceManager;
import de.fhdo.service.EnergyManager;
import de.fhdo.service.LogManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ConfigPanel extends JPanel {
    private static final String DEFAULT_CONFIG_PATH = "src/main/resources/config/house_config.yml";
    
    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final EnergyManager energyManager = EnergyManager.getInstance();
    private final LogManager logManager = LogManager.getInstance();
    
    private JTextField configPathField;
    private JButton browseButton;
    private JButton loadButton;
    private JLabel statusLabel;

    private final MainFrame mainFrame;

    public ConfigPanel(MainFrame mainFrame) {
        setLayout(new BorderLayout());
        initializeComponents();
        layoutComponents();
        this.mainFrame = mainFrame;
    }

    private void initializeComponents() {
        // Initialize path field
        configPathField = new JTextField(DEFAULT_CONFIG_PATH);
        configPathField.setEditable(false);
        
        // Initialize buttons
        browseButton = new JButton("Browse");
        loadButton = new JButton("Load Configuration");

        
        // Initialize status label
        statusLabel = new JLabel("Ready to load configuration");
        statusLabel.setForeground(Color.GRAY);

        // Add button listeners
        browseButton.addActionListener(e -> browseConfigFile());
        loadButton.addActionListener(e -> loadConfiguration());
    }

    private void layoutComponents() {
        // Path panel
        JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
        pathPanel.setBorder(BorderFactory.createTitledBorder("Configuration File"));
        pathPanel.add(configPathField, BorderLayout.CENTER);
        pathPanel.add(browseButton, BorderLayout.EAST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(loadButton);

        // Top panel combining path and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(pathPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);

        // Add all components to main panel
        add(topPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void browseConfigFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("YAML files (*.yml, *.yaml)", "yml", "yaml"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            configPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void loadConfiguration() {
        File configFile = new File(configPathField.getText());
        if (!configFile.exists()) {
            showError("Configuration file does not exist!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Loading a new configuration will clear all existing data. Continue?",
                "Confirm Configuration Load",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                HouseConfig config = HouseConfig.loadFromFile(configFile.getPath());
                applyConfiguration(config);
                showSuccess("Configuration loaded successfully!");
            } catch (IOException e) {
                showError("Error loading configuration: " + e.getMessage());
            }
        }
    }

    private void applyConfiguration(HouseConfig config) {
        // Clear existing data
        deviceManager.clearAllDevices();
        energyManager.clearAllEnergies();
        energyManager.clearAllBatteries();
        logManager.clearAllLogs();

        // Add devices
        config.getDevices().forEach(dev -> {
            Device device = Device.builder()
                    .id(UUID.randomUUID().toString())
                    .name(dev.getName())
                    .type(Device.DeviceType.valueOf(dev.getType()))
                    .power(dev.getPower())
                    .isActive(false)
                    .build();
            deviceManager.addDevice(device);
        });

        // Add energy sources
        config.getEnergies().forEach(src -> {
            Energy energy = Energy.builder()
                    .id(UUID.randomUUID().toString())
                    .name(src.getName())
                    .type(Energy.EnergyType.valueOf(src.getType()))
                    .output(src.getOutput())
                    .isActive(false)
                    .build();
            energyManager.addEnergy(energy);
        });

        // Add batteries
        config.getBatteries().forEach(bat -> {
            Battery battery = Battery.builder()
                    .id(UUID.randomUUID().toString())
                    .name(bat.getName())
                    .capacity(bat.getCapacity())
                    .currentCharge(0.0)
                    .maxChargeRate(bat.getMaxChargeRate())
                    .isCharging(false)
                    .build();
            energyManager.addBattery(battery);
        });

        mainFrame.refreshAllPanels();
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        statusLabel.setForeground(Color.RED);
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(0, 128, 0));
    }
}