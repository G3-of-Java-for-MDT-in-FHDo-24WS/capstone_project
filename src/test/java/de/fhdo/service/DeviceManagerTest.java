package de.fhdo.service;

import de.fhdo.model.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DeviceManagerTest {
    private DeviceManager deviceManager;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        deviceManager = DeviceManager.getInstance();
        deviceManager.clearAllDevices();
        
        testDevice = Device.builder()
                .id("test-id")
                .name("Test Device")
                .type(Device.DeviceType.LIGHTING)
                .power(100.0)
                .isActive(false)
                .build();
    }

    @Test
    void testGetInstance() {
        DeviceManager instance1 = DeviceManager.getInstance();
        DeviceManager instance2 = DeviceManager.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testAddAndGetDeviceById() {
        deviceManager.addDevice(testDevice);
        assertEquals(testDevice, deviceManager.getDeviceById(testDevice.getId()));
    }

    @Test
    void testAddDuplicateDevice() {
        deviceManager.addDevice(testDevice);
        Device duplicateDevice = Device.builder()
                .id(testDevice.getId())
                .name("Duplicate Device")
                .type(Device.DeviceType.LIGHTING)
                .power(200.0)
                .isActive(true)
                .build();
        
        deviceManager.addDevice(duplicateDevice);
        assertEquals(1, deviceManager.getAllDevices().size());
        assertEquals("Duplicate Device", deviceManager.getDeviceById(testDevice.getId()).getName());
    }

    @Test
    void testGetAllDevices() {
        deviceManager.addDevice(testDevice);
        List<Device> devices = deviceManager.getAllDevices();
        assertEquals(1, devices.size());
        assertEquals(testDevice, devices.get(0));
    }

    @Test
    void testRemoveDevice() {
        deviceManager.addDevice(testDevice);
        deviceManager.removeDevice(testDevice.getId());
        assertTrue(deviceManager.getAllDevices().isEmpty());
    }

    @Test
    void testToggleDevice() {
        deviceManager.addDevice(testDevice);
        deviceManager.toggleDevice(testDevice.getId());
        assertTrue(deviceManager.getDeviceById(testDevice.getId()).isActive());
        
        deviceManager.toggleDevice(testDevice.getId());
        assertFalse(deviceManager.getDeviceById(testDevice.getId()).isActive());
    }

    @Test
    void testGetDevicesByStateById() {
        Device activeDevice = Device.builder()
                .id("active-id")
                .name("Active Device")
                .type(Device.DeviceType.LIGHTING)
                .power(100.0)
                .isActive(true)
                .build();

        deviceManager.addDevice(testDevice);  
        deviceManager.addDevice(activeDevice);  

        List<Device> activeDevices = deviceManager.getDevicesByState(true);
        List<Device> inactiveDevices = deviceManager.getDevicesByState(false);

        assertEquals(1, activeDevices.size());
        assertEquals(1, inactiveDevices.size());
        assertEquals(activeDevice, activeDevices.get(0));
        assertEquals(testDevice, inactiveDevices.get(0));
    }

    @Test
    void testGetCurrentTotalConsumption() {
        Device device1 = Device.builder()
                .id("id-1")
                .name("Device 1")
                .type(Device.DeviceType.LIGHTING)
                .power(100.0)
                .isActive(true)
                .build();

        Device device2 = Device.builder()
                .id("id-2")
                .name("Device 2")
                .type(Device.DeviceType.APPLIANCE)
                .power(200.0)
                .isActive(true)
                .build();

        deviceManager.addDevice(device1);
        deviceManager.addDevice(device2);

        assertEquals(300.0, deviceManager.getCurrentTotalConsumption());
    }

    @Test
    void testTotalConsumptionWithInactiveDevices() {
        Device inactiveDevice = Device.builder()
                .id("inactive-id")
                .name("Inactive Device")
                .type(Device.DeviceType.LIGHTING)
                .power(100.0)
                .isActive(false)
                .build();

        deviceManager.addDevice(inactiveDevice);
        assertEquals(0.0, deviceManager.getCurrentTotalConsumption());
    }

    @Test
    void testClearAllDevices() {
        deviceManager.addDevice(testDevice);
        deviceManager.clearAllDevices();
        assertTrue(deviceManager.getAllDevices().isEmpty());
    }

    @Test
    void testGetNonExistentDevice() {
        assertThrows(IllegalArgumentException.class, () -> deviceManager.getDeviceById("non-existent-id"));
    }
} 