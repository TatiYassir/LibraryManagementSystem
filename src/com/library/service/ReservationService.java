package com.library.service;

import java.util.ArrayList;

import com.library.model.Member;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Reservation;
import com.library.model.ExpiredReservationResult;
import com.library.enums.CopyStatus;
import com.library.enums.ReservationStatus;
import java.sql.SQLException;
import java.time.LocalDate;
import com.library.repository.ReservationRepository;
import com.library.repository.BookCopyRepository;

public class ReservationService {

    // Attributes
    // Stores all reservation records, including completed and cancelled ones
    private final ArrayList<Reservation> reservations = new ArrayList<>();

    // Handles reservation database operations
    private final ReservationRepository reservationRepository =
            new ReservationRepository();

    // Used to find a free copy when a reservation is created
    private final BookCopyRepository bookCopyRepository =
            new BookCopyRepository();

    // Methods

    /**
     * Creates and stores a new reservation for a member.
     * MariaDB generates the reservation ID.
     *
     * @param member the member making the reservation
     * @param book the book being reserved
     * @return the newly created reservation
     */
    public Reservation reserve(Member member, Book book)
            throws SQLException {

        // Validate input
        if (member == null || book == null) {
            throw new IllegalArgumentException(
                    "Member and book cannot be null");
        }

        // A member cannot hold two open reservations for the same book
        for (Reservation existing :
                findReservationsByMember(member.getMemberId())) {

            boolean open =
                    existing.getStatus() == ReservationStatus.WAITING
                    || existing.getStatus() == ReservationStatus.READY_FOR_PICKUP;

            if (open && existing.getBook().getBookId() == book.getBookId()) {
                throw new IllegalStateException(
                        "This member already has an open reservation for this book");
            }
        }

        // Get the reservation date
        LocalDate reservationDate = LocalDate.now();

        // Save the reservation in the database
        int reservationId = reservationRepository.addReservation(
                member, book, reservationDate);

        // Create the Java object using the database-generated ID
        Reservation reservation = new Reservation(
                reservationId,
                member,
                book,
                reservationDate
        );

        // Store the reservation in memory
        reservations.add(reservation);

        // If copies are free right now, hand them to the waiting queue
        // (first come, first served) instead of leaving them idle
        for (BookCopy copy :
                bookCopyRepository.findCopiesByBookId(book.getBookId())) {

            if (copy.getStatus() == CopyStatus.AVAILABLE
                    && !assignNextReservation(book, copy)) {
                break;
            }
        }

        return reservation;
    }

    /**
     * Expires every READY_FOR_PICKUP reservation whose pickup deadline
     * has passed, releasing or reassigning its held copy.
     *
     * @return the results of all expired reservations
     */
    public ArrayList<ExpiredReservationResult> expireOverdueReservations()
            throws SQLException {

        ArrayList<ExpiredReservationResult> expiredResults =
                new ArrayList<>();

        for (Reservation databaseReservation :
                reservationRepository.findAllReservations()) {

            if (databaseReservation.getStatus()
                    != ReservationStatus.READY_FOR_PICKUP) {
                continue;
            }

            LocalDate deadline = databaseReservation.getExpirationDate();

            if (deadline == null || !LocalDate.now().isAfter(deadline)) {
                continue;
            }

            Reservation tracked =
                    findReservationById(databaseReservation.getReservationId());

            // Expire the reservation and check whether its copy was reassigned
            boolean copyReassigned = expireReservation(tracked);

            // Keep the reservation and reassignment result together
            expiredResults.add(
                    new ExpiredReservationResult(tracked, copyReassigned)
            );
        }

        return expiredResults;
    }

    /**
     * Searches for a reservation using its unique ID.
     *
     * First searches the in-memory list.
     * If not found, searches the database.
     *
     * @param reservationId the ID of the reservation
     * @return the matching reservation, or null if not found
     */
    public Reservation findReservationById(int reservationId)
            throws SQLException {

        // First, search the in-memory list
        for (Reservation reservation : reservations) {
            if (reservationId == reservation.getReservationId()) {
                return reservation;
            }
        }

        // If not found in memory, search MariaDB
        Reservation reservation =
                reservationRepository.findReservationById(reservationId);

        // Cache the retrieved reservation
        if (reservation != null) {
            reservations.add(reservation);
        }

        return reservation;
    }

    /**
     * Displays reservation records in a clean, aligned console table.
     */
    private void displayReservationTable(ArrayList<Reservation> reservationList) {

        System.out.println();
        System.out.println("==================================== RESERVATION HISTORY ====================================");
        System.out.printf(
            "%-4s %-18s %-20s %-8s %-12s %-12s %-18s%n",
            "ID", "MEMBER", "BOOK", "COPY",
            "RESERVED", "EXPIRATION", "STATUS"
        );

        System.out.println("-".repeat(96));

        for (Reservation res : reservationList) {

            String member = res.getMember().getFullName();
            String book = res.getBook().getTitle();
            String copyIdStr = (res.getAssignedCopy() == null)
                    ? "-"
                    : String.valueOf(res.getAssignedCopy().getCopyId());

            System.out.printf(
                "%-4d %-18.18s %-20.20s %-8s %-12s %-12s %-18s%n",
                res.getReservationId(),
                member,
                book,
                copyIdStr,
                res.getReservationDate(),
                res.getExpirationDate() == null ? "-" : res.getExpirationDate(),
                res.getStatus()
            );
        }

        System.out.println("-".repeat(96));
        System.out.println("Total reservation records: " + reservationList.size());
    }

    /**
     * Displays all reservation records stored in MariaDB.
     */
    public void displayAllReservations() throws SQLException {

        // Retrieve all reservations from the database
        ArrayList<Reservation> databaseReservations =
                reservationRepository.findAllReservations();

        if (databaseReservations.isEmpty()) {
            System.out.println("No reservation records found.");
            return;
        }

        ArrayList<Reservation> toDisplay = new ArrayList<>();

        for (Reservation databaseReservation : databaseReservations) {

            Reservation reservationToDisplay = databaseReservation;

            // Check whether this reservation is already cached
            for (Reservation cachedReservation : reservations) {

                if (cachedReservation.getReservationId()
                        == databaseReservation.getReservationId()) {

                    reservationToDisplay = cachedReservation;
                    break;
                }
            }

            // Cache it if it is not already in memory
            if (!reservations.contains(reservationToDisplay)) {
                reservations.add(reservationToDisplay);
            }

            toDisplay.add(reservationToDisplay);
        }

        displayReservationTable(toDisplay);
    }

    /**
     * Finds all reservations belonging to a specific member.
     *
     * @param memberId the ID of the member
     * @return a new list containing the member's reservations
     */
    public ArrayList<Reservation> findReservationsByMember(int memberId)
            throws SQLException {

        // Retrieve the member's reservations from MariaDB
        ArrayList<Reservation> databaseReservations =
                reservationRepository.findReservationsByMember(memberId);

        // Build the result using the service's existing objects when available
        ArrayList<Reservation> result = new ArrayList<>();

        for (Reservation databaseReservation : databaseReservations) {

            Reservation existingReservation = null;

            // Check whether this reservation is already cached
            for (Reservation cachedReservation : reservations) {

                if (cachedReservation.getReservationId()
                        == databaseReservation.getReservationId()) {

                    existingReservation = cachedReservation;
                    break;
                }
            }

            if (existingReservation != null) {
                result.add(existingReservation);
            } else {
                reservations.add(databaseReservation);
                result.add(databaseReservation);
            }
        }

        return result;
    }

    /**
     * Displays the reservation history of a specific member.
     *
     * @param memberId the ID of the member
     */
    public void displayReservationsByMember(int memberId)
            throws SQLException {
        ArrayList<Reservation> result = findReservationsByMember(memberId);

        if (result.isEmpty()) {
            System.out.println("This member has no reservation history.");
            return;
        }

        displayReservationTable(result);
    }

    /**
     * Returns the total number of reservation records in MariaDB.
     *
     * @return the total number of reservations
     */
    public int getTotalReservations() throws SQLException {
        return reservationRepository.countReservations();
    }

    /**
     * Assigns an available copy to the first waiting reservation
     * and saves the changes in MariaDB.
     *
     * @param book the book associated with the reservations
     * @param copy the available physical copy
     * @return true if a reservation was assigned, false otherwise
     */
    public boolean assignNextReservation(Book book, BookCopy copy)
            throws SQLException {

        // Validate input
        if (book == null || copy == null) {
            throw new IllegalArgumentException(
                    "Book and copy cannot be null");
        }

        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "This copy is not available");
        }

        if (copy.getBook().getBookId() != book.getBookId()) {
            throw new IllegalArgumentException(
                    "This copy does not belong to this book");
        }

        // Calculate the pickup expiration date
        LocalDate expirationDate = LocalDate.now().plusDays(3);

        // Assign the copy in MariaDB
        int reservationId =
                reservationRepository.assignNextWaitingReservation(
                        book.getBookId(),
                        copy.getCopyId(),
                        expirationDate
                );

        // No waiting reservation was found
        if (reservationId == -1) {
            return false;
        }

        // Find the reservation in the in-memory list
        Reservation reservation = null;

        for (Reservation current : reservations) {
            if (current.getReservationId() == reservationId) {
                reservation = current;
                break;
            }
        }

        if (reservation != null) {

            // Synchronize the existing Java objects
            reservation.assignCopy(copy, expirationDate);

        } else {

            // Retrieve the updated reservation from MariaDB
            reservation = reservationRepository.findReservationById(
                    reservationId);

            if (reservation == null) {
                throw new SQLException(
                        "Assigned reservation could not be retrieved");
            }

            reservations.add(reservation);

            // Synchronize the copy object supplied to this method
            copy.setStatus(CopyStatus.RESERVED);
        }

        return true;
    }

    /**
     * Cancels a reservation and reassigns its copy if another
     * reservation is waiting for the same book.
     */
    public void cancelReservation(Reservation reservation)
            throws SQLException {

        if (reservation == null) {
            throw new IllegalArgumentException(
                    "Reservation cannot be null");
        }

        // Verify that the reservation belongs to this service
        if (findReservationById(reservation.getReservationId())
                != reservation) {
            throw new IllegalArgumentException(
                    "Reservation does not belong to this service");
        }

        // Validate its current state before changing the database
        if (reservation.getStatus() != ReservationStatus.WAITING &&
                reservation.getStatus() != ReservationStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException(
                    "Reservation cannot be cancelled");
        }

        BookCopy copy = reservation.getAssignedCopy();

        if (copy != null && copy.getStatus() != CopyStatus.RESERVED) {
            throw new IllegalStateException(
                    "Assigned copy is not reserved");
        }

        // Calculate the next pickup expiration date
        LocalDate expirationDate = LocalDate.now().plusDays(3);

        // Perform cancellation and possible reassignment in MariaDB
        int nextReservationId =
                reservationRepository.cancelReservationAndReassign(
                        reservation.getReservationId(),
                        expirationDate
                );

        // Synchronize the cancelled reservation in memory
        reservation.cancel();

        // No copy was reassigned
        if (nextReservationId == -1) {
            return;
        }

        // Find the next reservation in memory
        Reservation nextReservation = null;

        for (Reservation current : reservations) {
            if (current.getReservationId() == nextReservationId) {
                nextReservation = current;
                break;
            }
        }

        if (nextReservation != null) {

            // Assign the released copy to the existing Java object
            nextReservation.assignCopy(copy, expirationDate);

        } else {

            // Synchronize the physical copy
            copy.setStatus(CopyStatus.RESERVED);

            // Retrieve the newly assigned reservation from MariaDB
            nextReservation =
                    reservationRepository.findReservationById(
                            nextReservationId);

            if (nextReservation == null) {
                throw new SQLException(
                        "Next reservation could not be retrieved");
            }

            reservations.add(nextReservation);
        }
    }

    /**
     * Expires a reservation and reassigns its copy if another
     * reservation is waiting for the same book.
     */
    public boolean expireReservation(Reservation reservation)
            throws SQLException {

        if (reservation == null) {
            throw new IllegalArgumentException(
                    "Reservation cannot be null");
        }

        // Verify that the reservation belongs to this service
        if (findReservationById(reservation.getReservationId())
                != reservation) {
            throw new IllegalArgumentException(
                    "Reservation does not belong to this service");
        }

        // Validate the reservation state
        if (reservation.getStatus() != ReservationStatus.READY_FOR_PICKUP ||
                reservation.getAssignedCopy() == null) {
            throw new IllegalStateException(
                    "Reservation cannot expire");
        }

        BookCopy copy = reservation.getAssignedCopy();

        if (copy.getStatus() != CopyStatus.RESERVED) {
            throw new IllegalStateException(
                    "Assigned copy is not reserved");
        }

        LocalDate expirationDate = reservation.getExpirationDate();

        if (expirationDate == null ||
                !LocalDate.now().isAfter(expirationDate)) {
            throw new IllegalStateException(
                    "Reservation has not expired yet");
        }

        // Calculate the next pickup deadline
        LocalDate nextExpirationDate = LocalDate.now().plusDays(3);

        // Expire the reservation and reassign its copy in MariaDB
        int nextReservationId =
                reservationRepository.expireReservationAndReassign(
                        reservation.getReservationId(),
                        nextExpirationDate
                );

        // Synchronize the expired reservation in memory
        reservation.expire();

        // No other waiting reservation exists
        if (nextReservationId == -1) {
            return false;
        }

        // Find the next reservation in memory
        Reservation nextReservation = null;

        for (Reservation current : reservations) {
            if (current.getReservationId() == nextReservationId) {
                nextReservation = current;
                break;
            }
        }

        if (nextReservation != null) {

            // Assign the released copy to the existing Java object
            nextReservation.assignCopy(copy, nextExpirationDate);

        } else {

            // Synchronize the physical copy
            copy.setStatus(CopyStatus.RESERVED);

            // Retrieve the newly assigned reservation from MariaDB
            nextReservation =
                    reservationRepository.findReservationById(
                            nextReservationId);

            if (nextReservation == null) {
                throw new SQLException(
                        "Next reservation could not be retrieved");
            }

            reservations.add(nextReservation);
        }

        // A waiting reservation received the copy
        return true;
    }
}