package models;

import java.io.Serializable;
import java.time.LocalTime;

public class Reminder implements Serializable {
    private Plannable item;
    private LocalTime reminderTime;
    private boolean notified;

    public Reminder(Plannable item, LocalTime reminderTime) {
        this.item = item;
        this.reminderTime = reminderTime;
        this.notified = false;
    }

    public void checkAndNotify() {
        if (!notified && LocalTime.now().isAfter(reminderTime)) {
            System.out.println("REMINDER: " + item.getTitle() + " is coming up!");
            notified = true;
        }
    }

    public Plannable getItem() {
        return item;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public boolean isNotified() {
        return notified;
    }
}
