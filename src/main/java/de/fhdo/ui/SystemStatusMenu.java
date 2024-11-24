package de.fhdo.ui;

import de.fhdo.model.Device;
import de.fhdo.service.DeviceManager;
import de.fhdo.service.EnergyManager;

import java.util.List;

import static de.fhdo.util.MenuHelper.*;

public class SystemStatusMenu extends Menu {

    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final EnergyManager energyManager = EnergyManager.getInstance();

    @Override
    public void show() {
        clearScreen();
        System.out.println("=== System Status ===");

        double totalConsumption = deviceManager.getTotalConsumption();
        double totalBatteryCharge = getTotalBatteryCharge();

        System.out.printf("Total Power Consumption: %.2f\n", totalConsumption);
        System.out.printf("Total Battery Charge: %.2f\n", totalBatteryCharge);

        List<Device> activeDevices = deviceManager.getDevicesByState(true);
        System.out.println("\nActive Devices:");
        listDevices(activeDevices);

        waitForEnter();
    }

    private double getTotalBatteryCharge() {
        return energyManager.getAllBatteries().stream()
                .mapToDouble(battery -> battery.getCurrentCharge())
                .sum();
    }
}
