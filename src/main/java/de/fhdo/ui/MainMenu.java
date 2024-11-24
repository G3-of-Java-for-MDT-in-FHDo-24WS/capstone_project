package de.fhdo.ui;

import de.fhdo.service.EnergyManager;
import lombok.extern.slf4j.Slf4j;

import static de.fhdo.util.MenuHelper.clearScreen;
import static de.fhdo.util.MenuHelper.getValidChoice;

@Slf4j
public class MainMenu extends Menu {

    private Boolean isRunning = true;

    private final EnergyManager energyManager = EnergyManager.getInstance();

    private final DeviceMenu deviceMenu = new DeviceMenu();
    private final EnergyMenu energyMenu = new EnergyMenu();
    private final BatteryMenu batteryMenu = new BatteryMenu();
    private final SystemStatusMenu systemStatusMenu = new SystemStatusMenu();
    private final LogMenu logMenu = new LogMenu();
    private final ConfigMenu configMenu = new ConfigMenu();

    @Override
    public void show() {
        while (isRunning) {
            clearScreen();
            System.out.print("""
                    === Smart House Energy Management System ===
                    1. Manage Devices
                    2. Manage Energies
                    3. Manage Batteries
                    4. View System Status
                    5. Manage Logs
                    6. Load Configuration
                    0. Exit
                    
                    Please select an option (0-6): 
                    """);

            int choice = getValidChoice(0, 6);
            processMainMenuChoice(choice);
        }
    }

    private void processMainMenuChoice(int choice) {
        switch (choice) {
            case 1 -> deviceMenu.show();
            case 2 -> energyMenu.show();
            case 3 -> batteryMenu.show();
            case 4 -> systemStatusMenu.show();
            case 5 -> logMenu.show();
            case 6 -> configMenu.show();
            case 0 -> exit();
        }
    }

    private void exit() {
        System.out.println("Shutting down Smart House Energy Management System...");
        energyManager.shutdown();
        isRunning = false;
    }
}