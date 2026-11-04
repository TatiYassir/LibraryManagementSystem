package com.library.model;

import java.time.LocalDate;

import com.library.enums.ReservationStatus;
import com.library.enums.CopyStatus;

public class Reservation {

    // Attributes
    private final int reservationId;
    private final Member member;
    private final Book book;
    private final LocalDate reservationDate;
    private ReservationStatus status;
    private LocalDate expirationDate;
    private BookCopy assignedCopy;

    // Constructor
    /**
     * Creates a new reservation for a member and a book.
     * A new reservation starts with WAITING status.
     *
     * @param reservationId unique identifier of the reservation
     * @param member the member making the reservation
     * @param book the book being reserved
     */
    public Reservation(int reservationId, Member member, Book book) {
        if (member == null || book == null) {
            throw new IllegalArgumentException("Member and book cannot be null");
        }

        this.reservationId = reservationId;
        this.member = member;
        this.book = book;
        this.reservationDate = LocalDate.now();
        this.status = ReservationStatus.WAITING;
    }

    // Getters
    public int getReservationId() {
        return reservationId;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public Book getBook() {
        return book;
    }

    public BookCopy getAssignedCopy() {
        return assignedCopy;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    // Methods

    /**
     * Assigns an available physical copy to this reservation.
     * Changes the copy status to RESERVED and the reservation
     * status to READY_FOR_PICKUP.
     * The member has three days to collect the book.
     *
     * @param copy the available physical copy assigned to the reservation
     */
    public void assignCopy(BookCopy copy) {
        // Check that the copy is not null
        if (copy == null) {
            throw new IllegalArgumentException("Assigned book cannot be null");
        }
        // Ensure the physical copy belongs to the reserved book
        if (copy.getBook().getBookId() != book.getBookId()) {
            throw new IllegalArgumentException(
                "Copy does not belong to this book"
            );
        }
        // Ensure the reservation is waiting and the copy is available
        if (assignedCopy != null ||
                status != ReservationStatus.WAITING ||
                copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot assign this copy");
        }

        assignedCopy = copy;
        copy.setStatus(CopyStatus.RESERVED);
        status = ReservationStatus.READY_FOR_PICKUP;
        expirationDate = LocalDate.now().plusDays(3);
    }

    /**
     * Confirms that the member has collected the reserved book.
     * Changes the reservation status to FULFILLED and the
     * assigned copy status to BORROWED.
     * The reservation must still be valid when collected.
     */
    public void fulfill() {
        // Validate reservation and assigned copy
        if (assignedCopy == null ||
                status != ReservationStatus.READY_FOR_PICKUP ||
                assignedCopy.getStatus() != CopyStatus.RESERVED) {
            throw new IllegalStateException("Reservation cannot be fulfilled");
        }

        // Check whether the reservation has expired
        if (LocalDate.now().isAfter(expirationDate)) {
            throw new IllegalStateException("Reservation has expired");
        }

        // Mark reservation as fulfilled
        status = ReservationStatus.FULFILLED;

        // Update the physical copy status
        assignedCopy.setStatus(CopyStatus.BORROWED);
    }

    /**
     * Cancels a reservation that is either waiting or ready for pickup.
     * If a copy has already been assigned, it becomes available again.
     * The reservation record is preserved for historical purposes.
     */
    public void cancel() {
    	// Only WAITING and READY_FOR_PICKUP reservations can be cancelled
    	if (status != ReservationStatus.WAITING &&
    	        status != ReservationStatus.READY_FOR_PICKUP) {
    	    throw new IllegalStateException("Reservation cannot be cancelled");
    	}
    	// Verify that the assigned copy is still reserved
    	if (assignedCopy != null &&
    	        assignedCopy.getStatus() != CopyStatus.RESERVED) {
    	    throw new IllegalStateException(
    	        "Assigned copy is not reserved"
    	    );
    	}

    	// Release the assigned copy if one exists
    	if (assignedCopy != null) {
    	    assignedCopy.setStatus(CopyStatus.AVAILABLE);
    	}

    	status = ReservationStatus.CANCELLED;
    }

    /**
     * Marks a reservation as expired when the member fails to
     * collect the book before the expiration date.
     * The assigned copy becomes available again.
     */
    public void expire() {
        // Validate reservation and assigned copy
        if (assignedCopy == null ||
                status != ReservationStatus.READY_FOR_PICKUP ||
                assignedCopy.getStatus() != CopyStatus.RESERVED) {
            throw new IllegalStateException("Reservation cannot expire");
        }

        // The reservation expires only after its expiration date
        if (!LocalDate.now().isAfter(expirationDate)) {
            throw new IllegalStateException("Reservation has not expired yet");
        }

        // Update reservation status
        status = ReservationStatus.EXPIRED;

        // Release the physical copy
        assignedCopy.setStatus(CopyStatus.AVAILABLE);
    }
    
    @Override
    public String toString() {
        return "Reservation ID: " + reservationId +
               "\nMember: " + member.getFullName() +
               "\nBook title: " + book.getTitle() +
               "\nReservation date: " + reservationDate +
               "\nExpiration date: " + expirationDate +
               "\nReservation status: " + status +
               "\nAssigned copy ID: " +
               (assignedCopy == null ? "Not assigned yet" : assignedCopy.getCopyId());
    }
}









