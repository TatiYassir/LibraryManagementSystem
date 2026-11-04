package com.library;

import java.util.ArrayList;
import java.time.LocalDate;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.enums.CopyStatus;
import com.library.service.BorrowService;

public class Main {
	public static void main (String [] args) {
		Book b1 = new Book("Clean Code","Robert C. Martin",1,2008);
		Member m1 = new Member("TATI Yassir","yassir@gamil.com","0612034876",75);
		Member m2 = new Member("wer ert","homhom@gamil.com","0612034785",185);
		BookCopy bc = new BookCopy(123, b1, CopyStatus.AVAILABLE);
		BookCopy bc2 = new BookCopy(124, b1, CopyStatus.AVAILABLE);
		BookCopy bc3 = new BookCopy(125, b1, CopyStatus.AVAILABLE);
		BorrowService service = new BorrowService();
		
		
		Borrow bw = service.borrowBook(
			    m1, bc, LocalDate.now().plusDays(14)
			);
		

		Borrow bw2 = service.borrowBook(
		    m1, bc2, LocalDate.now().plusDays(14)
		);
		
		Borrow bw1 = service.borrowBook(
			    m2, bc3, LocalDate.now().plusDays(14)
			);
		
		ArrayList<Borrow> history =
			    service.findBorrowingsByMember(75);

			System.out.println("Member history:");
			for (Borrow borrow : history) {
			    System.out.println(borrow);
			}
			service.displayBorrowingsByMember(75);
			service.displayBorrowingsByMember(999);
			System.out.println("Total records: " + history.size());
			System.out.println("Total records in service: " + service.getTotalBorrowings());
	}	
}
