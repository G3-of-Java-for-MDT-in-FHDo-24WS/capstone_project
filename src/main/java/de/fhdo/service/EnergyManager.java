package de.fhdo.service;

import de.fhdo.model.Device;
import de.fhdo.model.Battery;
import de.fhdo.model.Energy;
import de.fhdo.util.LoggerHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class EnergyManager {
    private final Map<String, Battery> batteries = new ConcurrentHashMap<>();
    private final Map<String, Energy> energies = new ConcurrentHashMap<>();
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

    public Battery getBatteryById(String batteryId) {
        Battery battery = batteries.get(batteryId);

        if (battery == null) {
            throw new IllegalArgumentException("Battery not found: " + batteryId);
        }

        return battery;
    }

    public Energy getEnergyById(String energyId) {
        Energy energy = energies.get(energyId);

        if (energy == null) {
            throw new IllegalArgumentException("Energy not found: " + energyId);
        }

        return energy;
    }

    public List<Battery> getAllBatteries() {
        return List.copyOf(batteries.values());
    }

    public List<Energy> getAllEnergies() {
        return List.copyOf(energies.values());
    }

    public void removeBatteryById(String batteryId) {
        Battery battery = batteries.remove(batteryId);
        if (battery != null) {
            LoggerHelper.logBatteryEvent(logManager, "Removed battery", battery.getName());
        }
    }

    public void removeEnergyById(String energyId) {
        Energy energy = energies.remove(energyId);
        if (energy != null) {
            LoggerHelper.logEnergyEvent(logManager, "Removed energy", energy.getName());
        }
    }

    public List<Battery> getBatteriesByState(boolean isCharging) {
        return getAllBatteries().stream()
                .filter(battery -> battery.isCharging() == isCharging)
                .collect(Collectors.toList());
    }

    public List<Energy> getEnergiesByState(boolean isActive) {
        return getAllEnergies().stream()
                .filter(energy -> energy.isActive() == isActive)
                .collect(Collectors.toList());
    }

    public void clearAllEnergies() {
        energies.clear();
        log.info("All energies have been cleared");
    }

    public void clearAllBatteries() {
        batteries.clear();
        log.info("All batteries have been cleared");
    }

    public void toggleEnergyById(String energyId) {
        Energy energy = getEnergyById(energyId);
        energy.toggle();
        LoggerHelper.logEnergyEvent(logManager, energy.isActive() ? "Activated energy" : "Deactivated energy", energy.getName());
    }

    public void startCharging(String batteryId) {
        Battery battery = getBatteryById(batteryId);

        if (battery.isCharging()) {
            log.info("Battery {} is already charging", battery.getName());
            return;
        }

        List<Energy> activeEnergies = getEnergiesByState(true);

        if (activeEnergies.isEmpty()) {
            log.info("No active energy sources found to charge the battery {}", battery.getName());
            return;
        }

        battery.setCharging(true);
        CompletableFuture.runAsync(() -> manageChargingTasks(battery), executorService);
    }

    private void manageChargingTasks(Battery battery) {
        List<Energy> activeEnergies = getEnergiesByState(true);
        List<CompletableFuture<Void>> tasks = activeEnergies.stream()
                .map(energy -> CompletableFuture.runAsync(() -> chargeFromEnergy(battery, energy), executorService))
                .collect(Collectors.toList());

        try {
            while (battery.isCharging()) {
                List<Energy> newActiveEnergies = getEnergiesByState(true);

                Set<Energy> removedEnergies = new HashSet<>(activeEnergies);
                Set<Energy> addedEnergies = new HashSet<>(newActiveEnergies);

                newActiveEnergies.forEach(removedEnergies::remove);
                activeEnergies.forEach(addedEnergies::remove);

                removedEnergies.forEach(energy -> tasks.stream()
                        .filter(task -> task.isDone() && task.isCompletedExceptionally())
                        .forEach(tasks::remove));

                addedEnergies.forEach(energy -> tasks.add(CompletableFuture.runAsync(() -> chargeFromEnergy(battery, energy), executorService)));

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
                        continue;
                    }

                    battery.setCurrentCharge(Math.max(0, battery.getCurrentCharge() + netCharge));
                    LoggerHelper.logChargingEvent(logManager, battery.getName(), energy.getName(), netCharge);
                }
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopCharging(String batteryId) {
        Battery battery = getBatteryById(batteryId);

        battery.setCharging(false);
        LoggerHelper.logBatteryEvent(logManager, "Stopped charging", battery.getName());
    }

    public void startPower(String deviceId, String batteryId) {
        Device device = deviceManager.getDeviceById(deviceId);
        Battery battery = getBatteryById(batteryId);

        if (device.isActive()) {
            log.info("Device {} is already powered on", device.getName());
            return;
        }

        List<Device> activeDevices = deviceManager.getDevicesByState(true);

        if(activeDevices.isEmpty()) {
            device.setActive(true);
            CompletableFuture.runAsync(() -> manageDevicePowerTask(device, battery), executorService);
        } else {
            device.setActive(true);
        }
    }

    private void manageDevicePowerTask(Device device, Battery battery) {
        List<Device> activeDevices = deviceManager.getDevicesByState(true);
        List<CompletableFuture> tasks = activeDevices.stream()
                .map(activeDevice -> CompletableFuture.runAsync(() -> powerFromBattery(activeDevice, battery), executorService))
                .collect(Collectors.toList());

        try {
            while (true) {
                List<Device> newActiveDevices = deviceManager.getDevicesByState(true);

                Set<Device> removedDevices = new HashSet<>(activeDevices);
                Set<Device> addedDevices = new HashSet<>(newActiveDevices);

                newActiveDevices.forEach(removedDevices::remove);
                activeDevices.forEach(addedDevices::remove);

                removedDevices.forEach(activeDevice -> tasks.stream()
                        .filter(task -> task.isDone() && task.isCompletedExceptionally())
                        .forEach(tasks::remove));

                addedDevices.forEach(activeDevice -> tasks.add(CompletableFuture.runAsync(() -> powerFromBattery(activeDevice, battery), executorService)));

                if (tasks.stream().allMatch(CompletableFuture::isDone)) {
                    device.setActive(false);
                    break;
                }

                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            tasks.forEach(task -> task.cancel(true));
            device.setActive(false);
        }
    }

    private void powerFromBattery(Device device, Battery battery) {
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
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopPowerDevice(String deviceId, String batteryId) {
        Device device = deviceManager.getDeviceById(deviceId);
        Battery battery = getBatteryById(batteryId);

        device.setActive(false);
        LoggerHelper.logDevicePowerEvent(logManager, "Powered off", device.getName(), battery.getName());
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
    }
}
