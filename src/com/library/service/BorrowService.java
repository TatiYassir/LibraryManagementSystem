package com.library.service;

import java.util.ArrayList;
import java.time.LocalDate;

import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;

import com.library.enums.CopyStatus;
import com.library.enums.BorrowStatus;

public class BorrowService {

    // Attributes

    private int nextBorrowId = 1;
    private final ArrayList<Borrow> borrows = new ArrayList<>();

    // Methods

    /**
     * Borrows a book copy for a member.
     * Checks copy availability and enforces the maximum
     * of three active borrowings per member.
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
                    && borrow.getStatus() == BorrowStatus.ACTIVE) {
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
     * Updates the borrowing record and makes the copy available again.
     *
     * @param borrow the borrowing record to return
     */
    public void returnBook(Borrow borrow) {

        if (borrow == null) {
            throw new IllegalArgumentException("Borrow cannot be null");
        }

        // Update the borrowing record
        borrow.returnBook();

        // Make the physical book copy available again
        BookCopy copy = borrow.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
    }
    
 // Mark active borrowings as overdue when their return date has passed
    public void checkOverdueBorrowings() {
    	for (Borrow borrow : borrows) {
    		if (borrow.getStatus() == BorrowStatus.ACTIVE
    		        && LocalDate.now().isAfter(borrow.getExpectedReturnDate())) {
    			borrow.markAsOverdue();
    		}
    	}
    }
    
 // Find a borrowing record using its unique ID
    public Borrow findBorrowById(int borrowId) {
        for (Borrow borrow : borrows) {
            if (borrowId == borrow.getBorrowId()) {
                return borrow;
            }
        }

        return null;
    }
    
    // Display borrowings :
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
    
 // Find all borrowing records belonging to a specific member
    public ArrayList<Borrow> findBorrowingsByMember(int memberId) {
    	ArrayList<Borrow> result = new ArrayList<>();
    	for (Borrow borrow : borrows) {
    		if (memberId == borrow.getMember().getMemberId()) {
    			result.add(borrow);
            }
    	}
    	return result;
    }
    
    public int getTotalBorrowings() {
        return borrows.size();
    }
    
 // Display the borrowing history of a specific member
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
}


























