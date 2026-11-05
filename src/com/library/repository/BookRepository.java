package com.library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.library.model.Book;
public class BookRepository {

    // Insert a book and return its database-generated ID
    public int addBook(String title, String author, int publicationYear)
            throws SQLException {

        String sql = """
                INSERT INTO books (title, author, publication_year)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, publicationYear);

            statement.executeUpdate();

            // Retrieve the automatically generated book ID
            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    return keys.getInt(1);
                }

                throw new SQLException("Book ID was not generated");
            }
        }
    }
 // Retrieve all registered books, including those without physical copies
    public ArrayList<Book> findAllBooks() throws SQLException {

        String sql = """
                SELECT book_id, title, author, publication_year
                FROM books
                ORDER BY book_id
                """;

        ArrayList<Book> books = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Book book = new Book(
                        result.getString("title"),
                        result.getString("author"),
                        result.getInt("book_id"),
                        result.getInt("publication_year")
                );

                books.add(book);
            }
        }

        return books;
    }
 // Retrieve a book using its database ID
    public Book findBookById(int bookId) throws SQLException {

        String sql = """
                SELECT book_id, title, author, publication_year
                FROM books
                WHERE book_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return new Book(
                            result.getString("title"),
                            result.getString("author"),
                            result.getInt("book_id"),
                            result.getInt("publication_year")
                    );
                }

                return null;
            }
        }
    }
 // Update an existing book's information
    public void updateBook(Book book) throws SQLException {

        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        String sql = """
                UPDATE books
                SET title = ?, author = ?, publication_year = ?
                WHERE book_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setInt(3, book.getPublicationYear());
            statement.setInt(4, book.getBookId());

            statement.executeUpdate();
        }
    }
    public void deleteBook(int bookId) throws SQLException {

        String sql = """
                DELETE FROM books
                WHERE book_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);
            statement.executeUpdate();
        }
    }
}