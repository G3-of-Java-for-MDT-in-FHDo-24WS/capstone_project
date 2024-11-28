package de.fhdo.model;

import lombok.Data;
import lombok.Builder;

import java.util.Objects;

@Data
@Builder
public class Device {
    private String id;
    private String name;
    private DeviceType type;
    private boolean isActive;
    private double power;

    public enum DeviceType {
        LIGHTING,
        APPLIANCE,
        HEATING
    }

    public void toggle() {
        isActive = !isActive;
    }

    public String toString() {
        return String.format("""
                ID: %s
                Name: %s
                Type: %s
                Power: %.2f units
                Status: %s
                
                """, id, name, type, power, isActive ? "Active" : "Inactive");
    }
} 