import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReminderNotifierApp {
    private static final Logger LOGGER = Logger.getLogger(ReminderNotifierApp.class.getName());
    private ScheduledExecutorService scheduler;
    private TrayIcon trayIcon;
    private SystemTray tray;
    private Set<String> notifiedReminders = new HashSet<>();

    public void startReminderNotifications() {
        if (!SystemTray.isSupported()) {
            LOGGER.severe("System tray is not supported!");
            return;
        }

        String username = Main.currentemail;
        if (username == null || username.isEmpty()) {
            LOGGER.severe("No user is currently logged in!");
            return;
        }

        try {
            String iconPath = "C:/path/to/reminder_icon.png"; 
            Image icon = Toolkit.getDefaultToolkit().getImage(iconPath);
            if (icon == null) {
                LOGGER.severe("Icon not found at path: " + iconPath);
                return;
            }

            tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(icon, "Reminder Notifications");
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);
            LOGGER.info("Tray icon added successfully");

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    checkAndNotify(username);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in reminder check", e);
                }
            }, 0, 1, TimeUnit.MINUTES);
            LOGGER.info("Scheduled task started successfully");

        } catch (AWTException e) {
            LOGGER.log(Level.SEVERE, "Unable to add to system tray", e);
        }
    }

    private void checkAndNotify(String username) {
        LOGGER.info("Checking reminders for user: " + username);
        List<String> reminders = fetchReminders(username);
        if (reminders.isEmpty()) {
            LOGGER.info("No reminders found for user: " + username);
        } else {
            for (String reminder : reminders) {
                if (!notifiedReminders.contains(reminder)) {
                    trayIcon.displayMessage("Reminder", reminder, TrayIcon.MessageType.INFO);
                    LOGGER.info("Displayed reminder: " + reminder);
                    notifiedReminders.add(reminder);
                } else {
                    LOGGER.info("Reminder already notified: " + reminder);
                }
            }
        }
    }

    private List<String> fetchReminders(String username) {
        List<String> reminders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Fetch reminders for courses that are not done
            String query = "SELECT r.course_name, r.topic, r.reminder_time FROM reminders r " +
                           "JOIN courses c ON r.course_name = c.course_name " +
                           "WHERE r.user_email = ? AND c.status = 'not done' AND DATE(r.reminder_time) = CURDATE() AND TIME(r.reminder_time) <= CURTIME()";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String courseName = rs.getString("course_name");
                String topic = rs.getString("topic");
                Timestamp reminderTime = rs.getTimestamp("reminder_time");
                String reminder = "Course: " + courseName + ", Topic: " + topic + ", Time: " + reminderTime;
                reminders.add(reminder);
                LOGGER.info("Fetched reminder: " + reminder);
            }

            // Delete reminders for courses that are done
            String deleteQuery = "DELETE r FROM reminders r " +
                                 "JOIN courses c ON r.course_name = c.course_name " +
                                 "WHERE r.user_email = ? AND c.status = 'done' AND DATE(r.reminder_time) = CURDATE()";
            PreparedStatement deletePs = conn.prepareStatement(deleteQuery);
            deletePs.setString(1, username);
            int deletedRows = deletePs.executeUpdate();
            if (deletedRows > 0) {
                LOGGER.info("Deleted " + deletedRows + " reminders for completed courses.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching reminders", e);
        }
        return reminders;
    }

    public void stopReminderNotifications() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            LOGGER.info("Scheduled task stopped successfully");
        }
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
            LOGGER.info("Tray icon removed successfully");
        }
    }

    // Method to clear notified reminders, can be called after modifying reminders
    public void clearNotifiedReminders() {
        notifiedReminders.clear();
        LOGGER.info("Cleared notified reminders");
    }
}