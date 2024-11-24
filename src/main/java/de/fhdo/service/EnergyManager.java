package de.fhdo.service;

import de.fhdo.model.Device;
import de.fhdo.model.Battery;
import de.fhdo.model.Energy;
import de.fhdo.util.LoggerHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class EnergyManager {
    private final Map<String, Battery> batteries = new ConcurrentHashMap<>();
    private final Map<String, Energy> energies = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Void>> devicePowerTasks = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final LogManager logManager = LogManager.getInstance();

    private static volatile EnergyManager instance;

    private EnergyManager() {
    }

    public static EnergyManager getInstance() {
        if (instance == null) {
            synchronized (EnergyManager.class) {
                if (instance == null) {
                    instance = new EnergyManager();
                }
            }
        }
        return instance;
    }

    public void addEnergy(Energy energy) {
        energies.put(energy.getId(), energy);
        LoggerHelper.logEnergyEvent(logManager, "Added new energy", energy.getName());
    }

    public void addBattery(Battery battery) {
        batteries.put(battery.getId(), battery);
        LoggerHelper.logBatteryEvent(logManager, "Added new battery", battery.getName());
    }

    public void removeBattery(String batteryId) {
        Battery battery = batteries.remove(batteryId);
        if (battery != null) {
            LoggerHelper.logBatteryEvent(logManager, "Removed battery", battery.getName());
        }
    }

    public Battery getBattery(String batteryId) {
        return batteries.get(batteryId);
    }

    public List<Battery> getAllBatteries() {
        return List.copyOf(batteries.values());
    }

    public List<Battery> getBatteriesByState(boolean isCharging) {
        return batteries.values().stream()
                .filter(battery -> battery.isCharging() == isCharging)
                .collect(Collectors.toList());
    }

    public List<Energy> getAllEnergies() {
        return List.copyOf(energies.values());
    }

    public Energy getEnergy(String energyId) {
        Energy energy = energies.get(energyId);
        if (energy == null) {
            throw new IllegalArgumentException("Energy not found: " + energyId);
        }
        return energy;
    }

    public void clearAllEnergies() {
        energies.clear();
        log.info("All energies have been cleared");
    }

    public void clearAllBatteries() {
        batteries.clear();
        log.info("All batteries have been cleared");
    }

    public void removeEnergy(String energyId) {
        Energy energy = energies.remove(energyId);
        if (energy != null) {
            LoggerHelper.logEnergyEvent(logManager, "Removed energy", energy.getName());
        }
    }

    public void startCharging(String batteryId) {
        Battery battery = batteries.get(batteryId);
        if (battery == null || battery.isCharging()) {
            return;
        }

        battery.setCharging(true);

        List<Energy> activeEnergies = energies.values().stream()
                .filter(Energy::isActive)
                .collect(Collectors.toList());

        if (activeEnergies.isEmpty()) {
            battery.setCharging(false);
            return;
        }

        CompletableFuture.runAsync(() -> manageChargingTasks(battery, activeEnergies), executorService);
    }

    private void manageChargingTasks(Battery battery, List<Energy> activeEnergies) {
        List<CompletableFuture<Void>> tasks = activeEnergies.stream()
                .map(energy -> CompletableFuture.runAsync(() -> chargeFromEnergy(battery, energy), executorService))
                .toList();

        try {
            while (battery.isCharging()) {
                if (tasks.stream().allMatch(CompletableFuture::isDone)) {
                    battery.setCharging(false);
                    break;
                }
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            tasks.forEach(task -> task.cancel(true));
            battery.setCharging(false);
        }
    }

    private void chargeFromEnergy(Battery battery, Energy energy) {
        try {
            while (battery.isCharging()) {
                synchronized (battery) {
                    double availablePower = energy.getOutput();
                    double batteryDeficit = battery.getCapacity() - battery.getCurrentCharge();
                    double deviceConsumption = deviceManager.getTotalConsumption();

                    double chargePower = Math.min(battery.getMaxChargeRate(), availablePower);

                    if (batteryDeficit <= 0 && deviceConsumption <= 0) {
                        break;
                    }

                    double netCharge = chargePower - deviceConsumption;
                    if (netCharge > 0) {
                        double chargeAmount = Math.min(netCharge, batteryDeficit);
                        battery.setCurrentCharge(battery.getCurrentCharge() + chargeAmount);
                        LoggerHelper.logChargingEvent(logManager, battery.getName(), energy.getName(), chargeAmount);
                    } else {
                        battery.setCurrentCharge(Math.max(0, battery.getCurrentCharge() + netCharge));
                        LoggerHelper.logChargingEvent(logManager, battery.getName(), energy.getName(), netCharge);
                    }
                }
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopCharging(String batteryId) {
        Battery battery = batteries.get(batteryId);
        if (battery != null) {
            battery.setCharging(false);
        }
    }

    public void toggleEnergy(String energyId) {
        Energy energy = getEnergy(energyId);
        energy.toggle();
        LoggerHelper.logEnergyEvent(logManager, energy.isActive() ? "Activated energy" : "Deactivated energy", energy.getName());
    }

    public void powerDevice(String deviceId, String batteryId) {
        Device device = deviceManager.getDevice(deviceId);
        Battery battery = batteries.get(batteryId);

        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        if (battery == null) {
            throw new IllegalArgumentException("Battery not found: " + batteryId);
        }

        if (device.isActive()) {
            log.info("Device {} is already powered on", device.getName());
            return;
        }

        device.setActive(true);
        LoggerHelper.logDevicePowerEvent(logManager, "Device powered on", device.getName(), battery.getName());

        CompletableFuture<Void> deviceTask = CompletableFuture.runAsync(() -> manageDevicePowerTask(device, battery), executorService);
        devicePowerTasks.put(deviceId, deviceTask);
    }

    private void manageDevicePowerTask(Device device, Battery battery) {
        try {
            while (device.isActive()) {
                synchronized (battery) {
                    double consumption = device.getPower();
                    if (battery.getCurrentCharge() >= consumption) {
                        battery.setCurrentCharge(battery.getCurrentCharge() - consumption);
                        LoggerHelper.logDevicePowerEvent(logManager, "Consuming power", device.getName(), battery.getName());
                    } else {
                        log.info("Battery {} does not have enough charge to power the device {}", battery.getId(), device.getName());
                        device.setActive(false);
                        LoggerHelper.logDevicePowerEvent(logManager, "Powered off due to low battery", device.getName(), battery.getName());
                        break;
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopPowerDevice(String deviceId) {
        Device device = deviceManager.getDevice(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        device.setActive(false);
        LoggerHelper.logDevicePowerEvent(logManager, "Powered off", device.getName(), null);

        CompletableFuture<Void> deviceTask = devicePowerTasks.remove(deviceId);
        if (deviceTask != null) {
            deviceTask.cancel(true);
        }
    }

    public void shutdown() {
        energies.values().forEach(energy -> energy.setActive(false));
        batteries.values().forEach(battery -> battery.setCharging(false));
        deviceManager.getAllDevices().forEach(device -> device.setActive(false));

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        devicePowerTasks.values().forEach(task -> task.cancel(true));
    }
}
