package de.fhdo.model;

import lombok.Data;
import lombok.Builder;

import java.util.Objects;

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
                Output: %.2f units
                Status: %s
                
                """, id, name, type, output, isActive ? "Active" : "Inactive");
    }

    public void toggle() {
        isActive = !isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Energy energy = (Energy) o;
        return Objects.equals(id, energy.id) && Objects.equals(name, energy.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
} 