package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class GuestWindow extends JFrame {
    private final Connection conn;
    private final JTextArea resultArea = new JTextArea();

    public GuestWindow(Connection conn) {
        super("Guest Panel");
        this.conn = conn;
        setupUI();
    }

    private void setupUI() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> performSearch(searchField.getText()));

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchBtn);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        add(panel);
        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    private void performSearch(String text) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM search_by_original(?)")) {

            stmt.setString(1, text);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(String.format("ID: %d\nURL: %s\nAlias: %s\n\n",
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3)));
            }
            resultArea.setText(sb.toString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage());
        }
    }
}