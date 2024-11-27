package de.fhdo.service;

import de.fhdo.model.Battery;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SystemMonitor {
    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final EnergyManager energyManager = EnergyManager.getInstance();
    private final LogManager logManager = LogManager.getInstance();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static SystemMonitor instance;

    private SystemMonitor() {
        startMonitoring();
    }

    public static synchronized SystemMonitor getInstance() {
        if (instance == null) {
            instance = new SystemMonitor();
        }
        return instance;
    }

    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::monitorSystemStatus, 0, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::logSystemData, 0, 5, TimeUnit.SECONDS);
    }

    private void monitorSystemStatus() {
        double totalConsumption = deviceManager.getCurrentTotalConsumption();
        double totalBatteryCharge = getTotalBatteryCharge();

        if (totalConsumption > totalBatteryCharge) {
            log.warn("Power consumption warning: Usage {}units exceeds total battery charge {}",
                    totalConsumption, totalBatteryCharge);
            logManager.logEvent(LogManager.Category.SYSTEM, "System Monitor",
                    String.format("POWER WARNING: Consumption %.2f units exceeds total battery charge %.2f",
                            totalConsumption, totalBatteryCharge));
        }
    }

    private double getTotalBatteryCharge() {
        return energyManager.getAllBatteries().stream()
                .mapToDouble(Battery::getCurrentCharge)
                .sum();
    }

    private void logSystemData() {
        double totalConsumption = deviceManager.getCurrentTotalConsumption();
        double totalBatteryCharge = getTotalBatteryCharge();

        logManager.logEvent(LogManager.Category.SYSTEM, "System Monitor",
                String.format("Total Consumption: %.2f, Total Battery Charge: %.2f",
                        totalConsumption, totalBatteryCharge));
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
