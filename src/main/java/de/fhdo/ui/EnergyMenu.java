package de.fhdo.ui;

import de.fhdo.model.Energy;
import de.fhdo.service.EnergyManager;
import de.fhdo.util.MenuHelper;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static de.fhdo.util.MenuHelper.*;

public class EnergyMenu extends Menu {
    private final Scanner scanner = MenuHelper.getScanner();
    private final EnergyManager energyManager = EnergyManager.getInstance();

    @Override
    public void show() {
        while (true) {
            System.out.print("""
                    === Energy Management ===
                    1. Add New Energy
                    2. List All Energies
                    3. Remove Energy
                    4. Toggle Energy State
                    0. Return to Main Menu
                    
                    Please select an option (0-4): 
                    """);

            int choice = getValidChoice(0, 4);
            if (choice == 0) {
                break;
            }

            switch (choice) {
                case 1 -> addEnergy();
                case 2 -> listEnergy(energyManager.getAllEnergies());
                case 3 -> removeEnergy();
                case 4 -> toggleEnergy();
            }

            waitForEnter();
        }
    }


    private void addEnergy() {
        

        System.out.println("=== Add New Energy ===");
        System.out.println("Enter energy name: ");
        String name = scanner.nextLine();

        System.out.println("Available energy types:");
        Energy.EnergyType[] types = Energy.EnergyType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%d. %s\n", i + 1, types[i]);
        }

        System.out.println("Select energy type (1-" + types.length + "): ");
        int typeChoice = getValidChoice(1, types.length);
        Energy.EnergyType type = types[typeChoice - 1];

        System.out.println("Enter output: ");
        double output = getValidDouble();

        Energy energy = Energy.builder().id(UUID.randomUUID().toString()).name(name).type(type).output(output).isActive(true).build();
        energyManager.addEnergy(energy);

        System.out.println("Energy added successfully:");
        System.out.print(energy);
    }

    private void listEnergy(List<Energy> energies) {
        

        if (energies.isEmpty()) {
            System.out.println("No energies found.");
            return;
        }

        for (int i = 0; i < energies.size(); i++) {
            Energy energy = energies.get(i);
            System.out.println("Energy #" + (i + 1));
            System.out.print(energy);
        }
    }

    private void removeEnergy() {
        System.out.println("=== Delete Energy ===");

        List<Energy> energies = energyManager.getAllEnergies();
        listEnergy(energies);

        if (energies.isEmpty()) {
            return;
        }

        System.out.println("Please select the energy number to remove (1-" + energies.size() + ", 0 to return to the previous menu): ");
        int choice = getValidChoice(0, energies.size());
        if (choice == 0) return;
        String energyId = energies.get(choice - 1).getId();

        energyManager.removeEnergyById(energyId);
        System.out.println("Energy removed successfully!");
    }

    private void toggleEnergy() {
        
        System.out.println("=== Toggle Energy State ===");

        List<Energy> energies = energyManager.getAllEnergies();
        listEnergy(energies);

        if (energies.isEmpty()) {
            return;
        }

        System.out.println("Please select the energy number to toggle (1-" + energies.size() + ", 0 to return to the previous menu): ");
        int choice = getValidChoice(0, energies.size());
        if (choice == 0) return;
        String energyId = energies.get(choice - 1).getId();

        energyManager.toggleEnergyById(energyId);
        System.out.println("Device state toggled successfully!");
    }
}
