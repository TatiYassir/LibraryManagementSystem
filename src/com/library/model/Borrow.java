package com.library.model;
import com.library.enums.BorrowStatus;
import java.time.LocalDate;
public class Borrow {
	// Attributes :
	private final int borrowId;
	private final Member member;
	private final BookCopy bookCopy;
	private BorrowStatus status;
	private final LocalDate borrowDate;
	private LocalDate expectedReturnDate;
	private LocalDate actualReturnDate;


	// Constructor used to create a new borrowing
	public Borrow(int borrowId, Member member, BookCopy bookCopy,
	        LocalDate expectedReturnDate) {

	    if (member == null || bookCopy == null) {
	        throw new IllegalArgumentException(
	                "Member and book copy cannot be null");
	    }

	    this.borrowDate = LocalDate.now();

	    if (expectedReturnDate == null ||
	            !expectedReturnDate.isAfter(borrowDate)) {
	        throw new IllegalArgumentException(
	                "Invalid expected return date");
	    }

	    this.borrowId = borrowId;
	    this.member = member;
	    this.bookCopy = bookCopy;
	    this.expectedReturnDate = expectedReturnDate;
	    this.actualReturnDate = null;
	    this.status = BorrowStatus.ACTIVE;
	}
	// Restore an existing borrowing from the database
	public Borrow(int borrowId, Member member, BookCopy bookCopy,
	        LocalDate borrowDate, LocalDate expectedReturnDate,
	        LocalDate actualReturnDate, BorrowStatus status) {

	    if (member == null || bookCopy == null || borrowDate == null
	            || expectedReturnDate == null || status == null) {
	        throw new IllegalArgumentException("Invalid borrowing data");
	    }

	    if (!expectedReturnDate.isAfter(borrowDate)) {
	        throw new IllegalArgumentException("Invalid expected return date");
	    }

	    if (status == BorrowStatus.RETURNED && actualReturnDate == null) {
	        throw new IllegalArgumentException(
	                "Returned borrowing needs an actual return date");
	    }

	    if (status != BorrowStatus.RETURNED && actualReturnDate != null) {
	        throw new IllegalArgumentException(
	                "Only returned borrowings can have an actual return date");
	    }

	    this.borrowId = borrowId;
	    this.member = member;
	    this.bookCopy = bookCopy;
	    this.borrowDate = borrowDate;
	    this.expectedReturnDate = expectedReturnDate;
	    this.actualReturnDate = actualReturnDate;
	    this.status = status;
	}

	
	// getter :
	
	public int getBorrowId() {
		return borrowId;
	}
	public Member getMember() {
		return member;
	}
	public BookCopy getBookCopy() {
	    return bookCopy;
	}
	public LocalDate getBorrowDate() {
		return borrowDate;
	}
	public LocalDate getExpectedReturnDate() {
		return expectedReturnDate;
	}
	public LocalDate getActualReturnDate() {
		return actualReturnDate;
	}
	public BorrowStatus getStatus() {
		return status;
	}
	
	// setter :
	public void setExpectedReturnDate(LocalDate expectedReturnDate) {
		if (expectedReturnDate == null || !expectedReturnDate.isAfter(borrowDate)) {
		    throw new IllegalArgumentException("Invalid expected return date");
		}
		this.expectedReturnDate = expectedReturnDate;
	}
	
	// Methods :
	public void returnBook() {
		if(status == BorrowStatus.RETURNED ) {
			throw new IllegalStateException("This book has already been returned");
		}
		actualReturnDate = LocalDate.now();
		status = BorrowStatus.RETURNED;
	}
	public void markAsOverdue() {

	    // Only active borrowings can become overdue
	    if (status != BorrowStatus.ACTIVE) {
	        throw new IllegalStateException(
	            "Only active borrowings can become overdue"
	        );
	    }

	    // Verify that the expected return date has passed
	    if (!LocalDate.now().isAfter(expectedReturnDate)) {
	        throw new IllegalStateException(
	            "Borrowing is not overdue yet"
	        );
	    }

	    status = BorrowStatus.OVERDUE;
	}
    
	
	@Override
	public String toString() {
	    return "Borrow ID: " + borrowId +
	           "\nMember: " + member.getFullName() +
	           "\nBook: " + bookCopy.getBook().getTitle() +
	           "\nCopy ID: " + bookCopy.getCopyId() +
	           "\nBorrow Date: " + borrowDate +
	           "\nExpected Return Date: " + expectedReturnDate +
	           "\nActual Return Date: " + actualReturnDate +
	           "\nStatus: " + status;
	}
}





















