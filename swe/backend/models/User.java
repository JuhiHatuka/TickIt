package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String username;
    private String password;

    private List<Plannable> items = new ArrayList<>();
    private List<Reminder> reminders = new ArrayList<>();
    private List<SimpleReminder> simpleReminders = new ArrayList<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String pwd) {
        return this.password.equals(pwd);
    }

    public List<Plannable> getItems() {
        return items;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public List<SimpleReminder> getSimpleReminders() {
        return simpleReminders;
    }
}
