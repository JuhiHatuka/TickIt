package models;

import java.io.Serializable;
import java.time.LocalTime;

public class SimpleReminder implements Serializable {
    private String message;
    private LocalTime reminderTime;
    private boolean notified = false;

    public SimpleReminder(String message, LocalTime reminderTime) {
        this.message = message;
        this.reminderTime = reminderTime;
    }

    public void checkAndNotify() {
        if (!notified && LocalTime.now().isAfter(reminderTime)) {
            System.out.println("REMINDER: " + message);
            notified = true;
        }
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public String getMessage() {
        return message;
    }

    public boolean isNotified() {
        return notified;
    }
}
