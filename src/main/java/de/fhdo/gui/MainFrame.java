package de.fhdo.gui;

import de.fhdo.service.DeviceManager;
import de.fhdo.service.EnergyManager;
import de.fhdo.service.LogManager;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final EnergyManager energyManager = EnergyManager.getInstance();
    private final LogManager logManager = LogManager.getInstance();

    private JTabbedPane tabbedPane;

    private DevicePanel devicePanel;
    private EnergyPanel energyPanel;
    private BatteryPanel batteryPanel;
    private SystemStatusPanel systemStatusPanel;
    private LogPanel logPanel;
    private ConfigPanel configPanel;

    public MainFrame() {
        initializeFrame();
        createComponents();
        addComponents();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeFrame() {
        setTitle("Smart House: Group03");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
    }

    private void createComponents() {
        tabbedPane = new JTabbedPane();

        devicePanel = new DevicePanel(deviceManager);
        energyPanel = new EnergyPanel(energyManager);
        batteryPanel = new BatteryPanel(energyManager);
        systemStatusPanel = new SystemStatusPanel(deviceManager, energyManager);
        logPanel = new LogPanel(logManager);
        configPanel = new ConfigPanel(this);

        tabbedPane.addTab("Devices", devicePanel);
        tabbedPane.addTab("Energy", energyPanel);
        tabbedPane.addTab("Batteries", batteryPanel);
        tabbedPane.addTab("System Status", systemStatusPanel);
        tabbedPane.addTab("Logs", logPanel);
        tabbedPane.addTab("Configuration", configPanel);
    }

    private void addComponents() {
        add(tabbedPane);
    }
    
    public void refreshAllPanels() {
        devicePanel.updateDeviceTable();
        energyPanel.updateEnergyTable();
        batteryPanel.updateBatteryTable();
        systemStatusPanel.updateStatus();
    }
}