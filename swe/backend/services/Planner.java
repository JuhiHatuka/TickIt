package services;

import models.*;
import utils.CalendarView;
import utils.UIFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Planner {
    private User user;
    private CalendarView calendarView;

    public Planner(User user) {
        this.user = user;
        this.calendarView = new CalendarView(this);
    }

    public void addTask(String title, String description, LocalDate dueDate, Priority priority) {
        user.getItems().add(new Task(title, description, dueDate, priority));
        UIFormatter.printSuccess("Task added!");
    }

    public void addEvent(String title, String description, LocalDate date, LocalTime start,
                         LocalTime end, String location) {
        if (hasTimeConflict(date, start, end)) {
            UIFormatter.printError("Time conflict! Event not added.");
            return;
        }
        user.getItems().add(new Event(title, description, date, start, end, location));
        UIFormatter.printSuccess("Event added!");
    }

    public void displayAll() {
        if (user.getItems().isEmpty()) {
            UIFormatter.printInfo("No tasks or events found!");
            return;
        }
        user.getItems().forEach(Plannable::displayDetails);
    }

    public void displayTasksOnly() {
        boolean found = false;
        for (Plannable p : user.getItems()) {
            if (p instanceof Task) {
                p.displayDetails();
                found = true;
            }
        }
        if (!found) UIFormatter.printInfo("No tasks in the record.");
    }

    public void displayEventsOnly() {
        boolean found = false;
        for (Plannable p : user.getItems()) {
            if (p instanceof Event) {
                p.displayDetails();
                found = true;
            }
        }
        if (!found) UIFormatter.printInfo("No events in the record.");
    }

    public void markTaskComplete(String title) {
        for (Plannable p : user.getItems()) {
            if (p instanceof Task && p.getTitle().equalsIgnoreCase(title)) {
                ((Task) p).markComplete();
                UIFormatter.printSuccess("Task completed!");
                return;
            }
        }
        UIFormatter.printError("Task not found.");
    }

    public void updateItem(String title, Scanner sc) {
        for (Plannable p : user.getItems()) {
            if (p.getTitle().equalsIgnoreCase(title)) {
                if (p instanceof Task) {
                    Task t = (Task) p;
                    System.out.print("New description: ");
                    String desc = sc.nextLine();
                    System.out.print("New deadline (YYYY-MM-DD): ");
                    LocalDate d = LocalDate.parse(sc.nextLine());
                    System.out.print("Priority (1=HIGH 2=MED 3=LOW): ");
                    int pr = Integer.parseInt(sc.nextLine());
                    Priority prio = (pr == 1) ? Priority.HIGH : (pr == 3) ? Priority.LOW : Priority.MEDIUM;
                    t.update(desc, d, prio);
                } else if (p instanceof Event) {
                    Event e = (Event) p;
                    System.out.print("New description: ");
                    String desc = sc.nextLine();
                    System.out.print("New date: ");
                    LocalDate date = LocalDate.parse(sc.nextLine());
                    System.out.print("New start time: ");
                    LocalTime st = LocalTime.parse(sc.nextLine());
                    System.out.print("New end time: ");
                    LocalTime et = LocalTime.parse(sc.nextLine());
                    System.out.print("New location: ");
                    String loc = sc.nextLine();
                    e.update(desc, date, st, et, loc);
                }
                UIFormatter.printSuccess("Successful Update!");
                return;
            }
        }
        UIFormatter.printError("Item not found.");
    }

    public void deleteItem(String title) {
        Iterator<Plannable> itr = user.getItems().iterator();
        while (itr.hasNext()) {
            Plannable p = itr.next();
            if (p.getTitle().equalsIgnoreCase(title)) {
                itr.remove();
                UIFormatter.printSuccess("Deleted!");
                return;
            }
        }
        UIFormatter.printError("Item not found.");
    }

    public void searchTasks(String keyword) {
        UIFormatter.printHeader("Search Results for: " + keyword);
        boolean found = false;
        for (Plannable p : user.getItems()) {
            if (p.containsKeyword(keyword)) {
                p.displayDetails();
                found = true;
            }
        }
        if (!found) UIFormatter.printInfo("No matching tasks or events found.");
    }

    public void displayTasksByStatus(boolean completed) {
        UIFormatter.printHeader(completed ? "Completed Tasks" : "Pending Tasks");
        boolean found = false;
        for (Plannable p : user.getItems()) {
            if (p instanceof Task && ((Task) p).isCompleted() == completed) {
                p.displayDetails();
                found = true;
            }
        }
        if (!found) UIFormatter.printInfo("No " + (completed ? "completed" : "pending") + " tasks found.");
    }

    public void displayTasksSortedByDeadline() {
        UIFormatter.printHeader("Tasks Sorted by Deadline");
        List<Task> tasks = user.getItems().stream()
                .filter(p -> p instanceof Task)
                .map(p -> (Task) p)
                .sorted(Comparator.comparing(Task::getDueDate))
                .toList();

        if (tasks.isEmpty()) {
            UIFormatter.printInfo("No tasks found.");
            return;
        }
        tasks.forEach(Task::displayDetails);
    }

    public void searchByDate(LocalDate date) {
        boolean found = false;
        for (Plannable p : user.getItems()) {
            if (p instanceof Task && ((Task) p).getDueDate().equals(date)) {
                UIFormatter.printInfo("Yes, you have a task scheduled ;-)");
                p.displayDetails();
                found = true;
            } else if (p instanceof Event && ((Event) p).getDate().equals(date)) {
                UIFormatter.printInfo("Yes, you have an event planned ;-)");
                p.displayDetails();
                found = true;
            }
        }
        if (!found) UIFormatter.printInfo("No items on this date.");
    }

    public void setReminder(String title, String timeStr) {
        LocalTime rt;
        String s = timeStr.trim();
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime ldt = LocalDateTime.parse(s, dtf);
            rt = ldt.toLocalTime();
        } catch (Exception e1) {
            try {
                rt = LocalTime.parse(s);
            } catch (Exception e2) {
                UIFormatter.printError("Invalid time! Use HH:MM or YYYY-MM-DD HH:MM");
                return;
            }
        }

        for (Plannable p : user.getItems()) {
            if (p.getTitle().equalsIgnoreCase(title)) {
                user.getReminders().add(new Reminder(p, rt));
                UIFormatter.printSuccess("Reminder added for: " + title + " at " + rt);
                return;
            }
        }
        UIFormatter.printError("Item not found.");
    }

    public void addSimpleReminder(String message, LocalTime time) {
        user.getSimpleReminders().add(new SimpleReminder(message, time));
        UIFormatter.printSuccess("Simple reminder added!");
    }

    public void processReminders() {
        user.getReminders().forEach(Reminder::checkAndNotify);
        user.getSimpleReminders().forEach(SimpleReminder::checkAndNotify);
    }

    public void displayTimeBasedReminders() {
        UIFormatter.printHeader("Reminders");
        if (user.getReminders().isEmpty() && user.getSimpleReminders().isEmpty()) {
            UIFormatter.printInfo("No reminders added yet.");
            return;
        }
        for (Reminder r : user.getReminders()) {
            System.out.println("• " + r.getItem().getTitle() + " | Time: " + r.getReminderTime() + " | " + (r.isNotified() ? "Notified" : "Pending"));
        }
        for (SimpleReminder sr : user.getSimpleReminders()) {
            System.out.println("• " + sr.getMessage() + " | Time: " + sr.getReminderTime() + " | " + (sr.isNotified() ? "Notified" : "Pending"));
        }
    }

    public boolean hasTimeConflict(LocalDate date, LocalTime start, LocalTime end) {
        for (Plannable p : user.getItems()) {
            if (p instanceof Event) {
                Event e = (Event) p;
                if (e.getDate().equals(date)) {
                    boolean conflict = (start.isAfter(e.getStartTime()) && start.isBefore(e.getEndTime())) ||
                            (end.isAfter(e.getStartTime()) && end.isBefore(e.getEndTime())) ||
                            start.equals(e.getStartTime()) ||
                            end.equals(e.getEndTime());
                    if (conflict) return true;
                }
            }
        }
        return false;
    }

    public boolean hasTaskOnDate(LocalDate date) {
        return user.getItems().stream()
                .anyMatch(p -> p instanceof Task && ((Task) p).getDueDate().equals(date));
    }

    public boolean hasEventOnDate(LocalDate date) {
        return user.getItems().stream()
                .anyMatch(p -> p instanceof Event && ((Event) p).getDate().equals(date));
    }

    public void showSummary() {
        UIFormatter.printHeader("PLANNER SUMMARY");
        if (user.getItems().isEmpty()) {
            UIFormatter.printInfo("No tasks/events added yet.");
            return;
        }
        for (Plannable p : user.getItems()) {
            if (p instanceof Task) System.out.println("[Task]  " + p.getTitle());
            else if (p instanceof Event) System.out.println("[Event] " + p.getTitle());
        }
        System.out.println("===========================\n");
    }

    public void showCalendar() {
        calendarView.displayCalendar();
    }

    public void viewTodaySchedule() {
        LocalDate today = LocalDate.now();
        List<Task> tasks = user.getItems().stream()
                .filter(i -> i instanceof Task).map(i -> (Task) i)
                .filter(t -> t.getDueDate().equals(today)).toList();
        List<Event> events = user.getItems().stream()
                .filter(i -> i instanceof Event).map(i -> (Event) i)
                .filter(e -> e.getDate().equals(today)).toList();

        UIFormatter.printHeader("TODAY'S SCHEDULE (" + today + ")");
        System.out.println("\nTasks:");
        if (tasks.isEmpty()) UIFormatter.printInfo("No tasks due today.");
        tasks.forEach(t -> System.out.println("• " + t.getTitle()));

        System.out.println("\nEvents:");
        if (events.isEmpty()) UIFormatter.printInfo("No events scheduled today.");
        events.forEach(e -> System.out.println("• " + e.getTitle()));
    }

    public void countItems() {
        long t = user.getItems().stream().filter(p -> p instanceof Task).count();
        long e = user.getItems().stream().filter(p -> p instanceof Event).count();
        System.out.println("Total Tasks: " + t);
        System.out.println("Total Events: " + e + "\n");
    }

    public void generateDailyReport() {
        LocalDate today = LocalDate.now();
        long completed = user.getItems().stream().filter(i -> i instanceof Task).map(i -> (Task) i)
                .filter(Task::isCompleted).filter(t -> t.getDueDate().equals(today)).count();
        long pending = user.getItems().stream().filter(i -> i instanceof Task).map(i -> (Task) i)
                .filter(t -> !t.isCompleted()).filter(t -> t.getDueDate().equals(today)).count();
        long events = user.getItems().stream().filter(i -> i instanceof Event).map(i -> (Event) i)
                .filter(e -> e.getDate().equals(today)).count();

        UIFormatter.printHeader("DAILY REPORT (" + today + ")");
        System.out.println("Completed tasks: " + completed);
        System.out.println("Pending tasks: " + pending);
        System.out.println("Scheduled events: " + events);

        if (pending == 0 && completed > 0)
            UIFormatter.printSuccess("Yesssssssssss! All tasks completed for today ab Rest karo");
        else if (pending > 0)
            UIFormatter.printInfo("You have " + pending + " tasks to complete. Keep going!");
    }
}
