package de.fhdo;

import de.fhdo.ui.MainMenu;
import de.fhdo.service.SystemMonitor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) {
        SystemMonitor monitor = SystemMonitor.getInstance();
        MainMenu mainMenu = new MainMenu();

        mainMenu.show();
        monitor.shutdown();
    }
}
