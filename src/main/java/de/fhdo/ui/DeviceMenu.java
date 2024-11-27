package de.fhdo.ui;

import de.fhdo.model.Device;
import de.fhdo.service.DeviceManager;
import de.fhdo.util.MenuHelper;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static de.fhdo.util.MenuHelper.*;

public class DeviceMenu extends Menu {
    private final Scanner scanner = MenuHelper.getScanner();
    private final DeviceManager deviceManager = DeviceManager.getInstance();

    @Override
    public void show() {
        while (true) {
            System.out.print("""
                    === Device Management ===
                    1. Add New Device
                    2. List All Devices
                    3. Remove Device
                    0. Return to Main Menu
                    
                    Please select an option (0-3): 
                    """);

            int choice = getValidChoice(0, 3);
            if (choice == 0)
                break;

            switch (choice) {
                case 1 -> addDevice();
                case 2 -> listDevices(deviceManager.getAllDevices());
                case 3 -> removeDevice();
//                case 4 -> toggleDevice();
            }

            waitForEnter();
        }
    }


    private void addDevice() {
        System.out.println("=== Add New Device ===");
        System.out.println("Enter device name:");

        String name = scanner.nextLine();

        System.out.println("Available device types:");
        Device.DeviceType[] types = Device.DeviceType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%d. %s\n", i + 1, types[i]);
        }

        System.out.println("Select device type (1-" + types.length + "):");
        int typeChoice = getValidChoice(1, types.length);
        Device.DeviceType type = types[typeChoice - 1];

        System.out.println("Enter Power Consumption: ");
        double consumption = getValidDouble();

        Device device = Device.builder().id(UUID.randomUUID().toString()).name(name).type(type).power(consumption).isActive(false).build();

        deviceManager.addDevice(device);
        System.out.println("\nDevice added successfully: ");
        System.out.print(device);
    }

    private void removeDevice() {
        System.out.println("=== Delete Device ===");

        List<Device> devices = deviceManager.getAllDevices();
        listDevices(devices);

        if (devices.isEmpty()) {
            return;
        }

        System.out.println("Please select the device number to remove (1-" + devices.size() + ", 0 to return to the previous menu): ");
        int choice = getValidChoice(0, devices.size());
        if (choice == 0) return;
        String deviceId = devices.get(choice - 1).getId();

        deviceManager.removeDevice(deviceId);
        System.out.println("Device removed successfully!");
    }

    private void toggleDevice() {
        System.out.println("=== Toggle Device State ===");

        List<Device> devices = deviceManager.getAllDevices();
        listDevices(devices);

        if (devices.isEmpty()) {
            return;
        }

        System.out.println("Please select the device number to toggle (1-" + devices.size() + ", 0 to return to the previous menu): ");
        int choice = getValidChoice(0, devices.size());
        if (choice == 0) return;
        String deviceId = devices.get(choice - 1).getId();

        deviceManager.toggleDevice(deviceId);
        System.out.println("Device state toggled successfully!");
    }
}
