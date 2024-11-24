package de.fhdo.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class Energy {
    private String id;
    private String name;
    private double output;
    private boolean isActive;
    private EnergyType type;

    public enum EnergyType {
        SOLAR,
        GRID
    }

    public String toString() {
        return String.format("""
                ID: %s
                Name: %s
                Type: %s
                Output: %.2f W
                Status: %s
                
                """, id, name, type, output, isActive ? "Active" : "Inactive");
    }

    public void toggle() {
        isActive = !isActive;
    }
} 