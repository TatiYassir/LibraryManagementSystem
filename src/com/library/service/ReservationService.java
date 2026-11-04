package com.library.service;

import java.util.ArrayList;

import com.library.model.Member;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Reservation;
import com.library.enums.CopyStatus;
import com.library.enums.ReservationStatus;

public class ReservationService {

    // Attributes
	// Stores all reservation records, including completed and cancelled ones
	private final ArrayList<Reservation> reservations = new ArrayList<>();

	// ID assigned to the next reservation
	private int nextReservationId = 1;

    // Methods

    /**
     * Creates and stores a new reservation for a member.
     * Each reservation receives a unique ID and starts with WAITING status.
     *
     * @param member the member making the reservation
     * @param book the book being reserved
     * @return the newly created reservation
     */
    public Reservation reserve(Member member, Book book) {

        // Validate input
        if (member == null || book == null) {
            throw new IllegalArgumentException("Member and book cannot be null");
        }

        // Create a new reservation
        Reservation reservation = new Reservation(
                nextReservationId,
                member,
                book
        );

        // Store the reservation
        reservations.add(reservation);

        // Move to the next reservation ID
        nextReservationId++;

        return reservation;
    }

    /**
     * Searches for a reservation using its unique ID.
     *
     * @param reservationId the ID of the reservation to find
     * @return the matching reservation, or null if not found
     */
    public Reservation findReservationById(int reservationId) {
        for (Reservation reservation : reservations) {
            if (reservationId == reservation.getReservationId()) {
                return reservation;
            }
        }

        return null;
    }

    /**
     * Displays all reservation records stored in the service.
     * Displays a message if no reservations exist.
     */
    public void displayAllReservations() {
        if (reservations.isEmpty()) {
            System.out.println("No reservation records found.");
            return;
        }

        for (Reservation reservation : reservations) {
            System.out.println(reservation);
        }
    }

    /**
     * Finds all reservations belonging to a specific member.
     *
     * @param memberId the ID of the member
     * @return a new list containing the member's reservations
     */
    public ArrayList<Reservation> findReservationsByMember(int memberId) {
        ArrayList<Reservation> result = new ArrayList<>();

        for (Reservation reservation : reservations) {
            if (memberId == reservation.getMember().getMemberId()) {
            	// Store matching reservations in a separate result list
                result.add(reservation);
            }
        }

        return result;
    }

    /**
     * Displays the reservation history of a specific member.
     *
     * @param memberId the ID of the member
     */
    public void displayReservationsByMember(int memberId) {
        ArrayList<Reservation> result = findReservationsByMember(memberId);

        if (result.isEmpty()) {
            System.out.println("This member has no reservation history.");
            return;
        }

        for (Reservation reservation : result) {
            System.out.println(reservation);
        }
    }

    /**
     * Returns the total number of reservation records.
     *
     * @return the number of reservations stored
     */
    public int getTotalReservations() {
        return reservations.size();
    }
    /**
     * Assigns an available book copy to the first waiting reservation
     * for the specified book.
     * Reservations are processed in FIFO order.
     *
     * @param book the book associated with the reservations
     * @param copy the available physical copy to assign
     * @return true if a reservation was assigned, false if none was waiting
     * @throws IllegalArgumentException if the book or copy is null,
     *         or if the copy does not belong to the specified book
     * @throws IllegalStateException if the copy is not available
     */
    public boolean assignNextReservation(Book book, BookCopy copy) {
    	if ( book == null || copy == null) {
            throw new IllegalArgumentException("book and copy cannot be null");
        }
    	if (copy.getStatus() != CopyStatus.AVAILABLE) {
    	    throw new IllegalStateException("This copy is not available");
    	}
    	if (copy.getBook().getBookId() != book.getBookId()) {
    	    throw new IllegalArgumentException("This copy does not belong to this book");
    	}
    	for (Reservation reservation : reservations) {
    	    if (reservation.getBook().getBookId() == book.getBookId() &&
    	    		reservation.getStatus() == ReservationStatus.WAITING) {
    	        reservation.assignCopy(copy);

    	        return true; // Stop here: we assigned the copy.
    	    }
    	}
    	return false;
    }
    /**
     * Cancels a reservation and, if it had an assigned book copy,
     * assigns that copy to the next waiting reservation.
     *
     * The cancelled reservation remains in the reservation history.
     *
     * @param reservation the reservation to cancel
     * @throws IllegalArgumentException if the reservation is null
     *         or does not belong to this service
     * @throws IllegalStateException if the reservation cannot be cancelled
     */
    public void cancelReservation(Reservation reservation) {

        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }

        // Make sure this reservation belongs to this service
        if (findReservationById(reservation.getReservationId()) != reservation) {
            throw new IllegalArgumentException(
                "Reservation does not belong to this service"
            );
        }

        // Keep a reference to the copy before cancelling
        BookCopy copy = reservation.getAssignedCopy();

        // Cancel the reservation and release its copy
        reservation.cancel();

        // If a copy was released, offer it to the next waiting member
        if (copy != null) {
            assignNextReservation(reservation.getBook(), copy);
        }
    }
    /**
     * Expires a reservation and, if it had an assigned book copy,
     * assigns that copy to the next waiting reservation.
     *
     * The expired reservation remains in the reservation history.
     *
     * @param reservation the reservation to expire
     * @throws IllegalArgumentException if the reservation is null
     *         or does not belong to this service
     * @throws IllegalStateException if the reservation cannot expire
     */
    public void expireReservation(Reservation reservation) {

        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }

        // Verify that this reservation belongs to this service
        if (findReservationById(reservation.getReservationId()) != reservation) {
            throw new IllegalArgumentException(
                "Reservation does not belong to this service"
            );
        }

        // Keep a reference to the assigned copy
        BookCopy copy = reservation.getAssignedCopy();

        // Expire the reservation and release its copy
        reservation.expire();

        // Assign the released copy to the next waiting member
        if (copy != null) {
            assignNextReservation(reservation.getBook(), copy);
        }
    }
}





















