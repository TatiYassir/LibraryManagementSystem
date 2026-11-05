package com.library;

import java.util.Scanner;
import com.library.repository.BookRepository;
import com.library.model.Book;
import java.util.ArrayList;
import java.sql.SQLException;
import com.library.repository.BookCopyRepository;
import com.library.enums.CopyStatus;
import com.library.model.BookCopy;
public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        int choice;

        do {

            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Book Management");
            System.out.println("2. Physical Copy Management");
            System.out.println("3. Member Management");
            System.out.println("4. Borrowing Management");
            System.out.println("5. Reservation Management");
            System.out.println("6. History and Reports");
            System.out.println("0. Exit");

            System.out.print("\nEnter your choice: ");

            String input = scanner.nextLine();

            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {

            	case 1:
            		bookMenu(scanner);
            		break;

            	case 2:
            	    physicalCopyMenu(scanner);
            	    break;

                case 3:
                    System.out.println("Member Management selected.");
                    break;

                case 4:
                    System.out.println("Borrowing Management selected.");
                    break;

                case 5:
                    System.out.println("Reservation Management selected.");
                    break;

                case 6:
                    System.out.println("History and Reports selected.");
                    break;

                case 0:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice. Enter a number from 0 to 6.");
                    break;
            }

        } while (choice != 0);

        scanner.close();
    }
    private static void bookMenu(Scanner scanner) {

        int choice;

        BookRepository bookRepository = new BookRepository();

        do {

            System.out.println("\n===== BOOK MANAGEMENT =====");
            System.out.println("1. Add a book");
            System.out.println("2. Find a book by ID");
            System.out.println("3. Display all books");
            System.out.println("4. Update a book");
            System.out.println("5. Return to main menu");

            System.out.print("\nEnter your choice: ");

            String input = scanner.nextLine();

            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {

            	case 1:
            		System.out.print("Enter book title: ");
            		String title = scanner.nextLine();

            		System.out.print("Enter author name: ");
            		String author = scanner.nextLine();
            		System.out.print("Enter publication year: ");
            		String yearInput = scanner.nextLine();

            		try {
            		    int publicationYear = Integer.parseInt(yearInput.trim());
            		    int bookId = bookRepository.addBook(title, author, publicationYear);

            		    System.out.println("\nBook added successfully!");
            		    System.out.println("Generated Book ID: " + bookId);
            		} catch (NumberFormatException e) {
            		    System.out.println("Invalid year. Please enter a number.");
            		} catch (SQLException e) {
            		    System.out.println("Database error: " + e.getMessage());
            		}
            		break;

                case 2:
                    try {
                        System.out.print("Enter book ID: ");

                        String bookIdInput = scanner.nextLine();

                        int bookId = Integer.parseInt(bookIdInput.trim());

                        Book book = bookRepository.findBookById(bookId);

                        if (book == null) {
                            System.out.println("Book not found.");
                        } else {
                            System.out.println("\n===== BOOK DETAILS =====");
                            System.out.println("Book ID: " + book.getBookId());
                            System.out.println("Title: " + book.getTitle());
                            System.out.println("Author: " + book.getAuthor());
                            System.out.println("Publication Year: "
                                    + book.getPublicationYear());
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID. Please enter a number.");
                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                    break;

                case 3:
                    try {
                        ArrayList<Book> books = bookRepository.findAllBooks();

                        System.out.println("\n===== ALL BOOKS =====");

                        if (books.isEmpty()) {
                            System.out.println("No books found.");
                        } else {
                            for (Book book : books) {
                                System.out.println("--------------------");
                                System.out.println("Book ID: " + book.getBookId());
                                System.out.println("Title: " + book.getTitle());
                                System.out.println("Author: " + book.getAuthor());
                                System.out.println("Publication Year: " + book.getPublicationYear());
                            }
                        }

                        System.out.println("--------------------");
                        System.out.println("Total books: " + books.size());

                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                    break;

                case 4:
                    try {
                        System.out.print("Enter book ID to update: ");
                        String idInput = scanner.nextLine();

                        int bookId = Integer.parseInt(idInput.trim());

                        Book book = bookRepository.findBookById(bookId);

                        if (book == null) {
                            System.out.println("Book not found.");
                        } else {
                            System.out.println("Book found: " + book.getTitle());

                            System.out.print("Enter new title: ");
                            String newTitle = scanner.nextLine();

                            System.out.print("Enter new author: ");
                            String newAuthor = scanner.nextLine();

                            System.out.print("Enter new publication year: ");
                            String newYearInput = scanner.nextLine();

                            try {
                                int newPublicationYear =
                                        Integer.parseInt(newYearInput.trim());

                                book.setTitle(newTitle);
                                book.setAuthor(newAuthor);
                                book.setPublicationYear(newPublicationYear);

                                bookRepository.updateBook(book);

                                System.out.println("\nBook updated successfully!");

                            } catch (NumberFormatException e) {
                                System.out.println("Invalid year. Please enter a number.");
                            } catch (IllegalArgumentException e) {
                                System.out.println("Invalid book information: " + e.getMessage());
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID. Please enter a number.");
                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("Returning to main menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 5.");
            }

        } while (choice != 5);
    }
    private static void physicalCopyMenu(Scanner scanner) {

        int choice;

        BookCopyRepository copyRepository = new BookCopyRepository();
        BookRepository bookRepository = new BookRepository();

        do {
            System.out.println("\n===== PHYSICAL COPY MANAGEMENT =====");
            System.out.println("1. Add a physical copy");
            System.out.println("2. Find a copy by ID");
            System.out.println("3. Display all copies");
            System.out.println("4. Display copies by book ID");
            System.out.println("5. Return to main menu");

            System.out.print("\nEnter your choice: ");

            String input = scanner.nextLine();

            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
            case 1: {
                try {
                    System.out.print("Enter book ID: ");
                    String bookIdInput = scanner.nextLine();

                    int bookId = Integer.parseInt(bookIdInput.trim());

                    Book book = bookRepository.findBookById(bookId);

                    if (book == null) {
                        System.out.println("Book not found.");
                    } else {
                        int copyId = copyRepository.addBookCopy(
                                book, CopyStatus.AVAILABLE);

                        System.out.println("\nPhysical copy added successfully!");
                        System.out.println("Generated Copy ID: " + copyId);
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID. Please enter a number.");
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
                break;
            }

            case 2: {
                try {
                    System.out.print("Enter copy ID: ");
                    String copyIdInput = scanner.nextLine();

                    int copyId = Integer.parseInt(copyIdInput.trim());

                    BookCopy copy = copyRepository.findBookCopyById(copyId);

                    if (copy == null) {
                        System.out.println("Physical copy not found.");
                    } else {
                        System.out.println("\n===== PHYSICAL COPY DETAILS =====");
                        System.out.println("Copy ID: " + copy.getCopyId());
                        System.out.println("Book ID: " + copy.getBook().getBookId());
                        System.out.println("Book Title: " + copy.getBook().getTitle());
                        System.out.println("Status: " + copy.getStatus());
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID. Please enter a number.");
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
                break;
            }

            case 3: {
                try {
                    ArrayList<BookCopy> copies =
                            copyRepository.findAllBookCopies();

                    System.out.println("\n===== ALL PHYSICAL COPIES =====");

                    if (copies.isEmpty()) {
                        System.out.println("No physical copies found.");
                    } else {
                        for (BookCopy copy : copies) {
                            System.out.println("--------------------");
                            System.out.println("Copy ID: " + copy.getCopyId());
                            System.out.println("Book ID: " + copy.getBook().getBookId());
                            System.out.println("Book Title: " + copy.getBook().getTitle());
                            System.out.println("Status: " + copy.getStatus());
                        }
                    }

                    System.out.println("--------------------");
                    System.out.println("Total copies: " + copies.size());

                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
                break;
            }

            case 4: {
                try {
                    System.out.print("Enter book ID: ");
                    String bookIdInput = scanner.nextLine();

                    int bookId = Integer.parseInt(bookIdInput.trim());

                    Book book = bookRepository.findBookById(bookId);

                    if (book == null) {
                        System.out.println("Book not found.");
                    } else {
                        ArrayList<BookCopy> copies =
                                copyRepository.findCopiesByBookId(bookId);

                        System.out.println("\n===== PHYSICAL COPIES =====");
                        System.out.println("Book: " + book.getTitle());

                        if (copies.isEmpty()) {
                            System.out.println("No physical copies found.");
                        } else {
                            for (BookCopy copy : copies) {
                                System.out.println("--------------------");
                                System.out.println("Copy ID: " + copy.getCopyId());
                                System.out.println("Status: " + copy.getStatus());
                            }
                        }

                        System.out.println("--------------------");
                        System.out.println("Total copies: " + copies.size());
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID. Please enter a number.");
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
                break;
            }

                case 5:
                    System.out.println("Returning to main menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 5.");
            }

        } while (choice != 5);
    }
}