import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class SignUp implements ActionListener {
    private JTextField email;
    private JPasswordField password;
    private JPasswordField confirmPassword;

    SignUp() {
        JFrame f = Main.getInstance();
        f.getContentPane().removeAll();
        Main.getInstance().dispose();
        f.setTitle("Sign Up");
        f.setSize(450, 350);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Font and Colors
        Font font = new Font("Arial", Font.PLAIN, 16);
        Color backgroundColor = new Color(245, 245, 245); // Light Gray
        Color buttonColor = new Color(60, 179, 113); // Medium Sea Green
        Color titleColor = new Color(70, 130, 180); // Steel Blue
        Color textColor = Color.WHITE;

        // Components
        JLabel emailLabel = new JLabel("Email:");
        email = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        password = new JPasswordField();
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPassword = new JPasswordField();
        JButton signUpButton = new JButton("Sign Up");
        JButton backButton = new JButton("Back to Login");

        // Action Listeners
        signUpButton.addActionListener(this);
        backButton.addActionListener(this);

        // Styling Inputs and Labels
        email.setPreferredSize(new Dimension(200, 20));
        email.setFont(font);
        emailLabel.setFont(font);

        password.setPreferredSize(new Dimension(200, 20));
        password.setFont(font);
        passwordLabel.setFont(font);

        confirmPassword.setPreferredSize(new Dimension(200, 20));
        confirmPassword.setFont(font);
        confirmPasswordLabel.setFont(font);

        // Styling Buttons
        signUpButton.setFont(font);
        signUpButton.setBackground(buttonColor);
        signUpButton.setForeground(textColor);
        signUpButton.setFocusPainted(false);

        backButton.setFont(font);
        backButton.setBackground(titleColor);
        backButton.setForeground(textColor);
        backButton.setFocusPainted(false);

        // Panel Layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        panel.add(email, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        panel.add(password, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        panel.add(confirmPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 40, 10, 40);
        panel.add(signUpButton, gbc);

        gbc.gridx = 1;
        panel.add(backButton, gbc);

        // Adding Background Panel
        JPanel backgroundPanel = new JPanel(new BorderLayout());
        backgroundPanel.setBackground(titleColor);

        JLabel titleLabel = new JLabel("Create Your Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(textColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        backgroundPanel.add(titleLabel, BorderLayout.NORTH);
        backgroundPanel.add(panel, BorderLayout.CENTER);

        f.add(backgroundPanel);
        f.revalidate();
        f.setVisible(true);
    }

    private void handleSignUp() {
        String email = this.email.getText();
        String password = String.valueOf(this.password.getPassword());
        String confirmPassword = String.valueOf(this.confirmPassword.getPassword());

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(Main.getInstance(), "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
        } else if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(Main.getInstance(), "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO users (email, password) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, email);
                ps.setString(2, password);
                Main.currentemail = email;

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(Main.getInstance(), "Sign-Up Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    Main.getInstance().dispose();
                    new DashboardPage();
                } else {
                    JOptionPane.showMessageDialog(Main.getInstance(), "Error during sign-up!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(Main.getInstance(), "Database connection error!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleBackToLogin() {
        Main.getInstance().dispose();
        new LogIn();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Back to Login")) {
            handleBackToLogin();
        } else if (e.getActionCommand().equals("Sign Up")) {
            handleSignUp();
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
