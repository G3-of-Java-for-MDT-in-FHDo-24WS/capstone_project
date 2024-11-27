package de.fhdo.model;

import lombok.Data;
import lombok.Builder;

import java.util.Objects;

@Data
@Builder
public class Battery {
    private String id;
    private String name;
    private double capacity = 100.0; // Max capacity is 100.0
    private double currentCharge = 0.0;
    private double maxChargeRate;
    private double maxDischargeRate;
    private boolean isCharging;

    public String toString() {
        return String.format("""
                ID: %s
                Name: %s
                Capacity: %.2f
                Current Charge: %.2f
                Max Charge Rate: %.2f
                Max Discharge Rate: %.2f
                Status: %s
                
                """, id, name, capacity, currentCharge, maxChargeRate, maxDischargeRate, isCharging ? "Charging" : "Not Charging");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Battery battery = (Battery) o;
        return Objects.equals(id, battery.id) && Objects.equals(name, battery.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
} 