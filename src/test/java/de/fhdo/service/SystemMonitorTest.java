package de.fhdo.service;

import de.fhdo.model.Device;
import de.fhdo.model.Battery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public class SystemMonitorTest {
    private SystemMonitor systemMonitor;
    private DeviceManager deviceManager;
    private EnergyManager energyManager;
    private LogManager logManager;

    @BeforeEach
    void setUp() {
        systemMonitor = SystemMonitor.getInstance();
        deviceManager = DeviceManager.getInstance();
        energyManager = EnergyManager.getInstance();
        logManager = LogManager.getInstance();
        
        deviceManager.clearAllDevices();
        energyManager.clearAllEnergies();
        energyManager.clearAllBatteries();
    }

    @Test
    void testGetInstance() {
        SystemMonitor instance1 = SystemMonitor.getInstance();
        SystemMonitor instance2 = SystemMonitor.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testMonitoringWithHighConsumption() throws InterruptedException {
        Device device = Device.builder()
                .id("device-id")
                .name("High Power Device")
                .type(Device.DeviceType.APPLIANCE)
                .power(2000.0)
                .isActive(true)
                .build();

        Battery battery = Battery.builder()
                .id("battery-id")
                .name("Low Charge Battery")
                .capacity(1000.0)
                .currentCharge(100.0)
                .maxChargeRate(100.0)
                .isCharging(false)
                .build();

        deviceManager.addDevice(device);
        energyManager.addBattery(battery);

        TimeUnit.SECONDS.sleep(6);
        
        List<String> logs = logManager.readLogFile(Paths.get("logs/system/System Monitor_" + 
            LocalDate.now().format(logManager.DATE_FORMAT) + ".log"));
        boolean hasWarning = logs.stream()
                .anyMatch(log -> log.contains("POWER WARNING"));
        assertTrue(hasWarning);
    }

    @Test
    void testSystemDataLogging() throws InterruptedException {
        Device device = Device.builder()
                .id("device-id")
                .name("Test Device")
                .type(Device.DeviceType.LIGHTING)
                .power(100.0)
                .isActive(true)
                .build();

        Battery battery = Battery.builder()
                .id("battery-id")
                .name("Test Battery")
                .capacity(1000.0)
                .currentCharge(500.0)
                .maxChargeRate(100.0)
                .isCharging(false)
                .build();

        deviceManager.addDevice(device);
        energyManager.addBattery(battery);

        TimeUnit.SECONDS.sleep(3);
        
        List<String> logs = logManager.readLogFile(Paths.get("logs/system/System Monitor_" + 
            LocalDate.now().format(logManager.DATE_FORMAT) + ".log"));
        boolean hasSystemData = logs.stream()
                .anyMatch(log -> log.contains("Total Consumption") && 
                               log.contains("Total Battery Charge"));
        assertTrue(hasSystemData);
    }

    @Test
    void testShutdown() throws InterruptedException {
        systemMonitor.shutdown();
        TimeUnit.SECONDS.sleep(3);
        List<String> logs = logManager.readLogFile(Paths.get("logs/system/System Monitor_" +
            LocalDate.now().format(logManager.DATE_FORMAT) + ".log"));
        int logCountBefore = logs.size();
        
        TimeUnit.SECONDS.sleep(3);
        logs = logManager.readLogFile(Paths.get("logs/system/System Monitor_" +
            LocalDate.now().format(logManager.DATE_FORMAT) + ".log"));
        assertEquals(logCountBefore, logs.size());
    }
} 