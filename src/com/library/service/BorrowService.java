package com.library.service;

import java.util.ArrayList;
import java.time.LocalDate;

import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.model.Reservation;

import com.library.enums.CopyStatus;
import com.library.enums.BorrowStatus;

import java.sql.SQLException;
import com.library.repository.BorrowRepository;
import com.library.enums.ReservationStatus;

public class BorrowService {

    // Attributes
    private final BorrowRepository borrowRepository = new BorrowRepository();
    private final ReservationService reservationService;
    private final ArrayList<Borrow> borrows = new ArrayList<>();
    
    /**
     * Creates a borrowing service connected to a reservation service.
     *
     * @param reservationService the service used to assign returned copies
     *                           to waiting reservations
     * @throws IllegalArgumentException if the service is null
     */
    public BorrowService(ReservationService reservationService) {
        if (reservationService == null) {
            throw new IllegalArgumentException("ReservationService cannot be null");
        }
        this.reservationService = reservationService;
    }

    /**
     * Borrows an available physical book copy for a member.
     */
    public Borrow borrowBook(
            Member member,
            BookCopy copy,
            LocalDate expectedReturnDate) throws SQLException {

        // Validate inputs
        if (member == null || copy == null || expectedReturnDate == null) {
            throw new IllegalArgumentException(
                    "Member, copy and expected return date cannot be null");
        }

        // Check copy availability
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new IllegalStateException("This copy is not available");
        }

        // Count outstanding borrowings directly from MariaDB
        int count = borrowRepository.countOutstandingBorrowings(
                member.getMemberId());

        // Enforce the maximum borrowing limit
        if (count >= 3) {
            throw new IllegalStateException(
                    "Member has reached the borrowing limit");
        }

        // Create a temporary Borrow object for repository insertion (ID 1 is a placeholder)
        Borrow draft = new Borrow(
                1,
                member,
                copy,
                expectedReturnDate
        );

        // Insert the borrowing and update the copy in MariaDB
        int borrowId = borrowRepository.addBorrow(draft);

        // Reconstruct the Java object using the database-generated ID
        Borrow borrow = new Borrow(
                borrowId,
                member,
                copy,
                draft.getBorrowDate(),
                draft.getExpectedReturnDate(),
                draft.getActualReturnDate(),
                draft.getStatus()
        );

        // Synchronize the physical copy
        copy.setStatus(CopyStatus.BORROWED);

        // Store the borrowing in memory
        borrows.add(borrow);

        return borrow;
    }

    /**
     * Returns a borrowed book and updates MariaDB.
     */
    public void returnBook(Borrow borrow) throws SQLException {

        if (borrow == null) {
            throw new IllegalArgumentException("Borrow cannot be null");
        }

        // Verify that this borrowing belongs to the service
        if (findBorrowById(borrow.getBorrowId()) != borrow) {
            throw new IllegalArgumentException(
                    "Borrowing does not belong to this service");
        }

        // Keep a reference to the physical copy
        BookCopy copy = borrow.getBookCopy();

        // Update MariaDB and synchronize the Java objects
        borrowRepository.returnBorrow(borrow);

        // Assign the released copy to the next waiting reservation
        reservationService.assignNextReservation(copy.getBook(), copy);
    }
    
    /**
     * Checks all active borrowings and marks them as overdue
     * when their expected return date has passed.
     */
    public void checkOverdueBorrowings() throws SQLException {

        // Update overdue statuses in MariaDB
        borrowRepository.updateOverdueBorrowings();

        // Synchronize the local borrowing objects
        for (Borrow borrow : borrows) {
            if (borrow.getStatus() == BorrowStatus.ACTIVE
                    && LocalDate.now().isAfter(borrow.getExpectedReturnDate())) {
                borrow.markAsOverdue();
            }
        }
    }
    
    /**
     * Searches for a borrowing record using its unique ID.
     */
    public Borrow findBorrowById(int borrowId) throws SQLException {

        // First, search in memory
        for (Borrow borrow : borrows) {
            if (borrowId == borrow.getBorrowId()) {
                return borrow;
            }
        }

        // If not found, search in MariaDB
        Borrow borrow = borrowRepository.findBorrowById(borrowId);

        // Cache the result if it exists
        if (borrow != null) {
            borrows.add(borrow);
        }

        return borrow;
    }

    /**
     * Displays borrowing records with clean spacing aligned to 105 characters.
     */
    private void displayBorrowingTable(ArrayList<Borrow> borrowings) {

        System.out.println();
        System.out.println("==================================== BORROWING HISTORY ====================================");
        System.out.printf(
            "%-4s %-18s %-20s %-6s %-12s %-12s %-12s %-10s%n",
            "ID", "MEMBER", "BOOK", "COPY",
            "BORROWED", "DUE DATE", "RETURNED", "STATUS"
        );

        System.out.println("-".repeat(100));

        for (Borrow borrow : borrowings) {

            String member = borrow.getMember().getFullName();
            String book = borrow.getBookCopy().getBook().getTitle();

            System.out.printf(
                "%-4d %-18.18s %-20.20s %-6d %-12s %-12s %-12s %-10s%n",
                borrow.getBorrowId(),
                member,
                book,
                borrow.getBookCopy().getCopyId(),
                borrow.getBorrowDate(),
                borrow.getExpectedReturnDate(),
                borrow.getActualReturnDate() == null
                    ? "-"
                    : borrow.getActualReturnDate(),
                borrow.getStatus()
            );
        }

        System.out.println("-".repeat(100));
        System.out.println("Total borrowing records: " + borrowings.size());
    }
    /**
     * Displays all borrowing records retrieved from MariaDB.
     */
    public void displayAllBorrowings() throws SQLException {

        ArrayList<Borrow> result = borrowRepository.findAllBorrowings();

        if (result.isEmpty()) {
            System.out.println("No borrowing records found.");
            return;
        }

        displayBorrowingTable(result);
    }

    /**
     * Finds all borrowing records belonging to a specific member.
     */
    public ArrayList<Borrow> findBorrowingsByMember(int memberId)
            throws SQLException {

        ArrayList<Borrow> databaseResults =
                borrowRepository.findBorrowingsByMember(memberId);

        ArrayList<Borrow> result = new ArrayList<>();

        for (Borrow databaseBorrow : databaseResults) {

            Borrow cachedBorrow = null;

            for (Borrow current : borrows) {
                if (current.getBorrowId() == databaseBorrow.getBorrowId()) {
                    cachedBorrow = current;
                    break;
                }
            }

            if (cachedBorrow != null) {
                result.add(cachedBorrow);
            } else {
                borrows.add(databaseBorrow);
                result.add(databaseBorrow);
            }
        }

        return result;
    }

    /**
     * Updates overdue statuses, then returns every overdue borrowing.
     */
    public ArrayList<Borrow> findOverdueBorrowings() throws SQLException {

        checkOverdueBorrowings();

        ArrayList<Borrow> result = new ArrayList<>();

        for (Borrow databaseBorrow : borrowRepository.findAllBorrowings()) {
            if (databaseBorrow.getStatus() == BorrowStatus.OVERDUE) {
                result.add(databaseBorrow);
            }
        }

        return result;
    }

    /**
     * Checks whether a member currently holds (active or overdue)
     * a copy of the given book.
     */
    public boolean hasOutstandingBorrowOfBook(int memberId, int bookId)
            throws SQLException {

        for (Borrow borrow : findBorrowingsByMember(memberId)) {
            if (borrow.getStatus() != BorrowStatus.RETURNED
                    && borrow.getBookCopy().getBook().getBookId() == bookId) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the total number of borrowing records.
     */
    public int getTotalBorrowings() throws SQLException {
        return borrowRepository.countBorrowings();
    }
    
    /**
     * Displays the borrowing history of a specific member.
     */
    public void displayBorrowingsByMember(int memberId)
            throws SQLException {

        ArrayList<Borrow> result = findBorrowingsByMember(memberId);

        if (result.isEmpty()) {
            System.out.println("This member has no borrowing history.");
            return;
        }

        displayBorrowingTable(result);
    }

    /**
     * Collects a reserved book copy and creates a new borrowing record.
     */
    public Borrow collectReservation(
            Reservation reservation,
            LocalDate expectedReturnDate) throws SQLException {

        // Validate inputs
        if (reservation == null || expectedReturnDate == null) {
            throw new IllegalArgumentException(
                    "Reservation and expected return date cannot be null");
        }

        // Verify ownership
        if (reservationService.findReservationById(
                reservation.getReservationId()) != reservation) {

            throw new IllegalArgumentException(
                    "Reservation does not belong to this service");
        }

        Member member = reservation.getMember();
        BookCopy copy = reservation.getAssignedCopy();

        if (copy == null) {
            throw new IllegalStateException(
                    "No book copy assigned to this reservation");
        }

        // Validate reservation status
        if (reservation.getStatus() != ReservationStatus.READY_FOR_PICKUP ||
                copy.getStatus() != CopyStatus.RESERVED) {

            throw new IllegalStateException(
                    "Reservation cannot be collected");
        }

        // Check the pickup deadline
        if (reservation.getExpirationDate() == null ||
                LocalDate.now().isAfter(reservation.getExpirationDate())) {

            throw new IllegalStateException(
                    "Reservation has expired");
        }

        // Validate expected return date
        if (!expectedReturnDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Expected return date must be after today");
        }

        // Count outstanding borrowings directly from MariaDB
        int activeBorrowings =
                borrowRepository.countOutstandingBorrowings(
                        member.getMemberId());

        if (activeBorrowings >= 3) {
            throw new IllegalStateException(
                    "Member has reached the borrowing limit");
        }

        // Execute the database transaction and receive its generated ID
        int borrowId = borrowRepository.collectReservation(
                reservation.getReservationId(),
                member,
                copy,
                expectedReturnDate
        );

        // Reconstruct the actual Borrow object with the database-generated ID
        Borrow borrow = new Borrow(
                borrowId,
                member,
                copy,
                expectedReturnDate
        );

        // Synchronize the reservation and physical copy
        reservation.fulfill();

        // Store the borrowing record in memory
        borrows.add(borrow);

        return borrow;
    }
}