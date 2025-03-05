package org.example;

import com.example.LoginWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());
        }
    }
}