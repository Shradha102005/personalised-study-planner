import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.swing.*;

public class SchedulePage implements ActionListener {
    private JFrame frame;
    private JPanel cardPanel, formPanel, schedulePanel, resultPanel;
    private JTextField courseNameField, scheduleDateField, deadlineDateField, topicsField, reminderFrequencyField;
    private JButton generateScheduleButton, backButton, backToFormButton;

    public SchedulePage() {
        frame = Main.getInstance();
        frame.getContentPane().removeAll();
        Main.getInstance().dispose();
        frame.setTitle("Schedule Page");
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
    
        cardPanel = new JPanel(new CardLayout());
    
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(230, 240, 255)); 
        schedulePanel = new JPanel(new GridBagLayout());
        schedulePanel.setBackground(new Color(250, 250, 250)); 
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    
        JLabel courseNameLabel = createStyledLabel("Course Name:");
        courseNameField = new JTextField(20);
    
        JLabel scheduleDateLabel = createStyledLabel("Schedule Start Date (YYYY-MM-DD):");
        scheduleDateField = new JTextField(20);
    
        JLabel deadlineDateLabel = createStyledLabel("Deadline Date (YYYY-MM-DD):");
        deadlineDateField = new JTextField(20);
    
        JLabel topicsLabel = createStyledLabel("Topics (comma-separated):");
        topicsField = new JTextField(20);
    
        JLabel reminderFrequencyLabel = createStyledLabel("Reminder Frequency (hours, default is 1):");
        reminderFrequencyField = new JTextField(20);
        reminderFrequencyField.setText("1");
    
        generateScheduleButton = createStyledButton("Generate Schedule");
        generateScheduleButton.addActionListener(this);

        backToFormButton = createStyledButton("Back to Form");
        backToFormButton.addActionListener(this);
    
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(courseNameLabel, gbc);
    
        gbc.gridx = 1;
        formPanel.add(courseNameField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(scheduleDateLabel, gbc);
    
        gbc.gridx = 1;
        formPanel.add(scheduleDateField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(deadlineDateLabel, gbc);
    
        gbc.gridx = 1;
        formPanel.add(deadlineDateField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(topicsLabel, gbc);
    
        gbc.gridx = 1;
        formPanel.add(topicsField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(reminderFrequencyLabel, gbc);
    
        gbc.gridx = 1;
        formPanel.add(reminderFrequencyField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(generateScheduleButton, gbc);
        gbc.gridy = 6;
        formPanel.add(backToFormButton, gbc);
    
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Generated Schedule"));
        resultPanel.setBackground(new Color(255, 245, 230));
    
        GridBagConstraints spGbc = new GridBagConstraints();
        spGbc.insets = new Insets(10, 10, 10, 10);
        spGbc.gridx = 0;
        spGbc.gridy = 0;
        spGbc.anchor = GridBagConstraints.CENTER;
        schedulePanel.add(resultPanel, spGbc);
    
        backButton = createStyledButton("Back");
        backButton.addActionListener(e -> showPanel("Form"));
        spGbc.gridy = 1;
        spGbc.anchor = GridBagConstraints.NORTH;
        schedulePanel.add(backButton, spGbc);
    
        cardPanel.add(formPanel, "Form");
        cardPanel.add(schedulePanel, "Schedule");
    
        frame.add(cardPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
    

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(40, 40, 40)); 
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(0, 120, 215)); 
        button.setForeground(Color.WHITE); 
        button.setFocusPainted(false);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == generateScheduleButton) {
            String courseName = courseNameField.getText().trim();
            String scheduleDate = scheduleDateField.getText().trim();
            String deadlineDate = deadlineDateField.getText().trim();
            String topics = topicsField.getText().trim();
            String reminderFrequencyStr = reminderFrequencyField.getText().trim();
            int reminderFrequency = 1; 
            try {
                reminderFrequency = Integer.parseInt(reminderFrequencyStr);
                if (reminderFrequency <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid reminder frequency! Using default of 1 hour.", "Warning", JOptionPane.WARNING_MESSAGE);
                reminderFrequency = 1; 
            }
            if (courseName.isEmpty() || scheduleDate.isEmpty() || deadlineDate.isEmpty() || topics.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                LocalDate start = LocalDate.parse(scheduleDate);
                LocalDate deadline = LocalDate.parse(deadlineDate);
                long daysBetween = ChronoUnit.DAYS.between(start, deadline);
                if (daysBetween <= 0) {
                    JOptionPane.showMessageDialog(frame, "Deadline date must be after the start date!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String[] topicList = topics.split(",");
                if (topicList.length > daysBetween) {
                    JOptionPane.showMessageDialog(frame, "Too many topics for the given schedule duration!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                saveToDatabase(courseName, scheduleDate, deadlineDate, topics);
                saveGeneratedSchedule(courseName, start, topicList, Main.currentemail);
                createReminders(courseName, start, reminderFrequency);
                displaySchedule(start, topicList);
                showPanel("Schedule");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid date format or other error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        if(e.getSource() == backToFormButton)
        {
            frame.dispose();
            new DashboardPage();
        }
    }

    private void saveToDatabase(String courseName, String scheduleDate, String deadlineDate, String topics) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO schedules (user_email, course_name, start_date, end_date, topics) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            String userEmail = Main.currentemail;
            ps.setString(1, userEmail);
            ps.setString(2, courseName);
            ps.setString(3, scheduleDate);
            ps.setString(4, deadlineDate);
            ps.setString(5, topics);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Schedule saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error saving schedule to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveGeneratedSchedule(String courseName, LocalDate startDate, String[] topics, String userEmail) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO generated_schedule (user_email, course_name, date, topic) VALUES (?, ?, ?, ?)")) {
    
            LocalDate currentDate = startDate;
            int daysBetween = (int) ChronoUnit.DAYS.between(startDate, startDate.plusDays(topics.length));

            int topicsPerDay = topics.length / daysBetween;
            int remainingTopics = topics.length % daysBetween;
    
            int topicIndex = 0;

            for (int day = 0; day < daysBetween; day++) {
                int topicsForToday = topicsPerDay;
                if (remainingTopics > 0) {
                    topicsForToday++;
                    remainingTopics--;
                }

                for (int i = 0; i < topicsForToday; i++) {
                    if (topicIndex < topics.length) {
                        ps.setString(1, userEmail);
                        ps.setString(2, courseName);
                        ps.setDate(3, Date.valueOf(currentDate));
                        ps.setString(4, topics[topicIndex].trim());
                        ps.addBatch();
                        topicIndex++;
                    }
                }
                currentDate = currentDate.plusDays(1);
            }
            ps.executeBatch();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error saving generated schedule to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private void createReminders(String courseName, LocalDate startDate, int reminderFrequency) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT date, topic FROM generated_schedule WHERE course_name = ? AND user_email = ? ORDER BY date";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, courseName);
            ps.setString(2, Main.currentemail);
            ResultSet rs = ps.executeQuery();
    
            while (rs.next()) {
                LocalDate scheduledDate = rs.getDate("date").toLocalDate();
                String topic = rs.getString("topic");
                LocalDateTime reminderTime = scheduledDate.atTime(9, 0);
                LocalDateTime endOfDay = scheduledDate.plusDays(1).atTime(0, 0); 
                while (reminderTime.isBefore(endOfDay)) {
                    try (PreparedStatement insertPs = conn.prepareStatement(
                            "INSERT INTO reminders (user_email, course_name, topic, reminder_time) VALUES (?, ?, ?, ?)")) {
                        insertPs.setString(1, Main.currentemail);
                        insertPs.setString(2, courseName);
                        insertPs.setString(3, topic);
                        insertPs.setTimestamp(4, Timestamp.valueOf(reminderTime));
                        insertPs.executeUpdate();
                    }
    
                    reminderTime = reminderTime.plusHours(reminderFrequency); 
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error creating reminders: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displaySchedule(LocalDate startDate, String[] topics) {
        resultPanel.removeAll();
        resultPanel.setBackground(new Color(255, 245, 230));

        LocalDate currentDate = startDate;

        for (String topic : topics) {
            JLabel scheduleLabel = new JLabel(currentDate + ": " + topic);
            scheduleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            scheduleLabel.setForeground(new Color(80, 40, 0));
            resultPanel.add(scheduleLabel);
            currentDate = currentDate.plusDays(1);
        }

        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private void showPanel(String panelName) {
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, panelName);
    }
}
