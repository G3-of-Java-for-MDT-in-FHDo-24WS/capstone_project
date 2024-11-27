package de.fhdo.config;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class HouseConfigTest {
    
    @Test
    void testLoadFromFile() throws IOException {
        HouseConfig config = HouseConfig.loadFromFile("src/test/resources/house_config.yml");
        
        assertNotNull(config);
        assertNotNull(config.getDevices());
        assertNotNull(config.getEnergies());
        assertNotNull(config.getBatteries());
        
        assertEquals(1, config.getDevices().size());
        HouseConfig.DeviceConfig firstDevice = config.getDevices().get(0);
        assertEquals("Living Room Lights", firstDevice.getName());
        assertEquals("LIGHTING", firstDevice.getType());
        assertEquals(100.0, firstDevice.getPower());
        
        assertEquals(1, config.getEnergies().size());
        HouseConfig.EnergyConfig firstEnergy = config.getEnergies().get(0);
        assertEquals("Solar Panels", firstEnergy.getName());
        assertEquals("SOLAR", firstEnergy.getType());
        assertEquals(3000.0, firstEnergy.getOutput());
        
        assertEquals(1, config.getBatteries().size());
        HouseConfig.BatteryConfig battery = config.getBatteries().get(0);
        assertEquals("Main Battery", battery.getName());
        assertEquals(10000.0, battery.getCapacity());
        assertEquals(2000.0, battery.getMaxChargeRate());
        assertEquals(2000.0, battery.getMaxDischargeRate());
    }
    
    @Test
    void testLoadFromNonExistentFile() {
        assertThrows(IOException.class, () -> 
            HouseConfig.loadFromFile("non_existent_file.yml")
        );
    }
} 