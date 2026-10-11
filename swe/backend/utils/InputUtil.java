package utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputUtil {
    
    public static int getIntInput(Scanner sc) {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                UIFormatter.printError("Invalid input! Please enter a valid number.");
                System.out.print("Try again: ");
            }
        }
    }

    public static String getStringInput(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            UIFormatter.printError("Input cannot be empty.");
        }
    }

    public static LocalDate getDateInput(Scanner sc, String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " (YYYY-MM-DD): ");
                return LocalDate.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e) {
                UIFormatter.printError("Invalid date format! Please use YYYY-MM-DD.");
            }
        }
    }

    public static LocalTime getTimeInput(Scanner sc, String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " (HH:MM): ");
                return LocalTime.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e) {
                UIFormatter.printError("Invalid time format! Please use HH:MM.");
            }
        }
    }
}
