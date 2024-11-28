package de.fhdo.gui;

import de.fhdo.model.Battery;
import de.fhdo.model.Energy;
import de.fhdo.service.EnergyManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.UUID;
import java.util.Vector;
import java.util.List;

public class EnergyPanel extends JPanel {
    private final EnergyManager energyManager;
    private JTable energyTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton removeButton;
    private JButton toggleButton;
    private Timer updateTimer;

    public EnergyPanel(EnergyManager energyManager) {
        this.energyManager = energyManager;
        setLayout(new BorderLayout());
        initializeComponents();
        layoutComponents();
        updateEnergyTable();
        setupUpdateTimer();
    }

    private void initializeComponents() {
        String[] columnNames = {"Name", "Type", "Output", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        energyTable = new JTable(tableModel);
        energyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addButton = new JButton("Add Energy Source");
        removeButton = new JButton("Remove Energy Source");
        toggleButton = new JButton("Toggle Status");

        addButton.addActionListener(e -> showAddEnergyDialog());
        removeButton.addActionListener(e -> removeSelectedEnergy());
        toggleButton.addActionListener(e -> toggleSelectedEnergy());
    }

    private void layoutComponents() {
        JScrollPane scrollPane = new JScrollPane(energyTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(toggleButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showAddEnergyDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Energy Source", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField nameField = new JTextField(20);
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);


        JComboBox<Energy.EnergyType> typeCombo = new JComboBox<>(Energy.EnergyType.values());
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        dialog.add(typeCombo, gbc);

        JSpinner outputSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1));
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Output:"), gbc);
        gbc.gridx = 1;
        dialog.add(outputSpinner, gbc);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter an energy source name", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            Energy energy = Energy.builder()
                    .id(UUID.randomUUID().toString())
                    .name(nameField.getText().trim())
                    .type((Energy.EnergyType) typeCombo.getSelectedItem())
                    .output((Double) outputSpinner.getValue())
                    .isActive(true)
                    .build();

            energyManager.addEnergy(energy);
            updateEnergyTable();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void removeSelectedEnergy() {
        int selectedRow = energyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an energy source to remove",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Energy energy = energyManager.getAllEnergies().get(selectedRow);
        if(energy.isActive()) {
            JOptionPane.showMessageDialog(this,
                    "Please deactivate the energy source before removing it",
                    "Active Source",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove this energy source?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            energyManager.removeEnergyById(energy.getId());
            updateEnergyTable();
        }
    }

    private void toggleSelectedEnergy() {
        int selectedRow = energyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an energy source to toggle",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Energy energy = energyManager.getAllEnergies().get(selectedRow);

        List<Battery> activeBatteries = energyManager.getBatteriesByState(true);
        if (!activeBatteries.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please deactivate all batteries using this energy source before toggling",
                    "Active Batteries",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        energyManager.toggleEnergyById(energy.getId());
        updateEnergyTable();
    }

    public void updateEnergyTable() {
        List<Energy> energies = energyManager.getAllEnergies();

        int selectedRow = energyTable.getSelectedRow();
        String selectEnergyId = null;
        if(selectedRow >= 0 && selectedRow < energies.size()) {
            selectEnergyId = energyManager.getAllEnergies().get(selectedRow).getId();
        }

        tableModel.setRowCount(0);
        for (int i = 0; i < energies.size(); i++) {
            Energy energy = energies.get(i);
            Vector<Object> row = new Vector<>();
            row.add(energy.getName());
            row.add(energy.getType());
            row.add(energy.getOutput());
            row.add(energy.isActive() ? "Active" : "Inactive");
            tableModel.addRow(row);

            if (selectEnergyId != null && energy.getId().equals(selectEnergyId)) {
                energyTable.setRowSelectionInterval(i, i);
            }
        }
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(1000, e -> updateEnergyTable());
        updateTimer.start();
    }
}