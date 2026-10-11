import models.*;
import services.AuthService;
import services.Planner;
import utils.InputUtil;
import utils.UIFormatter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AuthService authService = new AuthService();
        
        System.out.println(UIFormatter.CYAN + UIFormatter.BOLD + "\n************ Welcome to TickIt Planner ************" + UIFormatter.RESET);

        while (true) {
            UIFormatter.printHeader("Auth Menu");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = InputUtil.getIntInput(sc);

            if (choice == 1) {
                authService.register(sc);
            } else if (choice == 2) {
                User user = authService.login(sc);
                if (user != null) {
                    runPlannerApp(user, sc, authService);
                }
            } else if (choice == 3) {
                UIFormatter.printSuccess("Goodbye!");
                break;
            } else {
                UIFormatter.printError("Invalid choice.");
            }
        }
        sc.close();
    }
    
    private static void runPlannerApp(User user, Scanner sc, AuthService authService) {
        Planner planner = new Planner(user);
        boolean exit = false;
        
        while (!exit) {
            boolean taskExit = false, eventExit = false;
            UIFormatter.printHeader("Main Menu - " + user.getUsername());
            System.out.println("1. Task Management");
            System.out.println("2. Event Management");
            System.out.println("3. View All Items");
            System.out.println("4. Search & Filter");
            System.out.println("5. Reminders");
            System.out.println("6. Reports & Views");
            System.out.println("7. Logout");
            System.out.print("Enter choice: ");
            int choice = InputUtil.getIntInput(sc);
            
            switch (choice) {
                case 1:
                    while(!taskExit) {
                        UIFormatter.printHeader("Task Management");
                        System.out.println("1. Add Task");
                        System.out.println("2. Update Task");
                        System.out.println("3. Delete Task");
                        System.out.println("4. Mark Complete");
                        System.out.println("5. View All Tasks");
                        System.out.println("6. Back to Main Menu");
                        System.out.print("Enter choice: ");
                        int tChoice = InputUtil.getIntInput(sc);
                        switch(tChoice) {
                            case 1:
                                String title = InputUtil.getStringInput(sc, "Title: ");
                                String desc = InputUtil.getStringInput(sc, "Description: ");
                                LocalDate date = InputUtil.getDateInput(sc, "Due Date");
                                System.out.println("Priority (1=HIGH, 2=MED, 3=LOW): ");
                                int pr = InputUtil.getIntInput(sc);
                                Priority p = pr == 1 ? Priority.HIGH : pr == 3 ? Priority.LOW : Priority.MEDIUM;
                                planner.addTask(title, desc, date, p);
                                break;
                            case 2:
                                planner.updateItem(InputUtil.getStringInput(sc, "Task title to update: "), sc);
                                break;
                            case 3:
                                planner.deleteItem(InputUtil.getStringInput(sc, "Task title to delete: "));
                                break;
                            case 4:
                                planner.markTaskComplete(InputUtil.getStringInput(sc, "Task title to complete: "));
                                break;
                            case 5:
                                planner.displayTasksOnly();
                                break;
                            case 6:
                                taskExit = true;
                                break;
                            default: UIFormatter.printError("Invalid option.");
                        }
                    }
                    break;
                case 2:
                    while(!eventExit) {
                        UIFormatter.printHeader("Event Management");
                        System.out.println("1. Add Event");
                        System.out.println("2. Update Event");
                        System.out.println("3. Delete Event");
                        System.out.println("4. View All Events");
                        System.out.println("5. Back to Main Menu");
                        System.out.print("Enter choice: ");
                        int eChoice = InputUtil.getIntInput(sc);
                        switch(eChoice) {
                            case 1:
                                String title = InputUtil.getStringInput(sc, "Title: ");
                                String desc = InputUtil.getStringInput(sc, "Description: ");
                                LocalDate date = InputUtil.getDateInput(sc, "Date");
                                LocalTime st = InputUtil.getTimeInput(sc, "Start Time");
                                LocalTime et = InputUtil.getTimeInput(sc, "End Time");
                                String loc = InputUtil.getStringInput(sc, "Location: ");
                                planner.addEvent(title, desc, date, st, et, loc);
                                break;
                            case 2:
                                planner.updateItem(InputUtil.getStringInput(sc, "Event title to update: "), sc);
                                break;
                            case 3:
                                planner.deleteItem(InputUtil.getStringInput(sc, "Event title to delete: "));
                                break;
                            case 4:
                                planner.displayEventsOnly();
                                break;
                            case 5:
                                eventExit = true;
                                break;
                            default: UIFormatter.printError("Invalid option.");
                        }
                    }
                    break;
                case 3:
                    planner.displayAll();
                    break;
                case 4:
                    UIFormatter.printHeader("Search & Filter");
                    System.out.println("1. Search by Keyword");
                    System.out.println("2. Search by Date");
                    System.out.println("3. Tasks Sorted by Deadline");
                    System.out.println("4. View Completed Tasks");
                    System.out.println("5. View Pending Tasks");
                    System.out.print("Enter choice: ");
                    int fChoice = InputUtil.getIntInput(sc);
                    if (fChoice == 1) planner.searchTasks(InputUtil.getStringInput(sc, "Keyword: "));
                    else if (fChoice == 2) planner.searchByDate(InputUtil.getDateInput(sc, "Date"));
                    else if (fChoice == 3) planner.displayTasksSortedByDeadline();
                    else if (fChoice == 4) planner.displayTasksByStatus(true);
                    else if (fChoice == 5) planner.displayTasksByStatus(false);
                    else UIFormatter.printError("Invalid option.");
                    break;
                case 5:
                    UIFormatter.printHeader("Reminders");
                    System.out.println("1. Set Reminder for Item");
                    System.out.println("2. Add Simple Reminder");
                    System.out.println("3. View Reminders");
                    System.out.print("Enter choice: ");
                    int rChoice = InputUtil.getIntInput(sc);
                    if (rChoice == 1) planner.setReminder(InputUtil.getStringInput(sc, "Item Title: "), InputUtil.getStringInput(sc, "Time (YYYY-MM-DD HH:MM or HH:MM): "));
                    else if (rChoice == 2) planner.addSimpleReminder(InputUtil.getStringInput(sc, "Message: "), InputUtil.getTimeInput(sc, "Time"));
                    else if (rChoice == 3) planner.displayTimeBasedReminders();
                    else UIFormatter.printError("Invalid option.");
                    break;
                case 6:
                    UIFormatter.printHeader("Reports & Views");
                    System.out.println("1. View Calendar");
                    System.out.println("2. Show Summary");
                    System.out.println("3. Count Items");
                    System.out.println("4. View Today's Schedule");
                    System.out.println("5. Generate Daily Report");
                    System.out.print("Enter choice: ");
                    int vChoice = InputUtil.getIntInput(sc);
                    if (vChoice == 1) planner.showCalendar();
                    else if (vChoice == 2) planner.showSummary();
                    else if (vChoice == 3) planner.countItems();
                    else if (vChoice == 4) planner.viewTodaySchedule();
                    else if (vChoice == 5) planner.generateDailyReport();
                    else UIFormatter.printError("Invalid option.");
                    break;
                case 7:
                    authService.saveUsers(); // Save data explicitly on logout
                    UIFormatter.printSuccess("Logged out successfully.");
                    exit = true;
                    break;
                default:
                    UIFormatter.printError("Invalid choice! Try again.");
            }
            if (!exit) {
                planner.processReminders();
                authService.saveUsers(); // Auto save after every action loop iteration
            }
        }
    }
}
