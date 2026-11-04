package com.library;

import java.util.ArrayList;
import java.time.LocalDate;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.enums.CopyStatus;
import com.library.service.BorrowService;
import com.library.model.Reservation;
import com.library.service.ReservationService;

public class Main {
	public static void main (String [] args) {
		Book b1 = new Book("Clean Code","Robert C. Martin",1,2008);
		Member m1 = new Member("TATI Yassir","yassir@gamil.com","0612034876",75);
		Member m2 = new Member("wer ert","homhom@gamil.com","0612034785",185);
		BookCopy bc = new BookCopy(123, b1, CopyStatus.AVAILABLE);
		BookCopy bc2 = new BookCopy(124, b1, CopyStatus.AVAILABLE);
		BookCopy bc3 = new BookCopy(125, b1, CopyStatus.AVAILABLE);
		BorrowService service = new BorrowService();
		ReservationService reservationService = new ReservationService();
		
		Borrow bw = service.borrowBook(
			    m1, bc, LocalDate.now().plusDays(14)
			);
		

		Borrow bw2 = service.borrowBook(
		    m1, bc2, LocalDate.now().plusDays(14)
		);
		
		Borrow bw1 = service.borrowBook(
			    m2, bc3, LocalDate.now().plusDays(14)
			);
		
		Reservation r1 = reservationService.reserve(m1, b1);
		Reservation r2 = reservationService.reserve(m2, b1);
		// Display all reservations
		reservationService.displayAllReservations();

		// Find reservation by ID
		System.out.println(reservationService.findReservationById(1));

		// Display member's reservation history
		reservationService.displayReservationsByMember(m1.getMemberId());

		// Display total reservations
		System.out.println("Total reservations: "
		        + reservationService.getTotalReservations());
	}	
}
