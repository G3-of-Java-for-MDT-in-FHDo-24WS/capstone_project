package de.fhdo.model;

import lombok.Data;
import lombok.Builder;

import java.util.Objects;

@Data
@Builder
public class Battery {
    private String id;
    private String name;
    private double capacity;
    private double currentCharge;
    private double maxChargeRate;
    private boolean isCharging;

    public String toString() {
        return String.format("""
                ID: %s
                Name: %s
                Capacity: %.2f%% (%.2f units)
                Current Charge: %.2f%% (%.2f units)
                Max Charge Rate: %.2f%% (%.2f units)
                Status: %s
                
                """, id, name, currentCharge / capacity * 100, currentCharge, currentCharge / capacity * 100, currentCharge, maxChargeRate / capacity * 100, maxChargeRate, isCharging ? "Charging" : "Discharging");
    }
} 