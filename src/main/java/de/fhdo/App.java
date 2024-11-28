package de.fhdo;

import de.fhdo.gui.MainFrame;
import de.fhdo.service.SystemMonitor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

@Slf4j
public class App {
    public static void main(String[] args) {
        SystemMonitor monitor = SystemMonitor.getInstance();
        monitor.startMonitoring();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
