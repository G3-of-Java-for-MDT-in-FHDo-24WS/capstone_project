# Capstone Project

## Role Distribution and Screencast Link
Presentation Slides: [Slides Link](https://drive.google.com/file/d/1bNVuP7qbFhE3K6Wn7gKcPFdt5QmvKcBc/view?usp=sharing)<br>
Demo Video: [Video Link](https://drive.google.com/file/d/1M0F4bJZ-5qFpDAswvB9_aBaH4zMsExtW/view?usp=sharing)<br>

- Yanal Al Halabi - 7221683
  - Mainly worked on smart objects model and the services files(device, logging manager and system monitoring).
- Franklin Viegas - 7222134
  - Mainly worked on Unit Tests and a Smart object model named Energy.
- Linxin Zhang - 7221539
  - Mainly worked on the Interactive UI, Energy and Battery Management(including Concurrency Charging and Consuming).
- Anjali Bodke - 7222106
  - Mainly worked on Configuration of all the components and two helper classes named LoggerHelper and MenuHelper.


## Functional Requirements
- Application Initialization
  - The application can initialize the system by loading configurations (e.g., devices, batteries, and energy sources)
- Device Management
  - Users can add, remove, and list devices.
- Battery Management
  - Users can list all batteries.
  - Users can start and stop charging batteries.
  - Users can start and stop powering devices using battery.
- Energy Source Management
  - Users can add, remove, and list energy sources.
  - Users can toggle the state (active/inactive) of an energy source.
- System Monitoring
  - The system can monitor total power consumption and battery charge periodically.
  - The system can log warnings when power consumption exceeds available battery charge.
- Logging
  - The system can log events related to devices, batteries, and energy sources (e.g., addition, removal, state changes).
  - Logs can be categorized by type (e.g., DEVICE, BATTERY, ENERGY, SYSTEM).
  - Users can search logs by name or date.
  - Users can delete and archive logs.
- User Interface
  - The system can provide interactive menus for managing Devices, Batteries, Energy sources, Logs, System configuration, Overall system status
  - Each menu can validate user inputs and provide feedback.
- Configuration Loading
  - Users can load a new configuration file. 
  - The configuration file can define devices, batteries, and energy sources with attributes like name, type, and capacity.

