
package com.library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import com.library.model.Reservation;
import com.library.model.Member;
import com.library.model.Book;
import com.library.model.BookCopy;

import com.library.enums.CopyStatus;
import com.library.enums.ReservationStatus;

public class ReservationRepository {

        // Insert a new reservation and return its generated database ID
        public int addReservation(
                        Member member,
                        Book book,
                        LocalDate reservationDate) throws SQLException {

                if (member == null || book == null || reservationDate == null) {
                        throw new IllegalArgumentException(
                                        "Member, book and reservation date cannot be null");
                }

                String sql = """
                                INSERT INTO reservations (
                                    member_id,
                                    book_id,
                                    reservation_date,
                                    status
                                )
                                VALUES (?, ?, ?, ?)
                                """;

                try (Connection connection = DatabaseConnection.getConnection();
                                PreparedStatement statement = connection.prepareStatement(
                                                sql, Statement.RETURN_GENERATED_KEYS)) {

                        statement.setInt(1, member.getMemberId());
                        statement.setInt(2, book.getBookId());
                        statement.setDate(3,
                                        java.sql.Date.valueOf(reservationDate));
                        statement.setString(4, ReservationStatus.WAITING.name());

                        statement.executeUpdate();

                        try (ResultSet keys = statement.getGeneratedKeys()) {

                                if (keys.next()) {
                                        return keys.getInt(1);
                                }

                                throw new SQLException(
                                                "Reservation ID was not generated");
                        }
                }
        }

        // Find a reservation by its database ID
        public Reservation findReservationById(int reservationId)
                        throws SQLException {

                String sql = """
                                SELECT
                                    r.reservation_id,
                                    r.reservation_date,
                                    r.expiration_date,
                                    r.status,
                                    m.member_id,
                                    m.full_name,
                                    m.email,
                                    m.phone_number,
                                    b.book_id,
                                    b.title,
                                    b.author,
                                    b.publication_year,
                                    bc.copy_id,
                                    bc.status AS copy_status

                                FROM reservations r

                                JOIN members m
                                    ON r.member_id = m.member_id

                                JOIN books b
                                    ON r.book_id = b.book_id

                                LEFT JOIN book_copies bc
                                    ON r.assigned_copy_id = bc.copy_id

                                WHERE r.reservation_id = ?
                                """;

                try (Connection connection = DatabaseConnection.getConnection();
                                PreparedStatement statement = connection.prepareStatement(sql)) {

                        // Provide the reservation ID
                        statement.setInt(1, reservationId);

                        // Execute the SELECT query
                        try (ResultSet result = statement.executeQuery()) {

                                if (result.next()) {

                                        // Reconstruct the member
                                        Member member = new Member(
                                                        result.getString("full_name"),
                                                        result.getString("email"),
                                                        result.getString("phone_number"),
                                                        result.getInt("member_id"));

                                        // Reconstruct the book
                                        Book book = new Book(
                                                        result.getString("title"),
                                                        result.getString("author"),
                                                        result.getInt("book_id"),
                                                        result.getInt("publication_year"));

                                        // Reconstruct the assigned copy, if one exists
                                        BookCopy bookCopy = null;

                                        int copyId = result.getInt("copy_id");

                                        if (!result.wasNull()) {

                                                bookCopy = new BookCopy(
                                                                copyId,
                                                                book,
                                                                CopyStatus.valueOf(
                                                                                result.getString("copy_status")));
                                        }

                                        // Retrieve the expiration date
                                        java.sql.Date expirationSqlDate = result.getDate("expiration_date");

                                        LocalDate expirationDate = expirationSqlDate == null
                                                        ? null
                                                        : expirationSqlDate.toLocalDate();

                                        // Reconstruct the reservation
                                        return new Reservation(
                                                        result.getInt("reservation_id"),
                                                        member,
                                                        book,
                                                        result.getDate("reservation_date").toLocalDate(),
                                                        expirationDate,
                                                        bookCopy,
                                                        ReservationStatus.valueOf(
                                                                        result.getString("status")));
                                }

                                // Reservation does not exist
                                return null;
                        }
                }
        }

        // Retrieve all reservations from the database
        public ArrayList<Reservation> findAllReservations()
                        throws SQLException {
                // Stores all reconstructed reservations
                ArrayList<Reservation> reservations = new ArrayList<>();

                String sql = """
                                SELECT
                                    r.reservation_id,
                                    r.reservation_date,
                                    r.expiration_date,
                                    r.status,
                                    m.member_id,
                                    m.full_name,
                                    m.email,
                                    m.phone_number,
                                    b.book_id,
                                    b.title,
                                    b.author,
                                    b.publication_year,
                                    bc.copy_id,
                                    bc.status AS copy_status
                                FROM reservations r
                                JOIN members m ON r.member_id = m.member_id
                                JOIN books b ON r.book_id = b.book_id
                                LEFT JOIN book_copies bc ON r.assigned_copy_id = bc.copy_id
                                ORDER BY r.reservation_id
                                """;
                try (Connection connection = DatabaseConnection.getConnection();
                                PreparedStatement statement = connection.prepareStatement(sql)) {

                        try (ResultSet result = statement.executeQuery()) {

                                while (result.next()) {

                                        // Reconstruct the member
                                        Member member = new Member(
                                                        result.getString("full_name"),
                                                        result.getString("email"),
                                                        result.getString("phone_number"),
                                                        result.getInt("member_id"));

                                        // Reconstruct the book
                                        Book book = new Book(
                                                        result.getString("title"),
                                                        result.getString("author"),
                                                        result.getInt("book_id"),
                                                        result.getInt("publication_year"));

                                        // Reconstruct the assigned copy, if one exists
                                        BookCopy bookCopy = null;

                                        int copyId = result.getInt("copy_id");

                                        if (!result.wasNull()) {

                                                bookCopy = new BookCopy(
                                                                copyId,
                                                                book,
                                                                CopyStatus.valueOf(result.getString("copy_status")));
                                        }

                                        // Retrieve the expiration date
                                        java.sql.Date expirationSqlDate = result.getDate("expiration_date");

                                        LocalDate expirationDate = expirationSqlDate == null
                                                        ? null
                                                        : expirationSqlDate.toLocalDate();

                                        // Reconstruct the reservation
                                        Reservation reservation = new Reservation(
                                                        result.getInt("reservation_id"),
                                                        member,
                                                        book,
                                                        result.getDate("reservation_date").toLocalDate(),
                                                        expirationDate,
                                                        bookCopy,
                                                        ReservationStatus.valueOf(result.getString("status")));

                                        // Add the reconstructed reservation to the list
                                        reservations.add(reservation);

                                }
                        }
                }

                return reservations;
        }

        public ArrayList<Reservation> findReservationsByMember(int memberId)
                        throws SQLException {
                // Stores the member's reservations
                ArrayList<Reservation> reservations = new ArrayList<>();

                String sql = """
                                SELECT
                                    r.reservation_id,
                                    r.reservation_date,
                                    r.expiration_date,
                                    r.status,
                                    m.member_id,
                                    m.full_name,
                                    m.email,
                                    m.phone_number,
                                    b.book_id,
                                    b.title,
                                    b.author,
                                    b.publication_year,
                                    bc.copy_id,
                                    bc.status AS copy_status
                                FROM reservations r
                                JOIN members m ON r.member_id = m.member_id
                                JOIN books b ON r.book_id = b.book_id
                                LEFT JOIN book_copies bc ON r.assigned_copy_id = bc.copy_id
                                WHERE r.member_id = ?
                                ORDER BY r.reservation_id
                                """;
                try (Connection connection = DatabaseConnection.getConnection();
                                PreparedStatement statement = connection.prepareStatement(sql)) {

                        // Supply the member ID to the SQL placeholder
                        statement.setInt(1, memberId);

                        try (ResultSet result = statement.executeQuery()) {

                                while (result.next()) {

                                        // Reconstruct the member
                                        Member member = new Member(
                                                        result.getString("full_name"),
                                                        result.getString("email"),
                                                        result.getString("phone_number"),
                                                        result.getInt("member_id"));

                                        // Reconstruct the book
                                        Book book = new Book(
                                                        result.getString("title"),
                                                        result.getString("author"),
                                                        result.getInt("book_id"),
                                                        result.getInt("publication_year"));

                                        // Reconstruct the assigned copy, if one exists
                                        BookCopy bookCopy = null;

                                        int copyId = result.getInt("copy_id");

                                        if (!result.wasNull()) {
                                                bookCopy = new BookCopy(
                                                                copyId,
                                                                book,
                                                                CopyStatus.valueOf(result.getString("copy_status")));
                                        }

                                        // Retrieve the expiration date
                                        java.sql.Date expirationSqlDate = result.getDate("expiration_date");

                                        LocalDate expirationDate = expirationSqlDate == null
                                                        ? null
                                                        : expirationSqlDate.toLocalDate();

                                        // Reconstruct the reservation
                                        Reservation reservation = new Reservation(
                                                        result.getInt("reservation_id"),
                                                        member,
                                                        book,
                                                        result.getDate("reservation_date").toLocalDate(),
                                                        expirationDate,
                                                        bookCopy,
                                                        ReservationStatus.valueOf(result.getString("status")));

                                        // Add it to the list
                                        reservations.add(reservation);

                                }
                        }
                }

                return reservations;
        }

        // Count all reservation records in the database
        public int countReservations() throws SQLException {

                String sql = "SELECT COUNT(*) FROM reservations";

                try (Connection connection = DatabaseConnection.getConnection();
                                PreparedStatement statement = connection.prepareStatement(sql);
                                ResultSet result = statement.executeQuery()) {

                        if (result.next()) {
                                return result.getInt(1);
                        }

                        return 0;
                }
        }

        // Assign an available copy to the first waiting reservation
        // and save both changes in one database transaction
        public int assignNextWaitingReservation(
                        int bookId, int copyId, LocalDate expirationDate)
                        throws SQLException {

                if (expirationDate == null) {
                        throw new IllegalArgumentException(
                                        "Expiration date cannot be null");
                }

                // Find the oldest waiting reservation for this book
                String findSql = """
                                SELECT reservation_id
                                FROM reservations
                                WHERE book_id = ? AND status = ?
                                ORDER BY reservation_id
                                LIMIT 1
                                FOR UPDATE
                                """;

                // Reserve the physical copy
                String updateCopySql = """
                                UPDATE book_copies
                                SET status = ?
                                WHERE copy_id = ?
                                AND book_id = ?
                                AND status = ?
                                """;

                // Update the selected reservation
                String updateReservationSql = """
                                UPDATE reservations
                                SET assigned_copy_id = ?,
                                    expiration_date = ?,
                                    status = ?
                                WHERE reservation_id = ?
                                AND status = ?
                                """;

                try (Connection connection = DatabaseConnection.getConnection()) {

                        connection.setAutoCommit(false);

                        try {

                                int reservationId;

                                // Find the first waiting reservation (FIFO)
                                try (PreparedStatement statement = connection.prepareStatement(findSql)) {

                                        statement.setInt(1, bookId);
                                        statement.setString(
                                                        2, ReservationStatus.WAITING.name());

                                        try (ResultSet result = statement.executeQuery()) {

                                                if (!result.next()) {
                                                        connection.commit();
                                                        return -1;
                                                }

                                                reservationId = result.getInt("reservation_id");
                                        }
                                }

                                // Change the physical copy to RESERVED
                                try (PreparedStatement statement = connection.prepareStatement(updateCopySql)) {

                                        statement.setString(
                                                        1, CopyStatus.RESERVED.name());
                                        statement.setInt(2, copyId);
                                        statement.setInt(3, bookId);
                                        statement.setString(
                                                        4, CopyStatus.AVAILABLE.name());

                                        if (statement.executeUpdate() != 1) {
                                                throw new SQLException(
                                                                "Copy is not available or does not belong to this book");
                                        }
                                }

                                // Update the reservation
                                try (PreparedStatement statement = connection.prepareStatement(updateReservationSql)) {

                                        statement.setInt(1, copyId);
                                        statement.setDate(
                                                        2, java.sql.Date.valueOf(expirationDate));
                                        statement.setString(
                                                        3, ReservationStatus.READY_FOR_PICKUP.name());
                                        statement.setInt(4, reservationId);
                                        statement.setString(
                                                        5, ReservationStatus.WAITING.name());

                                        if (statement.executeUpdate() != 1) {
                                                throw new SQLException(
                                                                "Reservation could not be assigned");
                                        }
                                }

                                // Save both changes
                                connection.commit();

                                return reservationId;

                        } catch (SQLException | RuntimeException e) {

                                connection.rollback();
                                throw e;
                        }
                }
        }

        // Cancel a reservation and reassign its copy to the next waiting member
        public int cancelReservationAndReassign(
                        int reservationId, LocalDate nextExpirationDate)
                        throws SQLException {

                if (nextExpirationDate == null) {
                        throw new IllegalArgumentException(
                                        "Expiration date cannot be null");
                }

                // Retrieve and lock the reservation being cancelled
                String findSql = """
                                SELECT book_id, assigned_copy_id, status
                                FROM reservations
                                WHERE reservation_id = ?
                                FOR UPDATE
                                """;

                // Mark the selected reservation as cancelled
                String cancelSql = """
                                UPDATE reservations
                                SET status = ?
                                WHERE reservation_id = ?
                                AND status = ?
                                """;

                // Release the assigned physical copy
                String releaseCopySql = """
                                UPDATE book_copies
                                SET status = ?
                                WHERE copy_id = ?
                                AND book_id = ?
                                AND status = ?
                                """;

                // Find the next waiting reservation (FIFO)
                String findNextSql = """
                                SELECT reservation_id
                                FROM reservations
                                WHERE book_id = ?
                                AND status = ?
                                ORDER BY reservation_id
                                LIMIT 1
                                FOR UPDATE
                                """;

                // Assign the released copy to the next reservation
                String assignCopySql = """
                                UPDATE book_copies
                                SET status = ?
                                WHERE copy_id = ?
                                AND status = ?
                                """;

                // Update the next reservation
                String updateNextSql = """
                                UPDATE reservations
                                SET assigned_copy_id = ?,
                                    expiration_date = ?,
                                    status = ?
                                WHERE reservation_id = ?
                                AND status = ?
                                """;

                try (Connection connection = DatabaseConnection.getConnection()) {

                        connection.setAutoCommit(false);

                        try {

                                int bookId;
                                int copyId;
                                boolean hasAssignedCopy;
                                ReservationStatus currentStatus;

                                // 1. Retrieve the reservation and lock its row
                                try (PreparedStatement statement = connection.prepareStatement(findSql)) {

                                        statement.setInt(1, reservationId);

                                        try (ResultSet result = statement.executeQuery()) {

                                                if (!result.next()) {
                                                        throw new SQLException(
                                                                        "Reservation not found");
                                                }

                                                bookId = result.getInt("book_id");

                                                copyId = result.getInt("assigned_copy_id");
                                                hasAssignedCopy = !result.wasNull();

                                                currentStatus = ReservationStatus.valueOf(
                                                                result.getString("status"));
                                        }
                                }

                                // Only WAITING and READY_FOR_PICKUP can be cancelled
                                if (currentStatus != ReservationStatus.WAITING &&
                                                currentStatus != ReservationStatus.READY_FOR_PICKUP) {

                                        throw new IllegalStateException(
                                                        "Reservation cannot be cancelled");
                                }

                                // 2. Mark the reservation as CANCELLED
                                try (PreparedStatement statement = connection.prepareStatement(cancelSql)) {

                                        statement.setString(
                                                        1, ReservationStatus.CANCELLED.name());
                                        statement.setInt(2, reservationId);
                                        statement.setString(3, currentStatus.name());

                                        if (statement.executeUpdate() != 1) {
                                                throw new SQLException(
                                                                "Could not cancel reservation");
                                        }
                                }

                                // If no copy was assigned, cancellation is complete
                                if (!hasAssignedCopy) {
                                        connection.commit();
                                        return -1;
                                }

                                // 3. Release the physical copy
                                try (PreparedStatement statement = connection.prepareStatement(releaseCopySql)) {

                                        statement.setString(
                                                        1, CopyStatus.AVAILABLE.name());
                                        statement.setInt(2, copyId);
                                        statement.setInt(3, bookId);
                                        statement.setString(
                                                        4, CopyStatus.RESERVED.name());

                                        if (statement.executeUpdate() != 1) {
                                                throw new SQLException(
                                                                "Could not release the assigned copy");
                                        }
                                }

                                int nextReservationId;

                                // 4. Find the next waiting reservation
                                try (PreparedStatement statement = connection.prepareStatement(findNextSql)) {

                                        statement.setInt(1, bookId);
                                        statement.setString(
                                                        2, ReservationStatus.WAITING.name());

                                        try (ResultSet result = statement.executeQuery()) {

                                                if (!result.next()) {
                                                        connection.commit();
                                                        return -1;
                                                }

                                                nextReservationId = result.getInt("reservation_id");
                                        }
                                }

                                // 5. Change the released copy to RESERVED
                                try (PreparedStatement statement = connection.prepareStatement(assignCopySql)) {

                                        statement.setString(
                                                        1, CopyStatus.RESERVED.name());
                                        statement.setInt(2, copyId);
                                        statement.setString(
                                                        3, CopyStatus.AVAILABLE.name());

                                        if (statement.executeUpdate() != 1) {
                                                throw new SQLException(
                                                                "Could not reserve the released copy");
                                        }
                                }

                                // 6. Assign the copy to the next waiting reservation
                                try (PreparedStatement statement = connection.prepareStatement(updateNextSql)) {

                                        statement.setInt(1, copyId);

                                        statement.setDate(
                                                        2, java.sql.Date.valueOf(nextExpirationDate));

                                        statement.setString(
                                                        3, ReservationStatus.READY_FOR_PICKUP.name());

                                        statement.setInt(4, nextReservationId);

                                        statement.setString(
                                                        5, ReservationStatus.WAITING.name());

                                        if (statement.executeUpdate() != 1) {
                                                throw new SQLException(
                                                                "Could not assign the next reservation");
                                        }
                                }

                                // Save all changes
                                connection.commit();

                                return nextReservationId;

                        } catch (SQLException | RuntimeException e) {

                                connection.rollback();
                                throw e;
                        }
                }
        }
     // Expire a reservation and reassign its copy to the next waiting member
        public int expireReservationAndReassign(
                int reservationId, LocalDate nextExpirationDate)
                throws SQLException {

            if (nextExpirationDate == null) {
                throw new IllegalArgumentException(
                        "Expiration date cannot be null");
            }

            // Retrieve and lock the reservation
            String findSql = """
                    SELECT book_id, assigned_copy_id, expiration_date, status
                    FROM reservations
                    WHERE reservation_id = ?
                    FOR UPDATE
                    """;

            // Mark the reservation as expired
            String expireSql = """
                    UPDATE reservations
                    SET status = ?
                    WHERE reservation_id = ?
                    AND status = ?
                    """;

            // Release the assigned copy
            String releaseCopySql = """
                    UPDATE book_copies
                    SET status = ?
                    WHERE copy_id = ?
                    AND book_id = ?
                    AND status = ?
                    """;

            // Find the next waiting reservation (FIFO)
            String findNextSql = """
                    SELECT reservation_id
                    FROM reservations
                    WHERE book_id = ?
                    AND status = ?
                    ORDER BY reservation_id
                    LIMIT 1
                    FOR UPDATE
                    """;

            // Reserve the released copy for the next member
            String assignCopySql = """
                    UPDATE book_copies
                    SET status = ?
                    WHERE copy_id = ?
                    AND status = ?
                    """;

            // Update the next reservation
            String updateNextSql = """
                    UPDATE reservations
                    SET assigned_copy_id = ?,
                        expiration_date = ?,
                        status = ?
                    WHERE reservation_id = ?
                    AND status = ?
                    """;

            try (Connection connection = DatabaseConnection.getConnection()) {

                connection.setAutoCommit(false);

                try {

                    int bookId;
                    int copyId;
                    LocalDate currentExpirationDate;
                    ReservationStatus currentStatus;

                    // 1. Retrieve the reservation and lock its row
                    try (PreparedStatement statement =
                            connection.prepareStatement(findSql)) {

                        statement.setInt(1, reservationId);

                        try (ResultSet result = statement.executeQuery()) {

                            if (!result.next()) {
                                throw new SQLException("Reservation not found");
                            }

                            bookId = result.getInt("book_id");

                            copyId = result.getInt("assigned_copy_id");

                            if (result.wasNull()) {
                                throw new IllegalStateException(
                                        "Reservation has no assigned copy");
                            }

                            java.sql.Date expirationSqlDate =
                                    result.getDate("expiration_date");

                            currentExpirationDate = expirationSqlDate == null
                                    ? null
                                    : expirationSqlDate.toLocalDate();

                            currentStatus = ReservationStatus.valueOf(
                                    result.getString("status"));
                        }
                    }

                    // Only READY_FOR_PICKUP reservations can expire
                    if (currentStatus != ReservationStatus.READY_FOR_PICKUP) {
                        throw new IllegalStateException(
                                "Reservation cannot expire");
                    }

                    // Verify that the pickup period has passed
                    if (currentExpirationDate == null ||
                            !LocalDate.now().isAfter(currentExpirationDate)) {

                        throw new IllegalStateException(
                                "Reservation has not expired yet");
                    }

                    // 2. Mark the reservation as EXPIRED
                    try (PreparedStatement statement =
                            connection.prepareStatement(expireSql)) {

                        statement.setString(
                                1, ReservationStatus.EXPIRED.name());
                        statement.setInt(2, reservationId);
                        statement.setString(
                                3, ReservationStatus.READY_FOR_PICKUP.name());

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Could not expire reservation");
                        }
                    }

                    // 3. Release the physical copy
                    try (PreparedStatement statement =
                            connection.prepareStatement(releaseCopySql)) {

                        statement.setString(
                                1, CopyStatus.AVAILABLE.name());
                        statement.setInt(2, copyId);
                        statement.setInt(3, bookId);
                        statement.setString(
                                4, CopyStatus.RESERVED.name());

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Could not release the assigned copy");
                        }
                    }

                    int nextReservationId;

                    // 4. Find the next waiting reservation
                    try (PreparedStatement statement =
                            connection.prepareStatement(findNextSql)) {

                        statement.setInt(1, bookId);
                        statement.setString(
                                2, ReservationStatus.WAITING.name());

                        try (ResultSet result = statement.executeQuery()) {

                            if (!result.next()) {
                                connection.commit();
                                return -1;
                            }

                            nextReservationId =
                                    result.getInt("reservation_id");
                        }
                    }

                    // 5. Change the released copy to RESERVED
                    try (PreparedStatement statement =
                            connection.prepareStatement(assignCopySql)) {

                        statement.setString(
                                1, CopyStatus.RESERVED.name());
                        statement.setInt(2, copyId);
                        statement.setString(
                                3, CopyStatus.AVAILABLE.name());

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Could not reserve the released copy");
                        }
                    }

                    // 6. Assign the copy to the next reservation
                    try (PreparedStatement statement =
                            connection.prepareStatement(updateNextSql)) {

                        statement.setInt(1, copyId);
                        statement.setDate(
                                2, java.sql.Date.valueOf(nextExpirationDate));
                        statement.setString(
                                3, ReservationStatus.READY_FOR_PICKUP.name());
                        statement.setInt(4, nextReservationId);
                        statement.setString(
                                5, ReservationStatus.WAITING.name());

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Could not assign the next reservation");
                        }
                    }

                    // Save all changes
                    connection.commit();

                    return nextReservationId;

                } catch (SQLException | RuntimeException e) {

                    connection.rollback();
                    throw e;
                }
            }
        }
}
