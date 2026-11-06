package com.library.ui;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Base class for every console menu.
 * It prints the menu, reads the choice and handles errors in ONE place,
 * so subclasses only describe their options and what each option does.
 * The "Return to main menu" option is added automatically as the last number.
 */
public abstract class Menu {

    protected final ConsoleInput in;

    protected Menu(ConsoleInput in) {
        this.in = in;
    }

    // Title shown at the top of the menu
    protected abstract String title();

    // Option labels, in order (numbered from 1)
    protected abstract String[] options();

    // Runs the action for a valid option number
    protected abstract void handle(int choice) throws SQLException;

    public void run() {

        String[] options = options();
        int exitOption = options.length + 1;

        while (true) {

            System.out.println("\n===== " + title() + " =====");

            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ". " + options[i]);
            }

            System.out.println(exitOption + ". Return to main menu");

            int choice = in.readChoice("\nEnter your choice: ");

            if (choice == exitOption) {
                System.out.println("Returning to main menu...");
                return;
            }

            if (choice < 1 || choice > options.length) {
                System.out.println("Invalid choice. Enter a number from 1 to "
                        + exitOption + ".");
                continue;
            }

            try {
                handle(choice);
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Error: this record is still used by other "
                        + "records (copies, borrowings or reservations), "
                        + "so the operation was refused.");
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Prints a list of items separated by lines, followed by a total
    protected void printList(String header, List<?> items,
            String emptyMessage, String totalLabel) {

        System.out.println("\n===== " + header + " =====");

        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }

        for (Object item : items) {
            System.out.println("--------------------");
            System.out.println(item);
        }

        System.out.println("--------------------");
        System.out.println(totalLabel + ": " + items.size());
    }

    // Shows a numbered list and lets the user pick one item.
    // Returns null (after printing emptyMessage) when the list is empty.
    protected <T> T choose(String header, List<T> items,
            String emptyMessage, String prompt) {

        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return null;
        }

        System.out.println("\n===== " + header + " =====");

        for (int i = 0; i < items.size(); i++) {
            System.out.println("--- " + (i + 1) + " ---");
            System.out.println(items.get(i));
        }

        return items.get(in.readIndex(prompt, items.size()));
    }
}
