package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginWindow extends JFrame {
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);

    public LoginWindow() {
        super("Login");
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridLayout(3, 2));
        add(new JLabel("User:"));
        add(userField);
        add(new JLabel("Password:"));
        add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(this::performLogin);
        add(loginBtn);

        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void performLogin(ActionEvent e) {
        try {
            Connection conn = DatabaseConnector.getConnection(
                    userField.getText(),
                    new String(passField.getPassword())
            );

            if (userField.getText().equals("admin")) {
                new AdminWindow(conn).setVisible(true);
            } else {
                new GuestWindow(conn).setVisible(true);
            }
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }
}