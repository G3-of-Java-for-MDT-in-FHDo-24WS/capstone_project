package de.fhdo.ui;

import de.fhdo.model.Battery;
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
        System.out.println("=== System Status ===");

        double currentTotalConsumption = deviceManager.getCurrentTotalConsumption();
        double currentTotalBatteryCharge = energyManager.getCurrentTotalBatteryCharge();
        double totalBatteryCapacity = energyManager.getCurrentTotalBatteryCapacity();

        System.out.printf("Current Power Consumption: %.2f%% (.2f units)\n", currentTotalConsumption / totalBatteryCapacity * 100, currentTotalConsumption);
        System.out.printf("Current Battery Charge: %.2f%% (.2f units)\n", currentTotalBatteryCharge / totalBatteryCapacity * 100, currentTotalBatteryCharge);

        List<Battery> activeBatteries = energyManager.getBatteriesByState(true);
        System.out.println("\nActive Batteries:");
        listBatteries(activeBatteries);

        List<Device> activeDevices = deviceManager.getDevicesByState(true);
        System.out.println("\nActive Devices:");
        listDevices(activeDevices);

        waitForEnter();
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
}
