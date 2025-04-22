package client;

import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {

    public LoginUI() {
        setTitle("Bienvenue sur IDEL");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Color.WHITE);
        ImageIcon logo = new ImageIcon("Assets/logo.png");
        JLabel logoLabel = new JLabel(logo);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(logoLabel, BorderLayout.CENTER);
        add(logoPanel, BorderLayout.CENTER);

        JButton entrerButton = new JButton("Entrer");
        entrerButton.setBackground(Color.decode("#f5a623"));
        entrerButton.setForeground(Color.BLACK);
        entrerButton.setFont(new Font("Arial", Font.BOLD, 14));
        entrerButton.addActionListener(e -> {
            new ClientUI();
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(entrerButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}
