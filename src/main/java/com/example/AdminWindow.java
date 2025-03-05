package com.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AdminWindow extends JFrame {
    private final Connection conn;
    private final JTable table = new JTable();
    private final DefaultTableModel tableModel = new DefaultTableModel();

    public AdminWindow(Connection conn) {
        super("Admin Panel");
        this.conn = conn;
        setupUI();
        refreshTable();
        setVisible(true);
    }

    private void setupUI() {
        tableModel.setColumnIdentifiers(new String[]{"ID", "Original URL", "Alias", "Created"});
        table.setModel(tableModel);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel btnPanel = new JPanel();
        btnPanel.add(createButton("Create User", this::createUser));
        btnPanel.add(createButton("Add", this::addLink));
        btnPanel.add(createButton("Update", this::updateLink));
        btnPanel.add(createButton("Delete", this::deleteLink));
        btnPanel.add(createButton("Clear", this::clearTable));
        btnPanel.add(createButton("Refresh", e -> refreshTable()));

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(panel);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        return btn;
    }

    private void refreshTable() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    tableModel.setRowCount(0);

                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM links")) {

                        while (rs.next()) {
                            tableModel.addRow(new Object[]{
                                    rs.getLong("id"),
                                    rs.getString("original"),
                                    rs.getString("alias"),
                                    rs.getTimestamp("created_at")
                            });
                        }
                    }
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> showError(ex));
                }
                return null;
            }
        };
        worker.execute();
    }

    private void createUser(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(3, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox adminCheckBox = new JCheckBox("Is Admin?");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Admin Privileges:"));
        panel.add(adminCheckBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Create User",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            boolean isAdmin = adminCheckBox.isSelected();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }

            try {
                DatabaseConnector.executeProcedure(
                        conn,
                        "call create_user(?, ?, ?)",
                        username, password, isAdmin
                );
                JOptionPane.showMessageDialog(this, "User created successfully!");
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

    private void addLink(ActionEvent e) {
        String original = JOptionPane.showInputDialog(this, "Original URL:");
        if (original == null || original.isEmpty()) return;

        String alias = JOptionPane.showInputDialog(this, "Alias:");
        if (alias == null || alias.isEmpty()) return;

        try {
            DatabaseConnector.executeProcedure(conn, "call add_link(?, ?)", original, alias);
            refreshTable();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void updateLink(ActionEvent e) {
        String input = JOptionPane.showInputDialog(this, "Enter link ID to update:");
        if (input == null || input.isEmpty()) return;

        try {
            long id = Long.parseLong(input);
            String original = JOptionPane.showInputDialog(this, "New Original URL:");
            String alias = JOptionPane.showInputDialog(this, "New Alias:");

            DatabaseConnector.executeProcedure(
                    conn,
                    "call update_link(?, ?, ?)",
                    id, original, alias
            );
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid ID format");
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void deleteLink(ActionEvent e) {
        String alias = JOptionPane.showInputDialog(this, "Enter alias to delete:");
        if (alias == null || alias.isEmpty()) return;

        try {
            DatabaseConnector.executeProcedure(conn, "call delete_by_alias(?)", alias);
            refreshTable();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void clearTable(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Clear entire table? This cannot be undone!",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseConnector.executeProcedure(conn, "call clear_table()");
                refreshTable();
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "Error: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
        ex.printStackTrace();
    }
}