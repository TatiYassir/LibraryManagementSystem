package com.library.service;

import java.util.ArrayList;
import java.time.LocalDate;

import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.model.Reservation;

import com.library.enums.CopyStatus;
import com.library.enums.BorrowStatus;

public class BorrowService {

    // Attributes

    private int nextBorrowId = 1;
    private final ReservationService reservationService;
    private final ArrayList<Borrow> borrows = new ArrayList<>();
    
    // Constructor :
    /**
     * Creates a borrowing service connected to a reservation service.
     *
     * @param reservationService the service used to assign returned copies
     *                           to waiting reservations
     * @throws IllegalArgumentException if the service is null
     */
    public BorrowService (ReservationService reservationService ) {
    	if (reservationService == null) {
    	    throw new IllegalArgumentException("ReservationService cannot be null");
    	}
    	this.reservationService = reservationService;
    }
    // Methods

    /**
     * Borrows a book copy for a member.
     * Checks copy availability and enforces the maximum
     * of three outstanding borrowings per member.
     *
     * @param member the member requesting the book
     * @param copy the physical book copy to borrow
     * @param expectedReturnDate the expected return date
     * @return the newly created borrowing record
     */
    public Borrow borrowBook(Member member, BookCopy copy,
                             LocalDate expectedReturnDate) {

        if (member == null || copy == null) {
            throw new IllegalArgumentException("Member and copy cannot be null");
        }

     // Check whether the requested copy is available
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new IllegalStateException("This copy is not available");
        }

     // Count how many active borrowings belong to this member
        int count = 0;

        for (Borrow borrow : borrows) {
            if (borrow.getMember().getMemberId() == member.getMemberId()
                    &&( borrow.getStatus() == BorrowStatus.ACTIVE 
                    || borrow.getStatus() == BorrowStatus.OVERDUE)) {
                count++;
            }
        }

        // Enforce the maximum borrowing limit
        if (count >= 3) {
            throw new IllegalStateException(
            		"Member has reached the borrowing limit");
        }

        // Create and register the new borrowing record
        Borrow borrow = new Borrow(
                nextBorrowId,
                member,
                copy,
                expectedReturnDate
        );

        // Update the copy's status and register the borrowing
        copy.setStatus(CopyStatus.BORROWED);
        borrows.add(borrow);
        nextBorrowId++;

        return borrow;
    }
// Methods :
    /**
     * Returns a borrowed book copy.
     * Verifies that the borrowing belongs to this service,
     * updates its status, and handles waiting reservations.
     *
     * @param borrow the borrowing record to return
     */
    public void returnBook(Borrow borrow) {

        if (borrow == null) {
            throw new IllegalArgumentException("Borrow cannot be null");
        }

        // Verify that this exact borrowing object belongs to this service
        if (findBorrowById(borrow.getBorrowId()) != borrow) {
            throw new IllegalArgumentException(
                "Borrowing does not belong to this service"
            );
        }

        // Update the borrowing record
        borrow.returnBook();

        // Make the physical copy available
        BookCopy copy = borrow.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);

        // Assign it to the next waiting reservation, if one exists
        reservationService.assignNextReservation(copy.getBook(), copy);
    }
    
    /**
     * Checks all active borrowings and marks them as overdue
     * when their expected return date has passed.
     */
    public void checkOverdueBorrowings() {
    	for (Borrow borrow : borrows) {
    		if (borrow.getStatus() == BorrowStatus.ACTIVE
    		        && LocalDate.now().isAfter(borrow.getExpectedReturnDate())) {
    			borrow.markAsOverdue();
    		}
    	}
    }
    
    /**
     * Searches for a borrowing record using its unique ID.
     *
     * @param borrowId the ID of the borrowing record
     * @return the matching borrowing record, or null if not found
     */
    public Borrow findBorrowById(int borrowId) {

        for (Borrow borrow : borrows) {

            if (borrowId == borrow.getBorrowId()) {
                return borrow;
            }

        }

        return null;
    }
    
    /**
     * Displays all borrowing records stored in the service.
     * Displays a message if no borrowing records exist.
     */
    public void displayAllBorrowings() {

        // Check whether any borrowing records exist
        if (borrows.isEmpty()) {
            System.out.println("No borrowing records found.");
            return;
        }

        // Display all borrowing records
        for (Borrow borrow : borrows) {
            System.out.println(borrow);
        }
    }
    
    /**
     * Finds all borrowing records belonging to a specific member.
     *
     * @param memberId the ID of the member
     * @return a new list containing the member's borrowing records
     */
    public ArrayList<Borrow> findBorrowingsByMember(int memberId) {
    	ArrayList<Borrow> result = new ArrayList<>();
    	for (Borrow borrow : borrows) {
    		if (memberId == borrow.getMember().getMemberId()) {
    			result.add(borrow);
            }
    	}
    	return result;
    }
    /**
     * Returns the total number of borrowing records.
     *
     * @return the number of borrowing records stored
     */
    public int getTotalBorrowings() {
        return borrows.size();
    }
    
    /**
     * Displays the borrowing history of a specific member.
     *
     * @param memberId the ID of the member
     */
    public void displayBorrowingsByMember(int memberId) {
    	ArrayList<Borrow> result = findBorrowingsByMember(memberId);
    	if (result.isEmpty()) {
    	    System.out.println("This member has no borrowing history.");
    	    return;
    	}
    	for (Borrow borrow : result) {
    	    System.out.println(borrow);
    	}
    }
    /**
     * Collects a reserved book copy and creates a new borrowing record.
     * Checks the member's borrowing limit before fulfilling the reservation.
     *
     * @param reservation the reservation being collected
     * @param expectedReturnDate the expected return date
     * @return the newly created borrowing record
     * @throws IllegalArgumentException if the reservation is null
     * @throws IllegalStateException if no copy is assigned, the borrowing
     *         limit is reached, or the reservation cannot be fulfilled
     */
    public Borrow collectReservation(Reservation reservation,
            LocalDate expectedReturnDate) {
    	 // 1. Validate the reservation
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }

        // 2. Verify ownership
        if (reservationService.findReservationById(
                reservation.getReservationId()) != reservation) {
            throw new IllegalArgumentException(
                "Reservation does not belong to this service"
            );
        }

        // 3. Retrieve the reservation's data
        Member member = reservation.getMember();
        BookCopy copy = reservation.getAssignedCopy();
        // A reservation must have an assigned copy before collection
        if (copy == null) {
            throw new IllegalStateException(
                "No book copy assigned to this reservation"
            );
        }
    	int activeBorrowings = 0;

    	for (Borrow borrow : borrows) {
    		if (borrow.getMember().getMemberId() == member.getMemberId()
                    &&( borrow.getStatus() == BorrowStatus.ACTIVE 
                    || borrow.getStatus() == BorrowStatus.OVERDUE)) {
    			activeBorrowings++;
    		}
    	}
    	if (activeBorrowings >= 3) {
    	    throw new IllegalStateException(
    	        "Member has reached the borrowing limit");
    	}
    	Borrow borrow = new Borrow(
    		    nextBorrowId,
    		    member,
    		    copy,
    		    expectedReturnDate
    		);
    	reservation.fulfill();

    	// Store the new borrowing record
    	borrows.add(borrow);

    	// Move to the next borrowing ID
    	nextBorrowId++;

    	return borrow;
    }
}
