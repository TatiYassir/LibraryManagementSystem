
package com.library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;

import com.library.model.Borrow;
import com.library.model.Member;
import com.library.model.Book;
import com.library.model.BookCopy;

import com.library.enums.CopyStatus;
import com.library.enums.BorrowStatus;

public class BorrowRepository {

	// Insert a borrowing and update the physical copy's status
	public int addBorrow(Borrow borrow) throws SQLException {

	    if (borrow == null) {
	        throw new IllegalArgumentException("Borrow cannot be null");
	    }

	    String insertSql = """
	            INSERT INTO borrows (
	                member_id, copy_id, borrow_date,
	                expected_return_date, actual_return_date, status
	            )
	            VALUES (?, ?, ?, ?, ?, ?)
	            """;

	    String updateSql = """
	            UPDATE book_copies
	            SET status = ?
	            WHERE copy_id = ? AND status = ?
	            """;

	    try (Connection connection = DatabaseConnection.getConnection()) {

	        // Start the transaction
	        connection.setAutoCommit(false);

	        try {
	            // Insert the borrowing record
	            int borrowId;

	            try (PreparedStatement statement = connection.prepareStatement(
	                    insertSql, Statement.RETURN_GENERATED_KEYS)) {

	                statement.setInt(1, borrow.getMember().getMemberId());
	                statement.setInt(2, borrow.getBookCopy().getCopyId());
	                statement.setDate(3, Date.valueOf(borrow.getBorrowDate()));
	                statement.setDate(4, Date.valueOf(borrow.getExpectedReturnDate()));

	                if (borrow.getActualReturnDate() == null) {
	                    statement.setNull(5, java.sql.Types.DATE);
	                } else {
	                    statement.setDate(5, Date.valueOf(borrow.getActualReturnDate()));
	                }

	                statement.setString(6, borrow.getStatus().name());

	                statement.executeUpdate();

	                try (ResultSet keys = statement.getGeneratedKeys()) {
	                    if (!keys.next()) {
	                        throw new SQLException("Borrow ID was not generated");
	                    }

	                    borrowId = keys.getInt(1);
	                }
	            }

	            // Mark the physical copy as BORROWED
	            try (PreparedStatement statement =
	                    connection.prepareStatement(updateSql)) {

	                statement.setString(1, CopyStatus.BORROWED.name());
	                statement.setInt(2, borrow.getBookCopy().getCopyId());
	                statement.setString(3, CopyStatus.AVAILABLE.name());

	                int rowsUpdated = statement.executeUpdate();

	                if (rowsUpdated == 0) {
	                    throw new SQLException(
	                            "Book copy does not exist or is not available");
	                }
	            }

	            // Save both operations
	            connection.commit();

	            return borrowId;

	        } catch (SQLException | RuntimeException e) {

	            // Undo both operations if anything fails
	            connection.rollback();
	            throw e;
	        }
	    }
	}

    // Retrieve a borrowing transaction using its database ID
    public Borrow findBorrowById(int borrowId) throws SQLException {

        String sql = """
                SELECT
                    br.borrow_id,
                    br.borrow_date,
                    br.expected_return_date,
                    br.actual_return_date,
                    br.status,
                    m.member_id,
                    m.full_name,
                    m.email,
                    m.phone_number,
                    bc.copy_id,
                    bc.status AS copy_status,
                    b.book_id,
                    b.title,
                    b.author,
                    b.publication_year
                FROM borrows br
                JOIN members m
                    ON br.member_id = m.member_id
                JOIN book_copies bc
                    ON br.copy_id = bc.copy_id
                JOIN books b
                    ON bc.book_id = b.book_id
                WHERE br.borrow_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, borrowId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    // Reconstruct the Member object
                    Member member = new Member(
                            result.getString("full_name"),
                            result.getString("email"),
                            result.getString("phone_number"),
                            result.getInt("member_id")
                    );

                    // Reconstruct the Book object
                    Book book = new Book(
                            result.getString("title"),
                            result.getString("author"),
                            result.getInt("book_id"),
                            result.getInt("publication_year")
                    );

                    // Reconstruct the BookCopy object
                    BookCopy copy = new BookCopy(
                            result.getInt("copy_id"),
                            book,
                            CopyStatus.valueOf(result.getString("copy_status"))
                    );

                    // Read the original borrowing dates
                    LocalDate borrowDate =
                            result.getDate("borrow_date").toLocalDate();

                    LocalDate expectedReturnDate =
                            result.getDate("expected_return_date").toLocalDate();

                    Date actualReturn = result.getDate("actual_return_date");

                    LocalDate actualReturnDate = (actualReturn == null)
                            ? null
                            : actualReturn.toLocalDate();

                    // Restore the original borrowing status
                    BorrowStatus status =
                            BorrowStatus.valueOf(result.getString("status"));

                    // Reconstruct the complete Borrow object
                    return new Borrow(
                            result.getInt("borrow_id"),
                            member,
                            copy,
                            borrowDate,
                            expectedReturnDate,
                            actualReturnDate,
                            status
                    );
                }

                // No borrowing record found
                return null;
            }
        }
    }
 // Return a borrowed book and update its physical copy
    public void returnBorrow(Borrow borrow) throws SQLException {

        if (borrow == null) {
            throw new IllegalArgumentException("Borrow cannot be null");
        }

        if (borrow.getStatus() != BorrowStatus.ACTIVE
                && borrow.getStatus() != BorrowStatus.OVERDUE) {
            throw new IllegalStateException(
                    "Only active or overdue borrowings can be returned");
        }

        String borrowSql = """
                UPDATE borrows
                SET actual_return_date = ?, status = ?
                WHERE borrow_id = ?
                  AND status IN ('ACTIVE', 'OVERDUE')
                """;

        String copySql = """
                UPDATE book_copies
                SET status = ?
                WHERE copy_id = ? AND status = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Update the borrowing record
                try (PreparedStatement statement =
                        connection.prepareStatement(borrowSql)) {

                    statement.setDate(1, Date.valueOf(LocalDate.now()));
                    statement.setString(2, BorrowStatus.RETURNED.name());
                    statement.setInt(3, borrow.getBorrowId());

                    int rowsUpdated = statement.executeUpdate();

                    if (rowsUpdated == 0) {
                        throw new SQLException(
                                "Borrowing not found or already returned");
                    }
                }

                // Make the physical copy available again
                try (PreparedStatement statement =
                        connection.prepareStatement(copySql)) {

                    statement.setString(1, CopyStatus.AVAILABLE.name());
                    statement.setInt(2, borrow.getBookCopy().getCopyId());
                    statement.setString(3, CopyStatus.BORROWED.name());

                    int rowsUpdated = statement.executeUpdate();

                    if (rowsUpdated == 0) {
                        throw new SQLException(
                                "Book copy not found or is not borrowed");
                    }
                }

                // Commit both database updates
                connection.commit();

                // Synchronize the Java objects after successful commit
                borrow.returnBook();
                borrow.getBookCopy().setStatus(CopyStatus.AVAILABLE);

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }
 // Update the status of a physical book copy
    public void updateCopyStatus(int copyId, CopyStatus status)
            throws SQLException {

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        String sql = """
                UPDATE book_copies
                SET status = ?
                WHERE copy_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, copyId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("Book copy not found");
            }
        }
    }
}
