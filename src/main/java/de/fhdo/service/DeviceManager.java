package de.fhdo.service;

import de.fhdo.model.Device;
import de.fhdo.util.LoggerHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DeviceManager {

    private final Map<String, Device> devices = new ConcurrentHashMap<>();
    private final LogManager logManager = LogManager.getInstance();

    private static volatile DeviceManager instance;

    private DeviceManager() {
    }

    public static DeviceManager getInstance() {
        if (instance == null) {
            synchronized (DeviceManager.class) {
                if (instance == null) {
                    instance = new DeviceManager();
                }
            }
        }
        return instance;
    }

    public void addDevice(Device device) {
        devices.put(device.getId(), device);
        LoggerHelper.logDeviceEvent(logManager, "Added new device", device.getName());
    }

    public void removeDevice(String deviceId) {
        Device device = devices.remove(deviceId);
        if (device != null) {
            LoggerHelper.logDeviceEvent(logManager, "Removed device", device.getName());
        } else {
            log.warn("Attempted to remove a device that does not exist: {}", deviceId);
        }
    }

    public void toggleDevice(String deviceId) {
        Device device = getDeviceById(deviceId);
        device.toggle();
        LoggerHelper.logDeviceEvent(logManager, device.isActive() ? "Activated" : "Deactivated", device.getName());
    }

    public Device getDeviceById(String deviceId) {
        Device device = devices.get(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }
        return device;
    }

    public List<Device> getAllDevices() {
        return List.copyOf(devices.values());
    }

    public List<Device> getDevicesByState(boolean isActive) {
        return devices.values().stream()
                .filter(device -> device.isActive() == isActive)
                .toList();
    }

    public double getTotalConsumption() {
        return devices.values().stream()
                .filter(Device::isActive)
                .mapToDouble(Device::getPower)
                .sum();
    }

    public void clearAllDevices() {
        devices.clear();
        log.info("All devices have been cleared.");
    }
}
