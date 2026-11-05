package com.library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
}