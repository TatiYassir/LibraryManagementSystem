package com.library.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Reads and validates console input.
 * Invalid input throws IllegalArgumentException with a user-friendly message,
 * which the menus catch in one place.
 */
public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    // Prints a prompt and returns the trimmed line (may be empty)
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Same as readLine, but rejects empty input
    public String readNonEmpty(String prompt) {
        String value = readLine(prompt);

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty.");
        }

        return value;
    }

    // Reads a whole number
    public int readInt(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid number. Please enter a whole number.");
        }
    }

    // Reads a whole number, or returns null when the user just presses Enter
    public Integer readOptionalInt(String prompt) {
        String value = readLine(prompt);

        if (value.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid number. Please enter a whole number.");
        }
    }

    // Reads a menu choice; returns -1 when the input is not a number
    public int readChoice(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Reads a 1-based result number and returns the matching 0-based index
    public int readIndex(String prompt, int size) {
        int number = readInt(prompt);

        if (number < 1 || number > size) {
            throw new IllegalArgumentException("Invalid selection.");
        }

        return number - 1;
    }

    // Reads a date in YYYY-MM-DD format
    public LocalDate readDate(String prompt) {
        try {
            return LocalDate.parse(readLine(prompt));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date. Please use YYYY-MM-DD.");
        }
    }

    // Asks a yes/no question; only "y" or "Y" means yes
    public boolean confirm(String prompt) {
        return readLine(prompt).equalsIgnoreCase("y");
    }
}