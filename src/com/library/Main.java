package com.library;

import java.time.LocalDate;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.enums.CopyStatus;
import com.library.service.BorrowService;

public class Main {
	public static void main (String [] args) {
		Book b1 = new Book("Clean Code","hendi",1,1965);
		Member m1 = new Member("TATI Yassir","yassir@gamil.com","0612034876",75);
		BookCopy bc = new BookCopy(123, b1, CopyStatus.AVAILABLE);

		BorrowService service = new BorrowService();

		Borrow bw = service.borrowBook(
		    m1,
		    bc,
		    LocalDate.now().plusDays(14)
		);
		try {
			Borrow b = service.borrowBook(
				    m1,
				    bc,
				    LocalDate.now().plusDays(14)
				);
		} catch (IllegalStateException e) {
			System.out.println(e.getMessage());
		}
		System.out.println(bw);
		System.out.println("Copy status: " + bc.getStatus());
	}
}
