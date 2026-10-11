package services;

public class ReminderSystem implements ReminderInterface {
    public void setReminder(String time) {
        System.out.println("Reminder set for: " + time);
    }

    public void notifyUser() {
        System.out.println("Reminder: You have an upcoming task/event!");
    }
}
