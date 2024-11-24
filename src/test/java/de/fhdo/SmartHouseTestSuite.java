package de.fhdo;


import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import de.fhdo.config.HouseConfigTest;
import de.fhdo.service.LogManagerTest;
import de.fhdo.service.DeviceManagerTest;
import de.fhdo.service.EnergyManagerTest;
import de.fhdo.service.SystemMonitorTest;

@Suite
@SelectClasses({
    HouseConfigTest.class,
    LogManagerTest.class,
    DeviceManagerTest.class,
    EnergyManagerTest.class,
    SystemMonitorTest.class
})
public class SmartHouseTestSuite {
} 