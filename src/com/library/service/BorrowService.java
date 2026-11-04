package com.library.service;

import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.enums.CopyStatus;
import java.time.LocalDate;

public class BorrowService {
	
	// Attributes :
	private int nextBorrowId = 1;
	// Constructor :
	public Borrow borrowBook(Member member, BookCopy copy,
			LocalDate expectedReturnDate) {
		if (member == null || copy == null) {
		    throw new IllegalArgumentException("Member and copy cannot be null");
		}
		
		if (copy.getStatus() != CopyStatus.AVAILABLE) {
		    throw new IllegalStateException("This copy is not available");
		}
		
		Borrow borrow = new Borrow(
				nextBorrowId,
				member,
				copy,
				expectedReturnDate
			);
		copy.setStatus(CopyStatus.BORROWED);
		
		nextBorrowId++;
		return borrow;
	}
}
