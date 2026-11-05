package com.library.ui;

import java.sql.SQLException;

import com.library.model.Book;
import com.library.repository.BookRepository;

public class BookMenu extends Menu {

    private final BookRepository bookRepository = new BookRepository();
    private final Pickers pickers;

    public BookMenu(ConsoleInput in, Pickers pickers) {
        super(in);
        this.pickers = pickers;
    }

    @Override
    protected String title() {
        return "BOOK MENU";
    }

    @Override
    protected String[] options() {
        return new String[] {
            "Add a book", "Search books", "Show all books",
            "Update a book", "Delete a book"
        };
    }

    @Override
    protected void handle(int choice) throws SQLException {
        switch (choice) {
            case 1 -> addBook();
            case 2 -> searchBooks();
            case 3 -> showAllBooks();
            case 4 -> updateBook();
            case 5 -> deleteBook();
        }
    }

    private void addBook() throws SQLException {
        String title = in.readLine("Title: ");
        String author = in.readLine("Author: ");
        int year = in.readInt("Publication year: ");

        // The Book constructor validates everything (ID 1 is a placeholder)
        new Book(title, author, 1, year);

        int id = bookRepository.addBook(title, author, year);
        System.out.println("Book added with ID " + id + ".");
    }

    private void searchBooks() throws SQLException {
        String keyword = in.readNonEmpty("Search keyword: ");
        printList("SEARCH RESULTS", bookRepository.searchBooks(keyword),
                "No books found.", "Total books found");
    }

    private void showAllBooks() throws SQLException {
        printList("ALL BOOKS", bookRepository.findAllBooks(),
                "No books registered.", "Total books");
    }

    private void updateBook() throws SQLException {
        Book book = pickers.selectBook();
        if (book == null) return;

        System.out.println("Press Enter to keep the current value.");

        String title = in.readLine("Title [" + book.getTitle() + "]: ");
        String author = in.readLine("Author [" + book.getAuthor() + "]: ");
        Integer year = in.readOptionalInt(
                "Publication year [" + book.getPublicationYear() + "]: ");

        // Setters validate, so invalid input never reaches the database
        if (!title.isEmpty()) book.setTitle(title);
        if (!author.isEmpty()) book.setAuthor(author);
        if (year != null) book.setPublicationYear(year);

        bookRepository.updateBook(book);
        System.out.println("Book updated.");
    }

    private void deleteBook() throws SQLException {
        Book book = pickers.selectBook();
        if (book == null) return;

        if (!in.confirm("Delete \"" + book.getTitle() + "\"? (y/n): ")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        bookRepository.deleteBook(book.getBookId());
        System.out.println("Book deleted.");
    }
}
