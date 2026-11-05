
package com.library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import com.library.model.Borrow;
import com.library.model.Member;
import com.library.model.Book;
import com.library.model.BookCopy;

import com.library.enums.CopyStatus;
import com.library.enums.BorrowStatus;
import com.library.enums.ReservationStatus;

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

	                if (rowsUpdated != 1) {
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
 // Collect a reservation and create a borrowing record
 // All database changes happen in one transaction
 public int collectReservation(
         int reservationId,
         Member member,
         BookCopy copy,
         LocalDate expectedReturnDate) throws SQLException {

     if (member == null || copy == null || expectedReturnDate == null) {
         throw new IllegalArgumentException(
                 "Member, copy and expected return date cannot be null");
     }

     LocalDate borrowDate = LocalDate.now();

     if (!expectedReturnDate.isAfter(borrowDate)) {
         throw new IllegalArgumentException(
                 "Expected return date must be after borrow date");
     }

     // Retrieve and lock the reservation
     String findSql = """
             SELECT member_id, assigned_copy_id, status, expiration_date
             FROM reservations
             WHERE reservation_id = ?
             FOR UPDATE
             """;

     // Change the physical copy to BORROWED
     String updateCopySql = """
             UPDATE book_copies
             SET status = ?
             WHERE copy_id = ?
             AND status = ?
             """;

     // Create the borrowing record
     String insertBorrowSql = """
             INSERT INTO borrows (
                 member_id,
                 copy_id,
                 borrow_date,
                 expected_return_date,
                 status
             )
             VALUES (?, ?, ?, ?, ?)
             """;

     // Mark the reservation as FULFILLED
     String updateReservationSql = """
             UPDATE reservations
             SET status = ?
             WHERE reservation_id = ?
             AND status = ?
             AND assigned_copy_id = ?
             AND expiration_date >= ?
             """;

     try (Connection connection = DatabaseConnection.getConnection()) {

         connection.setAutoCommit(false);

         try {

             // 1. Verify the reservation
             try (PreparedStatement statement =
                     connection.prepareStatement(findSql)) {

                 statement.setInt(1, reservationId);

                 try (ResultSet result = statement.executeQuery()) {

                     if (!result.next()) {
                         throw new SQLException("Reservation not found");
                     }

                     int storedMemberId = result.getInt("member_id");

                     int storedCopyId = result.getInt("assigned_copy_id");
                     boolean copyAssigned = !result.wasNull();

                     ReservationStatus status =
                             ReservationStatus.valueOf(
                                     result.getString("status"));

                     java.sql.Date expirationSqlDate =
                             result.getDate("expiration_date");

                     if (!copyAssigned ||
                             storedMemberId != member.getMemberId() ||
                             storedCopyId != copy.getCopyId() ||
                             status != ReservationStatus.READY_FOR_PICKUP ||
                             expirationSqlDate == null ||
                             borrowDate.isAfter(expirationSqlDate.toLocalDate())) {

                         throw new IllegalStateException(
                                 "Reservation cannot be collected");
                     }
                 }
             }

             // 2. Change the copy from RESERVED to BORROWED
             try (PreparedStatement statement =
                     connection.prepareStatement(updateCopySql)) {

                 statement.setString(1, CopyStatus.BORROWED.name());
                 statement.setInt(2, copy.getCopyId());
                 statement.setString(3, CopyStatus.RESERVED.name());

                 if (statement.executeUpdate() != 1) {
                     throw new SQLException(
                             "Could not update the book copy");
                 }
             }

             // 3. Insert the new borrowing record
             int borrowId;

             try (PreparedStatement statement =
                     connection.prepareStatement(
                             insertBorrowSql,
                             Statement.RETURN_GENERATED_KEYS)) {

                 statement.setInt(1, member.getMemberId());
                 statement.setInt(2, copy.getCopyId());
                 statement.setDate(
                         3, java.sql.Date.valueOf(borrowDate));
                 statement.setDate(
                         4, java.sql.Date.valueOf(expectedReturnDate));
                 statement.setString(
                         5, BorrowStatus.ACTIVE.name());

                 statement.executeUpdate();

                 try (ResultSet keys = statement.getGeneratedKeys()) {

                     if (!keys.next()) {
                         throw new SQLException(
                                 "Borrow ID was not generated");
                     }

                     borrowId = keys.getInt(1);
                 }
             }

             // 4. Mark the reservation as FULFILLED
             try (PreparedStatement statement =
                     connection.prepareStatement(updateReservationSql)) {

                 statement.setString(
                         1, ReservationStatus.FULFILLED.name());
                 statement.setInt(2, reservationId);
                 statement.setString(
                         3, ReservationStatus.READY_FOR_PICKUP.name());
                 statement.setInt(4, copy.getCopyId());
                 statement.setDate(
                         5, java.sql.Date.valueOf(borrowDate));

                 if (statement.executeUpdate() != 1) {
                     throw new SQLException(
                             "Could not fulfill the reservation");
                 }
             }

             // Save all three database changes
             connection.commit();

             return borrowId;

         } catch (SQLException | RuntimeException e) {

             connection.rollback();
             throw e;
         }
     }
 }
//Count a member's active and overdue borrowings
public int countOutstandingBorrowings(int memberId)
      throws SQLException {

  String sql = """
          SELECT COUNT(*)
          FROM borrows
          WHERE member_id = ?
          AND status IN ('ACTIVE', 'OVERDUE')
          """;

  try (Connection connection = DatabaseConnection.getConnection();
       PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, memberId);

      try (ResultSet result = statement.executeQuery()) {

          if (result.next()) {
              return result.getInt(1);
          }

          return 0;
      }
  }
}
//Retrieve all borrowing records belonging to a member
public ArrayList<Borrow> findBorrowingsByMember(int memberId)
     throws SQLException {

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
         WHERE br.member_id = ?
         ORDER BY br.borrow_id
         """;

 ArrayList<Borrow> result = new ArrayList<>();

 try (Connection connection = DatabaseConnection.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)) {

     statement.setInt(1, memberId);

     try (ResultSet rs = statement.executeQuery()) {

         while (rs.next()) {

             Member member = new Member(
                     rs.getString("full_name"),
                     rs.getString("email"),
                     rs.getString("phone_number"),
                     rs.getInt("member_id")
             );

             Book book = new Book(
                     rs.getString("title"),
                     rs.getString("author"),
                     rs.getInt("book_id"),
                     rs.getInt("publication_year")
             );

             BookCopy copy = new BookCopy(
                     rs.getInt("copy_id"),
                     book,
                     CopyStatus.valueOf(rs.getString("copy_status"))
             );

             LocalDate borrowDate =
                     rs.getDate("borrow_date").toLocalDate();

             LocalDate expectedReturnDate =
                     rs.getDate("expected_return_date").toLocalDate();

             Date actualReturn = rs.getDate("actual_return_date");

             LocalDate actualReturnDate = (actualReturn == null)
                     ? null
                     : actualReturn.toLocalDate();

             BorrowStatus status =
                     BorrowStatus.valueOf(rs.getString("status"));

             Borrow borrow = new Borrow(
                     rs.getInt("borrow_id"),
                     member,
                     copy,
                     borrowDate,
                     expectedReturnDate,
                     actualReturnDate,
                     status
             );

             result.add(borrow);
         }
     }
 }

 return result;
}
//Count all borrowing records in the database
public int countBorrowings() throws SQLException {

 String sql = """
         SELECT COUNT(*)
         FROM borrows
         """;

 try (Connection connection = DatabaseConnection.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet result = statement.executeQuery()) {

     if (result.next()) {
         return result.getInt(1);
     }

     return 0;
 }
}
public ArrayList<Borrow> findAllBorrowings() throws SQLException {

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

            ORDER BY br.borrow_id
            """;

    ArrayList<Borrow> result = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet rs = statement.executeQuery()) {

        while (rs.next()) {

        	// Reconstruct Member
        	Member member = new Member(
        	        rs.getString("full_name"),
        	        rs.getString("email"),
        	        rs.getString("phone_number"),
        	        rs.getInt("member_id")
        	);

        	// Reconstruct Book
        	Book book = new Book(
        	        rs.getString("title"),
        	        rs.getString("author"),
        	        rs.getInt("book_id"),
        	        rs.getInt("publication_year")
        	);

            // Reconstruct BookCopy
            BookCopy copy = new BookCopy(
                    rs.getInt("copy_id"),
                    book,
                    CopyStatus.valueOf(rs.getString("copy_status"))
            );

            // Reconstruct Borrow
            Borrow borrow = new Borrow(
                    rs.getInt("borrow_id"),
                    member,
                    copy,
                    rs.getDate("borrow_date").toLocalDate(),
                    rs.getDate("expected_return_date").toLocalDate(),
                    rs.getDate("actual_return_date") != null
                            ? rs.getDate("actual_return_date").toLocalDate()
                            : null,
                    BorrowStatus.valueOf(rs.getString("status"))
            );

            result.add(borrow);
        }
    }

    return result;
}
//Mark active borrowings as overdue when their expected return date has passed
public int updateOverdueBorrowings() throws SQLException {

 String sql = """
         UPDATE borrows
         SET status = ?
         WHERE status = ?
         AND expected_return_date < ?
         """;

 try (Connection connection = DatabaseConnection.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)) {

     statement.setString(1, BorrowStatus.OVERDUE.name());
     statement.setString(2, BorrowStatus.ACTIVE.name());
     statement.setDate(3, Date.valueOf(LocalDate.now()));

     return statement.executeUpdate();
 }
}
}

