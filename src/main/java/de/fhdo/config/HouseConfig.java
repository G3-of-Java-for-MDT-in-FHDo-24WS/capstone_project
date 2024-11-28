package de.fhdo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
public class HouseConfig {
    private List<DeviceConfig> devices;
    private List<EnergyConfig> energies;
    private List<BatteryConfig> batteries;
    
    @Data
    public static class DeviceConfig {
        private String name;
        private String type;
        private double power;
    }
    
    @Data
    public static class EnergyConfig {
        private String name;
        private String type;
        private double output;
    }
    
    @Data
    public static class BatteryConfig {
        private String name;
        private double capacity;
        private double maxChargeRate;
    }
    
    public static HouseConfig loadFromFile(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(filename), HouseConfig.class);
    }
} 