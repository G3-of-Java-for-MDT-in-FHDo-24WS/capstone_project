package de.fhdo.util;

import de.fhdo.model.Device;
import lombok.Getter;

import java.util.List;
import java.util.Scanner;

public class MenuHelper {
    @Getter
    private static final Scanner scanner = new Scanner(System.in);

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static int getValidChoice(int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.printf("Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: \n");
            }
        }
    }

    public static double getValidDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: \n");
            }
        }
    }

    public static void listDevices(List<Device> devices) {
        if (devices.isEmpty()) {
            System.out.println("No devices found.");
            return;
        }

        for (int i = 0; i < devices.size(); i++) {
            Device device = devices.get(i);
            System.out.println("Device #" + (i + 1));
            System.out.print(device);
        }
    }
}
