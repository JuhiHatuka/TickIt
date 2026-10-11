package models;

import java.io.Serializable;

public abstract class Plannable implements Serializable {
    protected String title;
    protected String description;

    public Plannable(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean containsKeyword(String keyword) {
        String lower = keyword.toLowerCase();
        return title.toLowerCase().contains(lower) || description.toLowerCase().contains(lower);
    }

    public abstract void displayDetails();
}
