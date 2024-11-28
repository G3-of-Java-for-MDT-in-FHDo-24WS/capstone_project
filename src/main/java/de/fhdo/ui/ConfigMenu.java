package de.fhdo.ui;

import de.fhdo.config.HouseConfig;
import de.fhdo.model.Battery;
import de.fhdo.model.Device;
import de.fhdo.model.Energy;
import de.fhdo.service.DeviceManager;
import de.fhdo.service.EnergyManager;
import de.fhdo.service.LogManager;
import de.fhdo.util.MenuHelper;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

import static de.fhdo.util.MenuHelper.waitForEnter;

public class ConfigMenu extends Menu {
    private final Scanner scanner = MenuHelper.getScanner();
    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final EnergyManager energyManager = EnergyManager.getInstance();
    private final LogManager logManager = LogManager.getInstance();

    private static final String DEFAULT_CONFIG_PATH = "src/main/resources/config/house_config.yml";

    @Override
    public void show() {
        System.out.println("=== Load Configuration File ===");
        System.out.println("Please enter the configuration file path:");
        System.out.println("(Press Enter to use the default path: " + DEFAULT_CONFIG_PATH + ")");
        String path = scanner.nextLine().trim();

        if (path.isEmpty()) {
            path = DEFAULT_CONFIG_PATH;
        }

        File configFile = new File(path);
        if (!configFile.exists()) {
            System.out.println("Error: Configuration file does not exist!");
            System.out.println("File path: " + configFile.getAbsolutePath());
            waitForEnter();
            return;
        }

        System.out.println("Loading configuration from: " + path);
        System.out.println("Continue? (y/n, press Enter to confirm)");
        String answer = scanner.nextLine().trim().toLowerCase();

        if (!answer.isEmpty() && !answer.equals("y")) {
            System.out.println("Operation canceled.");
            return;
        }

        try {
            HouseConfig config = HouseConfig.loadFromFile(path);
            loadConfiguration(config);
        } catch (IOException e) {
            System.out.println("Error occurred while loading configuration file: " + e.getMessage());
            System.out.println("Please ensure the file exists and is accessible.");

        } finally {
            waitForEnter();
        }
    }

    private void loadConfiguration(HouseConfig config) {
        System.out.println("Preparing to clear existing data...");
        System.out.println("Continue? (y/n, press Enter to confirm)");
        String answer = scanner.nextLine().trim().toLowerCase();

        if (!answer.isEmpty() && !answer.equals("y")) {
            System.out.println("Operation canceled.");
            return;
        }

        deviceManager.clearAllDevices();
        energyManager.clearAllEnergies();
        energyManager.clearAllBatteries();
        logManager.clearAllLogs();

        config.getDevices().forEach(dev -> {
            Device device = Device.builder().id(UUID.randomUUID().toString()).name(dev.getName()).type(Device.DeviceType.valueOf(dev.getType())).power(dev.getPower()).isActive(false).build();
            deviceManager.addDevice(device);
        });

        config.getEnergies().forEach(src -> {
            Energy energy = Energy.builder().id(UUID.randomUUID().toString()).name(src.getName()).type(Energy.EnergyType.valueOf(src.getType())).output(src.getOutput()).isActive(false).build();
            energyManager.addEnergy(energy);
        });

        config.getBatteries().forEach(bat -> {
            Battery battery = Battery.builder().id(UUID.randomUUID().toString()).name(bat.getName()).capacity(bat.getCapacity()).currentCharge(0.0).maxChargeRate(bat.getMaxChargeRate()).isCharging(false).build();
            energyManager.addBattery(battery);
        });

        System.out.printf("\nConfiguration loaded! Total: %d devices, %d energy, %d batteries\n", config.getDevices().size(), config.getEnergies().size(), config.getBatteries().size());
    }
}
