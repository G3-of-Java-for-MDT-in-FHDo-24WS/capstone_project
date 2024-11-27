package de.fhdo.ui;

import de.fhdo.model.Battery;
import de.fhdo.model.Device;
import de.fhdo.service.DeviceManager;
import de.fhdo.service.EnergyManager;

import java.util.List;

import static de.fhdo.util.MenuHelper.*;

public class BatteryMenu extends Menu {
    private final EnergyManager energyManager = EnergyManager.getInstance();
    private final DeviceManager deviceManager = DeviceManager.getInstance();

    @Override
    public void show() {
        while (true) {
            System.out.print("""
                    === Battery Management ===
                    1. List All Batteries
                    2. Start Battery Charging
                    3. Stop Battery Charging
                    4. Power Device from Battery
                    5. Stop Battery Power Supply
                    0. Return to Main Menu
                    
                    Please select an option (0-5): 
                    """);

            int choice = getValidChoice(0, 5);
            if (choice == 0) {
                break;
            }

            switch (choice) {
                case 1 -> listBatteries(energyManager.getAllBatteries());
                case 2 -> startCharging();
                case 3 -> stopCharging();
                case 4 -> powerDeviceFromBattery();
                case 5 -> stopBatterySupply();
            }

            waitForEnter();
        }
    }

    private void listBatteries(List<Battery> batteries) {
        if (batteries.isEmpty()) {
            System.out.println("No batteries found.");
            return;
        }

        for (int i = 0; i < batteries.size(); i++) {
            Battery battery = batteries.get(i);
            System.out.println("Battery #" + (i + 1));
            System.out.print(battery);
        }
    }

    private void startCharging() {
        System.out.println("=== Start Battery Charging ===");
        List<Battery> batteries = energyManager.getBatteriesByState(false);
        listBatteries(batteries);

        if(batteries.isEmpty()) {
            return;
        }

        System.out.println("Please select the battery number to start charging (1-" + batteries.size() + ", 0 to return to the previous menu): ");
        int choice = getValidChoice(0, batteries.size());
        if (choice == 0) return;
        String batteryId = batteries.get(choice - 1).getId();

        energyManager.startCharging(batteryId);
        System.out.println("Battery charging started successfully!");
    }

    private void stopCharging() {
        System.out.println("=== Stop Battery Charging ===");
        List<Battery> batteries = energyManager.getBatteriesByState(true);
        listBatteries(batteries);

        if(batteries.isEmpty()) {
            return;
        }

        System.out.println("Please select the battery number to stop charging (1-" + batteries.size() + ", 0 to return to the previous menu): ");
        int choice = getValidChoice(0, batteries.size());
        if (choice == 0) return;
        String batteryId = batteries.get(choice - 1).getId();

        energyManager.stopCharging(batteryId);
        System.out.println("Battery charging stopped successfully!");
    }

    private void powerDeviceFromBattery() {
        System.out.println("=== Power Device from Battery ===");

        List<Device> devices = deviceManager.getDevicesByState(false);
        System.out.println("Inactive devices:");
        listDevices(devices);

        if(devices.isEmpty()) {
            return;
        }

        System.out.println("Please select the device number to power (1-" + devices.size() + ", 0 to return to the previous menu): ");
        int deviceChoice = getValidChoice(0, devices.size());
        if (deviceChoice == 0) return;
        String deviceId = devices.get(deviceChoice - 1).getId();

        List<Battery> batteries = energyManager.getAllBatteries();
        System.out.println("Available batteries:");
        listBatteries(batteries);

        if(batteries.isEmpty()) {
            return;
        }

        System.out.println("Please select the battery number to supply (1-" + batteries.size() + ", 0 to return to the previous menu): ");
        int batteryChoice = getValidChoice(0, batteries.size());
        if (batteryChoice == 0) return;
        String batteryId = batteries.get(batteryChoice - 1).getId();

        energyManager.startPower(deviceId, batteryId);
        System.out.println("Device successfully powered from battery!");
    }

    private void stopBatterySupply() {
        System.out.println("=== Stop Battery Power Supply ===");

        System.out.println("Devices powered by batteries:");
        List<Device> devices = deviceManager.getDevicesByState(true);
        listDevices(devices);

        if(devices.isEmpty()) {
            return;
        }

        System.out.println("Please select the device number to stop supply (1-" + devices.size() + ", 0 to return to the previous menu): ");
        int choice = getValidChoice(0, devices.size());
        if (choice == 0) return;
        String deviceId = devices.get(choice - 1).getId();
        String batteryId = energyManager.getAllBatteries().get(0).getId();

        energyManager.stopPowerDevice(deviceId, batteryId);
        System.out.println("Battery power supply stopped successfully!");
    }
}
