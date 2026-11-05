package com.library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.enums.CopyStatus;

public class BookCopyRepository {

    // Insert a physical copy associated with an existing book
    public int addBookCopy(Book book, CopyStatus status)
            throws SQLException {

        if (book == null || status == null) {
            throw new IllegalArgumentException(
                    "Book and status cannot be null");
        }

        String sql = """
                INSERT INTO book_copies (book_id, status)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, book.getBookId());
            statement.setString(2, status.name());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }

                throw new SQLException("Copy ID was not generated");
            }
        }
    }
 // Find a physical book copy using its database ID
    public BookCopy findBookCopyById(int copyId) throws SQLException {

        String sql = """
                SELECT bc.copy_id, bc.status,
                       b.book_id, b.title, b.author, b.publication_year
                FROM book_copies bc
                JOIN books b ON bc.book_id = b.book_id
                WHERE bc.copy_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, copyId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    // Rebuild the Book object
                    Book book = new Book(
                            result.getString("title"),
                            result.getString("author"),
                            result.getInt("book_id"),
                            result.getInt("publication_year")
                    );

                    // Convert the database status into a Java enum
                    CopyStatus status =
                            CopyStatus.valueOf(result.getString("status"));

                    // Rebuild and return the physical copy
                    return new BookCopy(
                            result.getInt("copy_id"),
                            book,
                            status
                    );
                }

                // No copy with this ID exists
                return null;
            }
        }
    }
    public ArrayList<BookCopy> findAllBookCopies() throws SQLException {

        String sql = """
                SELECT bc.copy_id, bc.status,
                       b.book_id, b.title, b.author, b.publication_year
                FROM book_copies bc
                JOIN books b ON bc.book_id = b.book_id
                """;

        ArrayList<BookCopy> copies = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    // Rebuild the Book object
                    Book book = new Book(
                            result.getString("title"),
                            result.getString("author"),
                            result.getInt("book_id"),
                            result.getInt("publication_year")
                    );

                    // Convert database status into a Java enum
                    CopyStatus status =
                            CopyStatus.valueOf(result.getString("status"));

                    // Rebuild the physical copy
                    BookCopy copy = new BookCopy(
                            result.getInt("copy_id"),
                            book,
                            status
                    );

                    copies.add(copy);
                }
            }
        }

        return copies;
    }
 // Search physical copies by book title or author
    public ArrayList<BookCopy> searchBookCopies(String keyword)
            throws SQLException {

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException(
                    "Search keyword cannot be empty");
        }

        String sql = """
                SELECT bc.copy_id, bc.status,
                       b.book_id, b.title, b.author, b.publication_year
                FROM book_copies bc
                JOIN books b ON bc.book_id = b.book_id
                WHERE LOWER(b.title) LIKE LOWER(?)
                   OR LOWER(b.author) LIKE LOWER(?)
                ORDER BY b.title, bc.copy_id
                """;

        ArrayList<BookCopy> copies = new ArrayList<>();

        String searchPattern = "%" + keyword.trim() + "%";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Book book = new Book(
                            result.getString("title"),
                            result.getString("author"),
                            result.getInt("book_id"),
                            result.getInt("publication_year")
                    );

                    CopyStatus status =
                            CopyStatus.valueOf(result.getString("status"));

                    BookCopy copy = new BookCopy(
                            result.getInt("copy_id"),
                            book,
                            status
                    );

                    copies.add(copy);
                }
            }
        }

        return copies;
    }
    public ArrayList<BookCopy> findCopiesByBookId(int bookId)
            throws SQLException {

        if (bookId <= 0) {
            throw new IllegalArgumentException("Invalid book ID");
        }

        String sql = """
                SELECT bc.copy_id, bc.status,
                       b.book_id, b.title, b.author, b.publication_year
                FROM book_copies bc
                JOIN books b ON bc.book_id = b.book_id
                WHERE bc.book_id = ?
                """;

        ArrayList<BookCopy> copies = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Book book = new Book(
                            result.getString("title"),
                            result.getString("author"),
                            result.getInt("book_id"),
                            result.getInt("publication_year")
                    );

                    CopyStatus status =
                            CopyStatus.valueOf(result.getString("status"));

                    BookCopy copy = new BookCopy(
                            result.getInt("copy_id"),
                            book,
                            status
                    );

                    copies.add(copy);
                }
            }
        }

        return copies;
    }
}