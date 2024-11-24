package de.fhdo.util;
import de.fhdo.service.LogManager;

public class LoggerHelper {
    public static void logEvent(LogManager logManager, LogManager.Category category, String action, String name, String additionalInfo) {
        String message = String.format("%s: %s", action, name);
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            message += " - " + additionalInfo;
        }
        logManager.logEvent(category, name, message);
    }

    public static void logEnergyEvent(LogManager logManager, String action, String energyName) {
        logEvent(logManager, LogManager.Category.ENERGY, action, energyName, null);
    }

    public static void logBatteryEvent(LogManager logManager, String action, String batteryName) {
        logEvent(logManager, LogManager.Category.BATTERY, action, batteryName, null);
    }

    public static void logChargingEvent(LogManager logManager, String batteryName, String energyName, double amount) {
        String chargeInfo = String.format("Charged %.2f from %s",
                amount, energyName);
        String dischargeInfo = String.format("Discharged %.2f to %s",
                amount, batteryName);
        logEvent(logManager, LogManager.Category.BATTERY, "Charging", batteryName, chargeInfo);
        logEvent(logManager, LogManager.Category.ENERGY, "Discharging", energyName, dischargeInfo);
    }

    public static void logDeviceEvent(LogManager logManager, String action, String deviceName) {
        logEvent(logManager, LogManager.Category.DEVICE, action, deviceName, null);
    }

    public static void logDevicePowerEvent(LogManager logManager, String action, String deviceName, String batteryName) {
        String status = action.contains("on") ? "powered" : "shut down";
        String deviceInfo = String.format("Is now %s by battery %s",
                status, batteryName);
        String batteryInfo = String.format("Is now %s device %s",
                status, deviceName);
        String batteryAction = action.contains("on") ? "Charging" : "Discharging";
        logEvent(logManager, LogManager.Category.DEVICE, action, deviceName, deviceInfo);
        logEvent(logManager, LogManager.Category.BATTERY, batteryAction, batteryName, batteryInfo);
    }
}
