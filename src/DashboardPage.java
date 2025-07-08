import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DashboardPage implements ActionListener {
    private JFrame f;
    private JButton viewScheduleButton, createScheduleButton, logoutButton, remindersButton;

    public DashboardPage() {
        f = Main.getInstance();
        f.getContentPane().removeAll();
        Main.getInstance().dispose();
        f.setTitle("Dashboard");

        Color backgroundColor = new Color(245, 245, 245);
        Color viewButtonColor = new Color(135, 206, 250);
        Color createButtonColor = new Color(144, 238, 144);
        Color logoutButtonColor = new Color(255, 160, 122);
        Color remindersButtonColor = new Color(255, 228, 196);
        Color hoverColor = new Color(70, 130, 180);
        Color textColor = Color.WHITE;

        Font buttonFont = new Font("Arial", Font.BOLD, 18);
        Font titleFont = new Font("Arial", Font.BOLD, 28);

        JLabel welcomeLabel = new JLabel("Welcome to your Study Planner!", JLabel.CENTER);
        welcomeLabel.setFont(titleFont);
        welcomeLabel.setForeground(new Color(0, 102, 153));

        viewScheduleButton = createStyledButton("View Schedule", buttonFont, viewButtonColor, textColor, hoverColor);
        createScheduleButton = createStyledButton("Create New Schedule", buttonFont, createButtonColor, textColor, hoverColor);
        logoutButton = createStyledButton("Logout", buttonFont, logoutButtonColor, textColor, hoverColor);
        remindersButton = createStyledButton("Edit Reminders", buttonFont, remindersButtonColor, textColor, hoverColor);

        viewScheduleButton.setActionCommand("view_schedule");
        createScheduleButton.setActionCommand("create_schedule");
        logoutButton.setActionCommand("logout");
        remindersButton.setActionCommand("edit_reminders");

        viewScheduleButton.addActionListener(this);
        createScheduleButton.addActionListener(this);
        logoutButton.addActionListener(this);
        remindersButton.addActionListener(this);

        f.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 30, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        f.add(welcomeLabel, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 50, 30));
        buttonPanel.add(viewScheduleButton);
        buttonPanel.add(createScheduleButton);
        buttonPanel.add(remindersButton);
        buttonPanel.add(logoutButton);
        buttonPanel.setBackground(backgroundColor);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        f.add(buttonPanel, gbc);

        f.getContentPane().setBackground(backgroundColor);
        f.setSize(500, 500);
        f.setVisible(true);
        f.revalidate();
        f.repaint();
    }

    private JButton createStyledButton(String text, Font font, Color background, Color foreground, Color hoverBackground) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverBackground);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(background);
            }
        });

        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        switch (action) {
            case "view_schedule":
                handleViewSchedule();
                break;
            case "create_schedule":
                handleCreateSchedule();
                break;
            case "logout":
                handleLogout();
                break;
            case "edit_reminders":
                handleEditReminders();
                break;
            default:
                System.out.println("Unknown action: " + action);
        }
    }

    private void handleViewSchedule() {
        new ViewSchedule();
    }

    private void handleCreateSchedule() {
        new SchedulePage();
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(f, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            f.getContentPane().removeAll();
            f.revalidate();
            f.repaint();
            new LogIn();
        }
    }

    private void handleEditReminders() {
        new ReminderPage();
    }
}
