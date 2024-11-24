package de.fhdo.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class Battery {
    private String id;
    private String name;
    private double capacity;
    private double currentCharge;
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
} 