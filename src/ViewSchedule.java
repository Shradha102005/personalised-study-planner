import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ViewSchedule implements ActionListener {
    private JFrame f;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JButton editButton, backButton, markDoneButton;
    private int selectedRow = -1;

    public ViewSchedule() {
        f = Main.getInstance();
        f.getContentPane().removeAll();
        Main.getInstance().dispose();
        f.setTitle("View and Customize Generated Schedule");
        f.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Course Name", "Date", "Topic", "Done"}, 0);
        scheduleTable = new JTable(tableModel);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.setFont(new Font("Arial", Font.PLAIN, 14));
        scheduleTable.setRowHeight(30);

        scheduleTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        scheduleTable.getTableHeader().setBackground(new Color(100, 149, 237)); // Cornflower blue
        scheduleTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        tablePanel.setBackground(new Color(245, 245, 245)); // Light gray

        tablePanel.add(scrollPane);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(new Color(245, 245, 245)); // Light gray

        editButton = new JButton("Edit Selected Schedule");
        editButton.setFont(new Font("Arial", Font.PLAIN, 16));
        editButton.setBackground(new Color(50, 205, 50)); // Lime green
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.addActionListener(this);

        markDoneButton = new JButton("Mark as Done");
        markDoneButton.setFont(new Font("Arial", Font.PLAIN, 16));
        markDoneButton.setBackground(new Color(255, 69, 0)); // Red
        markDoneButton.setForeground(Color.WHITE);
        markDoneButton.setFocusPainted(false);
        markDoneButton.addActionListener(this);

        backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.setBackground(new Color(70, 130, 180)); // Steel blue
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> goToDashboard());

        buttonPanel.add(editButton);
        buttonPanel.add(markDoneButton);
        buttonPanel.add(backButton);

        tablePanel.add(buttonPanel);

        f.add(tablePanel, BorderLayout.CENTER);

        loadGeneratedSchedules(); // Load schedule data

        f.setSize(1000, 600);
        f.setLocationRelativeTo(null);
        f.setResizable(true);
        f.revalidate();
        f.repaint();
        f.setVisible(true);
    }

    private void loadGeneratedSchedules() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String currentUserEmail = Main.currentemail;

            PreparedStatement ps = conn.prepareStatement(
                "SELECT course_name, date, topic, done FROM generated_schedule WHERE user_email = ?"
            );
            ps.setString(1, currentUserEmail);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String courseName = rs.getString("course_name");
                String date = rs.getString("date");
                String topic = rs.getString("topic");
                boolean done = rs.getBoolean("done");
                tableModel.addRow(new Object[]{courseName, date, topic, done ? "Yes" : "No"});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == editButton) {
            editSchedule();
        } else if (e.getSource() == markDoneButton) {
            markScheduleAsDone();
        }
    }

    private void editSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow != -1) {
            String courseName = tableModel.getValueAt(selectedRow, 0).toString();
            String date = tableModel.getValueAt(selectedRow, 1).toString();
            String topic = tableModel.getValueAt(selectedRow, 2).toString();

            JTextField courseNameField = new JTextField(courseName);
            JTextField dateField = new JTextField(date);
            JTextField topicField = new JTextField(topic);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Course Name:"));
            panel.add(courseNameField);
            panel.add(new JLabel("Date:"));
            panel.add(dateField);
            panel.add(new JLabel("Topic:"));
            panel.add(topicField);

            int result = JOptionPane.showConfirmDialog(f, panel, "Edit Schedule", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String newCourseName = courseNameField.getText();
                String newDate = dateField.getText();
                String newTopic = topicField.getText();

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "UPDATE generated_schedule SET course_name = ?, date = ?, topic = ? WHERE user_email = ? AND course_name = ? AND date = ? AND topic = ?";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setString(1, newCourseName);
                    ps.setString(2, newDate);
                    ps.setString(3, newTopic);
                    ps.setString(4, Main.currentemail);
                    ps.setString(5, courseName);
                    ps.setString(6, date);
                    ps.setString(7, topic);
                    int rowsUpdated = ps.executeUpdate();

                    if (rowsUpdated > 0) {
                        tableModel.setValueAt(newCourseName, selectedRow, 0);
                        tableModel.setValueAt(newDate, selectedRow, 1);
                        tableModel.setValueAt(newTopic, selectedRow, 2);
                        JOptionPane.showMessageDialog(f, "Schedule updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(f, "Schedule update failed!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(f, "Error updating schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(f, "Please select a schedule to edit.");
        }
    }

    private void markScheduleAsDone() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow != -1) {
            String courseName = tableModel.getValueAt(selectedRow, 0).toString();
            String date = tableModel.getValueAt(selectedRow, 1).toString();
            String topic = tableModel.getValueAt(selectedRow, 2).toString();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE generated_schedule SET done = ? WHERE user_email = ? AND course_name = ? AND date = ? AND topic = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setBoolean(1, true);
                ps.setString(2, Main.currentemail);
                ps.setString(3, courseName);
                ps.setString(4, date);
                ps.setString(5, topic);
                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated > 0) {
                    tableModel.setValueAt("Yes", selectedRow, 3);
                    deleteReminders(courseName, topic);
                    JOptionPane.showMessageDialog(f, "Schedule marked as done.");
                } else {
                    JOptionPane.showMessageDialog(f, "Failed to mark schedule as done!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(f, "Error marking schedule as done: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(f, "Please select a schedule to mark as done.");
        }
    }

    private void deleteReminders(String courseName, String topic) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM reminders WHERE course_name = ? AND topic = ? AND user_email = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, courseName);
            ps.setString(2, topic);
            ps.setString(3, Main.currentemail);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(f, "Error deleting reminders: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goToDashboard() {
        f.dispose();
        new DashboardPage();
    }
}