package com.library;

import java.util.ArrayList;
import java.sql.SQLException;
import java.util.Scanner;

import com.library.repository.MemberRepository;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;


import com.library.service.BorrowService;
import com.library.service.ReservationService;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;

import com.library.enums.BorrowStatus;
import com.library.enums.CopyStatus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;


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
                    memberMenu(scanner);
                    break;

                case 4:
                    borrowingMenu(scanner);
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
            System.out.println("5. Search books by title or author");
            System.out.println("6. Return to main menu");

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
                                int newPublicationYear = Integer.parseInt(newYearInput.trim());

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

                case 5: {
                    System.out.print("Enter title or author to search: ");
                    String keyword = scanner.nextLine().trim();

                    if (keyword.isEmpty()) {
                        System.out.println("Search keyword cannot be empty.");
                    } else {
                        try {
                            ArrayList<Book> matchingBooks = bookRepository.searchBooks(keyword);

                            System.out.println("\n===== SEARCH RESULTS =====");

                            if (matchingBooks.isEmpty()) {
                                System.out.println("No books found.");
                            } else {
                                for (Book book : matchingBooks) {
                                    System.out.println("--------------------");
                                    System.out.println("Book ID: " + book.getBookId());
                                    System.out.println("Title: " + book.getTitle());
                                    System.out.println("Author: " + book.getAuthor());
                                    System.out.println("Publication Year: "
                                            + book.getPublicationYear());
                                }

                                System.out.println("--------------------");
                                System.out.println("Total results: " + matchingBooks.size());
                            }

                        } catch (SQLException e) {
                            System.out.println("Database error: " + e.getMessage());
                        }
                    }
                    break;
                }
                case 6:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 6.");
            }

        } while (choice != 6);
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
            System.out.println("5. Search copies by book title or author");
            System.out.println("6. Return to main menu");

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
                        ArrayList<BookCopy> copies = copyRepository.findAllBookCopies();

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
                            ArrayList<BookCopy> copies = copyRepository.findCopiesByBookId(bookId);

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

                case 5: {
                    System.out.print("Enter book title or author to search: ");
                    String keyword = scanner.nextLine().trim();

                    if (keyword.isEmpty()) {
                        System.out.println("Search keyword cannot be empty.");
                    } else {
                        try {
                            ArrayList<BookCopy> matchingCopies = copyRepository.searchBookCopies(keyword);

                            System.out.println("\n===== SEARCH RESULTS =====");

                            if (matchingCopies.isEmpty()) {
                                System.out.println("No physical copies found.");
                            } else {
                                for (BookCopy copy : matchingCopies) {
                                    System.out.println("--------------------");
                                    System.out.println("Copy ID: " + copy.getCopyId());
                                    System.out.println("Book ID: " + copy.getBook().getBookId());
                                    System.out.println("Book Title: " + copy.getBook().getTitle());
                                    System.out.println("Author: " + copy.getBook().getAuthor());
                                    System.out.println("Status: " + copy.getStatus());
                                }

                                System.out.println("--------------------");
                                System.out.println("Total results: " + matchingCopies.size());
                            }

                        } catch (SQLException e) {
                            System.out.println("Database error: " + e.getMessage());
                        }
                    }
                    break;
                }
                case 6:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 6.");
            }

        } while (choice != 6);
    }

    private static void memberMenu(Scanner scanner) {

        int choice;

        MemberRepository memberRepository = new MemberRepository();

        do {
            System.out.println("\n===== MEMBER MANAGEMENT =====");
            System.out.println("1. Add a member");
            System.out.println("2. Find a member by ID");
            System.out.println("3. Display all members");
            System.out.println("4. Update a member");
            System.out.println("5. Delete a member");
            System.out.println("6. Search members by name, email, or phone");
            System.out.println("7. Return to main menu");

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
                        System.out.print("Enter full name: ");
                        String fullName = scanner.nextLine().trim();

                        System.out.print("Enter email: ");
                        String email = scanner.nextLine().trim();

                        System.out.print("Enter phone number: ");
                        String phoneNumber = scanner.nextLine().trim();

                        Member member = new Member(
                                fullName, email, phoneNumber, 1);

                        int memberId = memberRepository.addMember(member);

                        System.out.println("\nMember added successfully!");
                        System.out.println("Generated Member ID: " + memberId);

                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid member information: " + e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                    break;
                }
                case 2: {
                    try {
                        System.out.print("Enter member ID: ");
                        String idInput = scanner.nextLine();

                        int memberId = Integer.parseInt(idInput.trim());

                        Member member = memberRepository.findMemberById(memberId);

                        if (member == null) {
                            System.out.println("Member not found.");
                        } else {
                            System.out.println("\n===== MEMBER DETAILS =====");
                            System.out.println("Member ID: " + member.getMemberId());
                            System.out.println("Full Name: " + member.getFullName());
                            System.out.println("Email: " + member.getEmail());
                            System.out.println("Phone: " + member.getPhoneNumber());
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID. Please enter a number.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid member ID: " + e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                    break;
                }
                case 3: {
                    try {
                        ArrayList<Member> members = memberRepository.findAllMembers();

                        System.out.println("\n===== ALL MEMBERS =====");

                        if (members.isEmpty()) {
                            System.out.println("No members found.");
                        } else {
                            for (Member member : members) {
                                System.out.println("--------------------");
                                System.out.println("Member ID: " + member.getMemberId());
                                System.out.println("Full Name: " + member.getFullName());
                                System.out.println("Email: " + member.getEmail());
                                System.out.println("Phone: " + member.getPhoneNumber());
                            }
                        }

                        System.out.println("--------------------");
                        System.out.println("Total members: " + members.size());

                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                    break;
                }
                case 4: {
                    try {
                        System.out.print("Enter member ID to update: ");
                        String idInput = scanner.nextLine();

                        int memberId = Integer.parseInt(idInput.trim());

                        Member member = memberRepository.findMemberById(memberId);

                        if (member == null) {
                            System.out.println("Member not found.");
                        } else {
                            System.out.println("Member found: " + member.getFullName());

                            System.out.print("Enter new full name: ");
                            String newFullName = scanner.nextLine().trim();

                            System.out.print("Enter new email: ");
                            String newEmail = scanner.nextLine().trim();

                            System.out.print("Enter new phone number: ");
                            String newPhoneNumber = scanner.nextLine().trim();

                            Member updatedMember = new Member(
                                    newFullName,
                                    newEmail,
                                    newPhoneNumber,
                                    memberId);

                            memberRepository.updateMember(updatedMember);

                            System.out.println("\nMember updated successfully!");
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID. Please enter a number.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid member information: " + e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                    break;
                }
                case 5: {
                    try {
                        System.out.print("Enter member ID to delete: ");
                        String idInput = scanner.nextLine();

                        int memberId = Integer.parseInt(idInput.trim());

                        Member member = memberRepository.findMemberById(memberId);

                        if (member == null) {
                            System.out.println("Member not found.");
                        } else {
                            System.out.println("Member found: " + member.getFullName());

                            System.out.print("Are you sure you want to delete this member? (y/n): ");
                            String confirmation = scanner.nextLine().trim();

                            if (confirmation.equalsIgnoreCase("y")) {
                                memberRepository.deleteMember(memberId);
                                System.out.println("Member deleted successfully!");
                            } else {
                                System.out.println("Deletion cancelled.");
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID. Please enter a number.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid member ID: " + e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                    break;
                }
                case 6: {
                    System.out.print("Enter name, email, or phone to search: ");
                    String keyword = scanner.nextLine().trim();

                    if (keyword.isEmpty()) {
                        System.out.println("Search keyword cannot be empty.");
                    } else {
                        try {
                            ArrayList<Member> matchingMembers = memberRepository.searchMembers(keyword);

                            System.out.println("\n===== SEARCH RESULTS =====");

                            if (matchingMembers.isEmpty()) {
                                System.out.println("No members found.");
                            } else {
                                for (Member member : matchingMembers) {
                                    System.out.println("--------------------");
                                    System.out.println("Member ID: " + member.getMemberId());
                                    System.out.println("Full Name: " + member.getFullName());
                                    System.out.println("Email: " + member.getEmail());
                                    System.out.println("Phone: " + member.getPhoneNumber());
                                }

                                System.out.println("--------------------");
                                System.out.println("Total results: " + matchingMembers.size());
                            }

                        } catch (SQLException e) {
                            System.out.println("Database error: " + e.getMessage());
                        }
                    }
                    break;
                }

                case 7:
                    System.out.println("Returning to main menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 7.");
            }

        } while (choice != 7);
    }
    private static void borrowingMenu(Scanner scanner) {

        int choice;

        ReservationService reservationService =
                new ReservationService();

        BorrowService borrowService =
                new BorrowService(reservationService);
        MemberRepository memberRepository = 
        		new MemberRepository();
        BookCopyRepository copyRepository = 
        		new BookCopyRepository();
        BookRepository bookRepository = 
        		new BookRepository();

        do {
            System.out.println("\n===== BORROWING MANAGEMENT =====");
            System.out.println("1. Borrow a physical copy");
            System.out.println("2. Return a borrowed copy");
            System.out.println("3. Collect a reservation");
            System.out.println("4. Find a borrowing by ID");
            System.out.println("5. Display all borrowings");
            System.out.println("6. Display member borrowing history");
            System.out.println("7. Return to main menu");

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
                    System.out.print("Search member by name, email, or phone: ");
                    String keyword = scanner.nextLine().trim();

                    if (keyword.isEmpty()) {
                        System.out.println("Search keyword cannot be empty.");
                        break;
                    }

                    ArrayList<Member> matchingMembers =
                            memberRepository.searchMembers(keyword);

                    if (matchingMembers.isEmpty()) {
                        System.out.println("No members found.");
                        break;
                    }

                    System.out.println("\n===== MATCHING MEMBERS =====");

                    for (int i = 0; i < matchingMembers.size(); i++) {
                        Member member = matchingMembers.get(i);

                        System.out.println(
                                (i + 1) + ". " + member.getFullName()
                                + " | " + member.getEmail()
                                + " | " + member.getPhoneNumber()
                        );
                    }

                    System.out.print("\nSelect a member (enter result number): ");
                    String selectionInput = scanner.nextLine();

                    try {
                        int selectedIndex = Integer.parseInt(selectionInput.trim());

                        if (selectedIndex < 1 ||
                                selectedIndex > matchingMembers.size()) {
                            System.out.println("Invalid selection.");
                        } else {
                            Member selectedMember =
                                    matchingMembers.get(selectedIndex - 1);

                            System.out.println(
                                    "Selected member: " + selectedMember.getFullName());
                            System.out.print("\nSearch book by title or author: ");
                            String bookKeyword = scanner.nextLine().trim();

                            if (bookKeyword.isEmpty()) {
                                System.out.println("Search keyword cannot be empty.");
                            } else {
                                ArrayList<Book> matchingBooks =
                                        bookRepository.searchBooks(bookKeyword);

                                if (matchingBooks.isEmpty()) {
                                    System.out.println("No books found.");
                                } else {
                                    System.out.println("\n===== MATCHING BOOKS =====");

                                    for (int i = 0; i < matchingBooks.size(); i++) {
                                        Book book = matchingBooks.get(i);

                                        System.out.println(
                                                (i + 1) + ". " + book.getTitle()
                                                + " | " + book.getAuthor()
                                                + " | " + book.getPublicationYear()
                                        );
                                    }

                                    System.out.print("\nSelect a book (enter result number): ");
                                    String bookSelectionInput = scanner.nextLine();

                                    try {
                                        int bookIndex =
                                                Integer.parseInt(bookSelectionInput.trim());

                                        if (bookIndex < 1 || bookIndex > matchingBooks.size()) {
                                            System.out.println("Invalid selection.");
                                        } else {
                                            Book selectedBook =
                                                    matchingBooks.get(bookIndex - 1);

                                            System.out.println(
                                                    "Selected book: " + selectedBook.getTitle());
                                            ArrayList<BookCopy> bookCopies =
                                                    copyRepository.findCopiesByBookId(selectedBook.getBookId());

                                            ArrayList<BookCopy> availableCopies = new ArrayList<>();

                                            for (BookCopy copy : bookCopies) {
                                                if (copy.getStatus() == CopyStatus.AVAILABLE) {
                                                    availableCopies.add(copy);
                                                }
                                            }

                                            if (availableCopies.isEmpty()) {
                                                System.out.println("No available copies for this book.");
                                            } else {
                                                System.out.println("\n===== AVAILABLE COPIES =====");

                                                for (int i = 0; i < availableCopies.size(); i++) {
                                                    System.out.println((i + 1) + ". Available copy");
                                                }

                                                System.out.print("\nSelect a copy (enter result number): ");
                                                String copySelectionInput = scanner.nextLine();

                                                try {
                                                    int copyIndex = Integer.parseInt(copySelectionInput.trim());

                                                    if (copyIndex < 1 || copyIndex > availableCopies.size()) {
                                                        System.out.println("Invalid selection.");
                                                    } else {
                                                        BookCopy selectedCopy =
                                                                availableCopies.get(copyIndex - 1);

                                                        System.out.println("Available copy selected successfully.");
                                                        System.out.print("Enter expected return date (YYYY-MM-DD): ");
                                                        String returnDateInput = scanner.nextLine().trim();

                                                        try {
                                                            LocalDate expectedReturnDate = LocalDate.parse(returnDateInput);

                                                            Borrow borrow = borrowService.borrowBook(
                                                                    selectedMember,
                                                                    selectedCopy,
                                                                    expectedReturnDate
                                                            );

                                                            System.out.println("\nBorrowing created successfully!");
                                                            System.out.println("Generated Borrow ID: " + borrow.getBorrowId());

                                                        } catch (DateTimeParseException e) {
                                                            System.out.println("Invalid date. Please use YYYY-MM-DD.");
                                                        } catch (IllegalArgumentException | IllegalStateException e) {
                                                            System.out.println("Borrowing failed: " + e.getMessage());
                                                        }
                                                    }

                                                } catch (NumberFormatException e) {
                                                    System.out.println("Invalid selection. Enter a number.");
                                                }
                                            }
                                        }

                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid selection. Enter a number.");
                                    }
                                }
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid selection. Enter a number.");
                    }

                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
                break;
            }
            case 2: {
                try {
                    System.out.print("Search member by name, email, or phone: ");
                    String keyword = scanner.nextLine().trim();

                    if (keyword.isEmpty()) {
                        System.out.println("Search keyword cannot be empty.");
                        break;
                    }

                    ArrayList<Member> matchingMembers =
                            memberRepository.searchMembers(keyword);

                    if (matchingMembers.isEmpty()) {
                        System.out.println("No members found.");
                        break;
                    }

                    System.out.println("\n===== MATCHING MEMBERS =====");

                    for (int i = 0; i < matchingMembers.size(); i++) {
                        Member member = matchingMembers.get(i);

                        System.out.println(
                                (i + 1) + ". " + member.getFullName()
                                + " | " + member.getEmail()
                        );
                    }

                    System.out.print("\nSelect a member: ");
                    String selectionInput = scanner.nextLine();

                    try {
                        int selectedIndex =
                                Integer.parseInt(selectionInput.trim());

                        if (selectedIndex < 1 ||
                                selectedIndex > matchingMembers.size()) {

                            System.out.println("Invalid selection.");

                        } else {
                            Member selectedMember =
                                    matchingMembers.get(selectedIndex - 1);

                            ArrayList<Borrow> memberBorrowings =
                                    borrowService.findBorrowingsByMember(
                                            selectedMember.getMemberId());

                            ArrayList<Borrow> outstandingBorrowings =
                                    new ArrayList<>();

                            for (Borrow borrow : memberBorrowings) {
                                if (borrow.getStatus() != BorrowStatus.RETURNED) {
                                    outstandingBorrowings.add(borrow);
                                }
                            }

                            if (outstandingBorrowings.isEmpty()) {
                                System.out.println(
                                        "This member has no outstanding borrowings.");
                            } else {
                                System.out.println("\n===== OUTSTANDING BORROWINGS =====");

                                for (int i = 0; i < outstandingBorrowings.size(); i++) {
                                    Borrow borrow = outstandingBorrowings.get(i);

                                    System.out.println("--------------------");
                                    System.out.println((i + 1) + ".");
                                    System.out.println("Book: "
                                            + borrow.getBookCopy().getBook().getTitle());
                                    System.out.println("Copy ID: "
                                            + borrow.getBookCopy().getCopyId());
                                    System.out.println("Borrow Date: "
                                            + borrow.getBorrowDate());
                                    System.out.println("Expected Return Date: "
                                            + borrow.getExpectedReturnDate());
                                    System.out.println("Status: " + borrow.getStatus());
                                }

                                System.out.print("\nSelect a borrowing to return: ");
                                String borrowSelectionInput = scanner.nextLine();

                                try {
                                    int borrowIndex =
                                            Integer.parseInt(borrowSelectionInput.trim());

                                    if (borrowIndex < 1 ||
                                            borrowIndex > outstandingBorrowings.size()) {

                                        System.out.println("Invalid selection.");

                                    } else {
                                        Borrow selectedBorrow =
                                                outstandingBorrowings.get(borrowIndex - 1);

                                        System.out.print(
                                                "Confirm return? (y/n): ");

                                        String confirmation =
                                                scanner.nextLine().trim();

                                        if (confirmation.equalsIgnoreCase("y")) {
                                            borrowService.returnBook(selectedBorrow);

                                            System.out.println(
                                                    "\nBook returned successfully!");
                                        } else {
                                            System.out.println("Return cancelled.");
                                        }
                                    }

                                } catch (NumberFormatException e) {
                                    System.out.println(
                                            "Invalid selection. Enter a number.");
                                }
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid selection. Enter a number.");
                    }

                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Return failed: " + e.getMessage());
                }
                break;
            }    
            case 7:
                    System.out.println("Returning to main menu...");
                    break;

                default:
                    if (choice >= 1 && choice <= 6) {
                        System.out.println("Feature not implemented yet.");
                    } else {
                        System.out.println(
                                "Invalid choice. Enter a number from 1 to 7.");
                    }
            }

        } while (choice != 7);
    }
}