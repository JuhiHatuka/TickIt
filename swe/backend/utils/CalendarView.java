package utils;

import services.Planner;
import java.time.LocalDate;
import java.util.Scanner;

public class CalendarView {
    private Planner planner;

    public CalendarView(Planner planner) {
        this.planner = planner;
    }

    public void displayCalendar() {
        final String RESET = "\u001B[0m";
        final String YELLOW = "\u001B[33m";
        final String CYAN = "\u001B[36m";
        final String MAGENTA = "\u001B[35m";

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Enter year: ");
        int year = Integer.parseInt(sc.nextLine().trim());

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();
        int dayOfWeek = firstDay.getDayOfWeek().getValue();

        System.out.println("\n" + firstDay.getMonth() + " " + year);
        System.out.println("Mon Tue Wed Thu Fri Sat Sun");

        int count = 0;

        for (int i = 1; i < dayOfWeek; i++) {
            System.out.print("    ");
            count++;
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate curr = LocalDate.of(year, month, day);
            String ds = String.format("%3d", day);

            boolean hasT = (planner != null) && planner.hasTaskOnDate(curr);
            boolean hasE = (planner != null) && planner.hasEventOnDate(curr);

            if (hasT && hasE) {
                ds = MAGENTA + ds + "*" + RESET;
            } else if (hasT) {
                ds = YELLOW + ds + "t" + RESET;
            } else if (hasE) {
                ds = CYAN + ds + "e" + RESET;
            }

            System.out.print(ds + " ");
            count++;

            if (count % 7 == 0) {
                System.out.println();
            }
        }

        System.out.println("\n" + MAGENTA + "* = Tasks & Events" + RESET + ", "
                + YELLOW + "t = Tasks" + RESET + ", "
                + CYAN + "e = Events" + RESET);
    }
}
