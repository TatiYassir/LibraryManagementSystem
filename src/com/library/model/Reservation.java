package com.library.model;

import java.util.ArrayList;
import java.time.LocalDate;
import com.library.enums.ReservationStatus;
import com.library.enums.CopyStatus;

public class Reservation {
	//Attributes :
	private final int reservationId;
	private final Member member;
	private final Book book;
	private final LocalDate reservationDate;
	private ReservationStatus status;
	private LocalDate expirationDate;
	private BookCopy assignedCopy;
	// Constructor :
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
	
	// getter :
	
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
	
	// Methods :
	public void assignCopy(BookCopy copy) {

	    // 1. Check that the copy is not null
		if (copy == null) {
			throw new IllegalArgumentException("assigned book cannot be null"); 
		}
	    // 2. Check that this reservation doesn't already have a copy
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
	
	public void fulfill() {

	    // 1. Validate reservation and assigned copy
	    if (assignedCopy == null ||
	            status != ReservationStatus.READY_FOR_PICKUP ||
	            assignedCopy.getStatus() != CopyStatus.RESERVED) {

	        throw new IllegalStateException("Reservation cannot be fulfilled");
	    }

	    // 2. Check whether the reservation has expired
	    if (LocalDate.now().isAfter(expirationDate)) {
	        throw new IllegalStateException("Reservation has expired");
	    }

	    // 3. Mark reservation as fulfilled
	    status = ReservationStatus.FULFILLED;

	    // 4. Update the physical copy status
	    assignedCopy.setStatus(CopyStatus.BORROWED);
	}
	
	public void cancel() {
		if (status != ReservationStatus.WAITING &&
		        status != ReservationStatus.READY_FOR_PICKUP) {

		    throw new IllegalStateException("Reservation cannot be cancelled");
		}
		if (assignedCopy != null) {
			assignedCopy.setStatus(CopyStatus.AVAILABLE);
		}
		status = ReservationStatus.CANCELLED;
	}
	
	public void expire() {

	    // 1. Check reservation status
		if (assignedCopy == null ||
		        status != ReservationStatus.READY_FOR_PICKUP ||
		        assignedCopy.getStatus() != CopyStatus.RESERVED) {
		    throw new IllegalStateException("Reservation cannot expire");
		}

	    // 2. Check expiration date
	    if (!LocalDate.now().isAfter(expirationDate)) {
	        throw new IllegalStateException("Reservation has not expired yet");
	    }

	    // 3. Update reservation status
	    status = ReservationStatus.EXPIRED;

	    // 4. Update assigned copy status
	    assignedCopy.setStatus(CopyStatus.AVAILABLE);
	}
}




















