package de.fhdo.service;

import de.fhdo.model.Energy;
import de.fhdo.model.Battery;
import de.fhdo.model.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class EnergyManagerTest {
    private EnergyManager energyManager;
    private DeviceManager deviceManager;
    private Energy testEnergy;
    private Battery testBattery;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        energyManager = EnergyManager.getInstance();
        deviceManager = DeviceManager.getInstance();
        energyManager.clearAllEnergies();
        energyManager.clearAllBatteries();

        testEnergy = Energy.builder()
                .id("energy-id")
                .name("Test Energy")
                .type(Energy.EnergyType.SOLAR)
                .output(1000.0)
                .isActive(true)
                .build();

        testBattery = Battery.builder()
                .id("battery-id")
                .name("Test Battery")
                .capacity(1000.0)
                .currentCharge(500.0)
                .maxChargeRate(100.0)
                .maxDischargeRate(100.0)
                .isCharging(false)
                .build();

        testDevice = Device.builder()
                .id("device-id")
                .name("Test Device")
                .type(Device.DeviceType.LIGHTING)
                .power(50.0)
                .isActive(true)
                .build();

    }

    @Test
    void testGetInstance() {
        EnergyManager instance1 = EnergyManager.getInstance();
        EnergyManager instance2 = EnergyManager.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testAddAndGetEnergy() {
        energyManager.addEnergy(testEnergy);
        assertEquals(testEnergy, energyManager.getEnergy(testEnergy.getId()));
    }

    @Test
    void testAddAndGetBattery() {
        energyManager.addBattery(testBattery);
        assertEquals(testBattery, energyManager.getBattery(testBattery.getId()));
    }

    @Test
    void testGetAllEnergies() {
        energyManager.addEnergy(testEnergy);
        List<Energy> energies = energyManager.getAllEnergies();
        assertEquals(1, energies.size());
        assertEquals(testEnergy, energies.get(0));
    }

    @Test
    void testGetAllBatteries() {
        energyManager.addBattery(testBattery);
        List<Battery> batteries = energyManager.getAllBatteries();
        assertEquals(1, batteries.size());
        assertEquals(testBattery, batteries.get(0));
    }

    @Test
    void testRemoveEnergy() {
        energyManager.addEnergy(testEnergy);
        energyManager.removeEnergy(testEnergy.getId());
        assertTrue(energyManager.getAllEnergies().isEmpty());
    }

    @Test
    void testRemoveBattery() {
        energyManager.addBattery(testBattery);
        energyManager.removeBattery(testBattery.getId());
        assertTrue(energyManager.getAllBatteries().isEmpty());
    }

    @Test
    void testToggleEnergy() {
        energyManager.addEnergy(testEnergy);
        energyManager.toggleEnergy(testEnergy.getId());
        assertFalse(energyManager.getEnergy(testEnergy.getId()).isActive());
        
        energyManager.toggleEnergy(testEnergy.getId());
        assertTrue(energyManager.getEnergy(testEnergy.getId()).isActive());
    }

    @Test
    void testStartCharging() throws InterruptedException {
        energyManager.addBattery(testBattery);
        energyManager.addEnergy(testEnergy);
        
        energyManager.startCharging(testBattery.getId());
        TimeUnit.SECONDS.sleep(2); 
        
        Battery battery = energyManager.getBattery(testBattery.getId());
        assertTrue(battery.isCharging());
        assertTrue(battery.getCurrentCharge() > 500.0); 
    }

    @Test
    void testStartChargingWithNoActiveEnergy() {
        testEnergy.setActive(false);
        energyManager.addBattery(testBattery);
        energyManager.addEnergy(testEnergy);
        
        energyManager.startCharging(testBattery.getId());
        assertFalse(energyManager.getBattery(testBattery.getId()).isCharging());
    }

    @Test
    void testStopChargingBattery() {
        testBattery.setCharging(true);
        energyManager.addBattery(testBattery);
        
        energyManager.stopCharging(testBattery.getId());
        assertFalse(energyManager.getBattery(testBattery.getId()).isCharging());
    }

    @Test
    void testPowerDevice() {
        deviceManager.addDevice(testDevice);
        energyManager.addEnergy(testEnergy);
        energyManager.addBattery(testBattery);

        energyManager.powerDevice(testDevice.getId(), testBattery.getId());
        assertTrue(deviceManager.getDevicesByState(true).contains(testDevice));
    }

    @Test
    void testStopPowerDevice() {
        deviceManager.addDevice(testDevice);
        energyManager.addEnergy(testEnergy);
        energyManager.addBattery(testBattery);

        energyManager.powerDevice(testDevice.getId(), testBattery.getId());
        energyManager.stopPowerDevice(testDevice.getId());
        
        assertFalse(deviceManager.getDevicesByState(true).contains(testDevice));
        assertFalse(energyManager.getBatteriesByState(true).contains(testBattery));
    }

    @Test
    void testShutdown() {
        energyManager.addBattery(testBattery);
        energyManager.addEnergy(testEnergy);
        energyManager.toggleEnergy(testEnergy.getId());
        
        testBattery.setCharging(true);
        energyManager.shutdown();
        
        Battery battery = energyManager.getBattery(testBattery.getId());
        assertFalse(battery.isCharging());
    }

    @Test
    void testClearAllEnergies() {
        energyManager.addEnergy(testEnergy);
        energyManager.clearAllEnergies();
        assertTrue(energyManager.getAllEnergies().isEmpty());
    }

    @Test
    void testClearAllBatteries() {
        energyManager.addBattery(testBattery);
        energyManager.clearAllBatteries();
        assertTrue(energyManager.getAllBatteries().isEmpty());
    }
} 