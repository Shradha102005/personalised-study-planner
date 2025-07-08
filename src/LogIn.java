import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
public class LogIn implements ActionListener {
    private JTextField email;
    private JPasswordField password;
    LogIn() {
        JFrame f = Main.getInstance();
        f.getContentPane().removeAll();
        f.setTitle("Log In");
        f.setSize(400, 300);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Font font = new Font("Arial", Font.PLAIN, 16);
        JLabel emailLabel = new JLabel("Email:");
        email = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        password = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton signUpButton = new JButton("Sign Up");
        loginButton.addActionListener(this);
        signUpButton.addActionListener(this);
        email.setPreferredSize(new Dimension(200, 20));
        email.setFont(font);
        emailLabel.setFont(font);
        password.setPreferredSize(new Dimension(200, 20));
        password.setFont(font);
        passwordLabel.setFont(font);
        loginButton.setFont(font);
        loginButton.setBackground(new Color(60, 179, 113)); 
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        signUpButton.setFont(font);
        signUpButton.setBackground(new Color(30, 144, 255)); 
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFocusPainted(false);
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245)); 
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
        gbc.gridwidth = 1;
        panel.add(loginButton, gbc);
        gbc.gridx = 1;
        panel.add(signUpButton, gbc);
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBackground(new Color(70, 130, 180)); 
        JLabel titleLabel = new JLabel("Welcome to the Study Planner", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        backgroundPanel.add(titleLabel, BorderLayout.NORTH);
        backgroundPanel.add(panel, BorderLayout.CENTER);
        f.add(backgroundPanel);
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Login")) {
            handleLogin();
        } else if (e.getActionCommand().equals("Sign Up")) {
            handleSignUp();
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    private void handleLogin() {
        String email = this.email.getText();
        String password = String.valueOf(this.password.getPassword());
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            Main.currentemail = email;
            if (rs.next()) {
                JOptionPane.showMessageDialog(Main.getInstance(), "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                Main.getInstance().dispose();
                ReminderNotifierApp notifier = new ReminderNotifierApp();
                notifier.startReminderNotifications(); 
                new DashboardPage();
            } else {
                JOptionPane.showMessageDialog(Main.getInstance(), "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            ps.close();
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(Main.getInstance(), "Database connection error!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void handleSignUp() {
        Main.getInstance().dispose();
        new SignUp();
    } 

}
