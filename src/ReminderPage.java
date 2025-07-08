import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ReminderPage {
    private JFrame frame;
    private JTable reminderTable;
    private JButton modifyButton, deleteButton, backButton;
    private DefaultTableModel tableModel;

    public ReminderPage() {
        frame = Main.getInstance();
        frame.getContentPane().removeAll();
        Main.getInstance().dispose();
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());

        // Table to display reminders
        String[] columnNames = {"Course Name", "Topic", "Reminder Time"};
        tableModel = new DefaultTableModel(columnNames, 0);
        reminderTable = new JTable(tableModel);
        reminderTable.setFont(new Font("Arial", Font.PLAIN, 14));
        reminderTable.setRowHeight(30);
        reminderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set the table size (reduce it)
        reminderTable.setPreferredScrollableViewportSize(new Dimension(600, 200));

        // Set alternating row colors for better visibility
        reminderTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                } else {
                    c.setBackground(new Color(173, 216, 230)); // Light blue for selection
                }
                return c;
            }
        });

        // Table header styling
        reminderTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        reminderTable.getTableHeader().setBackground(new Color(100, 149, 237)); // Cornflower blue
        reminderTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(reminderTable);

        // GridBagConstraints to center the table
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 20, 0); // Top and bottom margins
        frame.add(scrollPane, gbc);

        // Button panel for Modify and Delete buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));  // Horizontal alignment of buttons
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBackground(new Color(245, 245, 245)); // Light gray background

        modifyButton = new JButton("Modify Reminder");
        modifyButton.setFont(new Font("Arial", Font.PLAIN, 16));
        modifyButton.setBackground(new Color(50, 205, 50)); // Lime green
        modifyButton.setForeground(Color.WHITE);
        modifyButton.setFocusPainted(false);
        modifyButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        deleteButton = new JButton("Delete Reminder");
        deleteButton.setFont(new Font("Arial", Font.PLAIN, 16));
        deleteButton.setBackground(new Color(255, 69, 0)); // Red-Orange
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(modifyButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(buttonPanel, gbc);

        backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.setBackground(new Color(30, 144, 255)); // Dodger blue
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        backButton.addActionListener(e -> goBack());

        JPanel backButtonPanel = new JPanel();
        backButtonPanel.setBackground(new Color(245, 245, 245));
        backButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(backButtonPanel, gbc);

        modifyButton.addActionListener(e -> modifyReminder());
        deleteButton.addActionListener(e -> deleteReminder());

        loadReminders();

        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
    }

    private void loadReminders() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT course_name, topic, reminder_time FROM reminders WHERE user_email = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, Main.currentemail);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String courseName = rs.getString("course_name");
                String topic = rs.getString("topic");
                Timestamp reminderTime = rs.getTimestamp("reminder_time");
                tableModel.addRow(new Object[]{courseName, topic, reminderTime});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading reminders: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void modifyReminder() {
        int selectedRow = reminderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a reminder to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseName = (String) tableModel.getValueAt(selectedRow, 0);
        String topic = (String) tableModel.getValueAt(selectedRow, 1);
        Timestamp currentReminderTime = (Timestamp) tableModel.getValueAt(selectedRow, 2);

        // Show input dialog to modify reminder time
        String newReminderTimeStr = JOptionPane.showInputDialog(frame, "Enter new reminder time (yyyy-MM-dd HH:mm:ss):", currentReminderTime.toString());
        if (newReminderTimeStr != null && !newReminderTimeStr.isEmpty()) {
            try {
                Timestamp newReminderTime = Timestamp.valueOf(newReminderTimeStr);

                // Update the reminder in the database based on courseName, topic, and user_email
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Query to update the reminder for the selected course, topic, and user_email
                    String updateQuery = "UPDATE reminders SET reminder_time = ? WHERE course_name = ? AND topic = ? AND user_email = ? AND reminder_time = ?";
                    PreparedStatement ps = conn.prepareStatement(updateQuery);
                    ps.setTimestamp(1, newReminderTime);
                    ps.setString(2, courseName);
                    ps.setString(3, topic);
                    ps.setString(4, Main.currentemail);
                    ps.setTimestamp(5, currentReminderTime);  // Ensure the correct row is being updated

                    int rowsUpdated = ps.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(frame, "Reminder updated successfully.");
                        // Update the table with the new reminder time
                        tableModel.setValueAt(newReminderTime, selectedRow, 2);
                    } else {
                        JOptionPane.showMessageDialog(frame, "No matching reminder found. It might have been modified or deleted.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IllegalArgumentException | SQLException e) {
                JOptionPane.showMessageDialog(frame, "Invalid date format or error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteReminder() {
        int selectedRow = reminderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a reminder to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseName = (String) tableModel.getValueAt(selectedRow, 0);
        String topic = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this reminder?", "Delete Reminder", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String deleteQuery = "DELETE FROM reminders WHERE course_name = ? AND topic = ? AND user_email = ?";
                PreparedStatement ps = conn.prepareStatement(deleteQuery);
                ps.setString(1, courseName);
                ps.setString(2, topic);
                ps.setString(3, Main.currentemail);
                ps.executeUpdate();
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(frame, "Reminder deleted successfully.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error deleting reminder: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void goBack() {
        frame.dispose();
        new DashboardPage();
    }
}