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
    void testAddAndGetEnergyById() {
        energyManager.addEnergy(testEnergy);
        assertEquals(testEnergy, energyManager.getEnergyById(testEnergy.getId()));
    }

    @Test
    void testAddAndGetBatteryById() {
        energyManager.addBattery(testBattery);
        assertEquals(testBattery, energyManager.getBatteryById(testBattery.getId()));
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
    void testRemoveEnergyById() {
        energyManager.addEnergy(testEnergy);
        energyManager.removeEnergyById(testEnergy.getId());
        assertTrue(energyManager.getAllEnergies().isEmpty());
    }

    @Test
    void testRemoveBatteryById() {
        energyManager.addBattery(testBattery);
        energyManager.removeBatteryById(testBattery.getId());
        assertTrue(energyManager.getAllBatteries().isEmpty());
    }

    @Test
    void testToggleEnergyById() {
        energyManager.addEnergy(testEnergy);
        energyManager.toggleEnergyById(testEnergy.getId());
        assertFalse(energyManager.getEnergyById(testEnergy.getId()).isActive());
        
        energyManager.toggleEnergyById(testEnergy.getId());
        assertTrue(energyManager.getEnergyById(testEnergy.getId()).isActive());
    }

    @Test
    void testStartCharging() throws InterruptedException {
        energyManager.addBattery(testBattery);
        energyManager.addEnergy(testEnergy);
        
        energyManager.startCharging(testBattery.getId());
        TimeUnit.SECONDS.sleep(2); 
        
        Battery battery = energyManager.getBatteryById(testBattery.getId());
        assertTrue(battery.isCharging());
        assertTrue(battery.getCurrentCharge() > 500.0); 
    }

    @Test
    void testStartChargingWithNoActiveEnergy() {
        testEnergy.setActive(false);
        energyManager.addBattery(testBattery);
        energyManager.addEnergy(testEnergy);
        
        energyManager.startCharging(testBattery.getId());
        assertFalse(energyManager.getBatteryById(testBattery.getId()).isCharging());
    }

    @Test
    void testStopChargingBattery() {
        testBattery.setCharging(true);
        energyManager.addBattery(testBattery);
        
        energyManager.stopCharging(testBattery.getId());
        assertFalse(energyManager.getBatteryById(testBattery.getId()).isCharging());
    }

    @Test
    void testStartPower() {
        deviceManager.addDevice(testDevice);
        energyManager.addEnergy(testEnergy);
        energyManager.addBattery(testBattery);

        energyManager.startPower(testDevice.getId(), testBattery.getId());
        assertTrue(deviceManager.getDevicesByState(true).contains(testDevice));
    }

    @Test
    void testStopStartPower() {
        deviceManager.addDevice(testDevice);
        energyManager.addEnergy(testEnergy);
        energyManager.addBattery(testBattery);

        energyManager.startPower(testDevice.getId(), testBattery.getId());
        energyManager.stopPowerDevice(testDevice.getId(), testBattery.getId());

        assertFalse(deviceManager.getDevicesByState(true).contains(testDevice));
        assertFalse(energyManager.getBatteriesByState(true).contains(testBattery));
    }

    @Test
    void testShutdown() {
        energyManager.addBattery(testBattery);
        energyManager.addEnergy(testEnergy);
        energyManager.toggleEnergyById(testEnergy.getId());
        
        testBattery.setCharging(true);
        energyManager.shutdown();
        
        Battery battery = energyManager.getBatteryById(testBattery.getId());
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