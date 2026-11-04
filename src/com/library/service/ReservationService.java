package com.library.service;

import java.util.ArrayList;

import com.library.model.Member;
import com.library.model.Book;
import com.library.model.Reservation;

public class ReservationService {

    // Attributes
    private int nextReservationId = 1;
    private final ArrayList<Reservation> reservations = new ArrayList<>();

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

        // Prepare the next ID
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
}