package com.library.ui;

import java.sql.SQLException;
import java.util.Scanner;

import com.library.service.BorrowService;
import com.library.service.ReservationService;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ConsoleInput in = new ConsoleInput(scanner);
        Pickers pickers = new Pickers(in);

        // Shared services: they keep the in-memory cache, so every menu
        // must use the same instances
        ReservationService reservationService = new ReservationService();
        BorrowService borrowService = new BorrowService(reservationService);

        // Release copies held by reservations that were never collected
        try {
            int expired = reservationService.expireOverdueReservations();
            if (expired > 0) {
                System.out.println(expired
                        + " expired reservation(s) were released at startup.");
            }
        } catch (SQLException e) {
            System.out.println("Could not check expired reservations: "
                    + e.getMessage());
        }

        Menu bookMenu = new BookMenu(in, pickers);
        Menu copyMenu = new CopyMenu(in, pickers, reservationService);
        Menu memberMenu = new MemberMenu(in, pickers);
        Menu borrowingMenu = new BorrowingMenu(
                in, pickers, borrowService, reservationService);
        Menu reservationMenu = new ReservationMenu(
                in, pickers, reservationService, borrowService);
        Menu reportsMenu = new ReportsMenu(in, borrowService, reservationService);

        while (true) {
            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Books");
            System.out.println("2. Book copies");
            System.out.println("3. Members");
            System.out.println("4. Borrowings");
            System.out.println("5. Reservations");
            System.out.println("6. Reports");
            System.out.println("7. Exit");

            switch (in.readChoice("\nEnter your choice: ")) {
                case 1 -> bookMenu.run();
                case 2 -> copyMenu.run();
                case 3 -> memberMenu.run();
                case 4 -> borrowingMenu.run();
                case 5 -> reservationMenu.run();
                case 6 -> reportsMenu.run();
                case 7 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Enter 1 to 7.");
            }
        }
    }
}
