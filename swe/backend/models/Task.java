package models;

import java.time.LocalDate;

public class Task extends Plannable {
    private LocalDate dueDate;
    private Priority priority;
    private boolean completed;

    public Task(String title, String description, LocalDate dueDate, Priority priority) {
        super(title, description);
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
    }

    public void markComplete() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void update(String newDescription, LocalDate newDueDate, Priority newPriority) {
        this.description = newDescription;
        this.dueDate = newDueDate;
        this.priority = newPriority;
    }

    @Override
    public void displayDetails() {
        System.out.println("\nTASK: " + title);
        System.out.println("Description: " + description);
        System.out.println("Due Date: " + dueDate);
        System.out.println("Priority: " + priority);
        System.out.println("Status: " + (completed ? "Completed" : "Pending"));
        System.out.println();
    }

    @Override
    public String toString() {
        return title + " (Due: " + dueDate + ", Priority: " + priority + ", Status: "
                + (completed ? "TASK DONE :)" : "Scheduled") + ")";
    }
}
