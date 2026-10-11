package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event extends Plannable {
    private LocalDate date;
    private LocalTime startTime, endTime;
    private String location;
    private boolean completed;

    public Event(String title, String description, LocalDate date, LocalTime startTime,
            LocalTime endTime, String location) {
        super(title, description);
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.completed = false;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markComplete() {
        this.completed = true;
    }

    public void update(String newDescription, LocalDate newDate, LocalTime newStart, LocalTime newEnd,
            String newLocation) {
        this.description = newDescription;
        this.date = newDate;
        this.startTime = newStart;
        this.endTime = newEnd;
        this.location = newLocation;
    }

    @Override
    public void displayDetails() {
        System.out.println("\nEVENT: " + title);
        System.out.println("Description: " + description);
        System.out.println("Date: " + date);
        System.out.println("Time: " + startTime + " - " + endTime);
        System.out.println("Location: " + location);
        System.out.println("Status: " + (completed ? "Completed :)" : "Scheduled"));
        System.out.println();
    }

    @Override
    public String toString() {
        return title + " (Date: " + date + ", Time: " + startTime + " - " + endTime + ", Loc: " + location + ")";
    }
}
