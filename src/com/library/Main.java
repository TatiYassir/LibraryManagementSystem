package com.library;

import java.time.LocalDate;

import com.library.enums.CopyStatus;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.service.BorrowService;
import com.library.service.ReservationService;

public class Main {
	public static void main(String[] args) {

		// 1. Create a book and one available copy
		Book book = new Book("Clean Code", "Robert C. Martin", 1, 2008);
		BookCopy copy = new BookCopy(123, book, CopyStatus.AVAILABLE);

		// 2. Create two members
		Member memberA = new Member("Member A", "a@gmail.com", "0611111111", 1);
		Member memberB = new Member("Member B", "b@gmail.com", "0622222222", 2);

		// 3. Create services
		ReservationService reservationService = new ReservationService();
		BorrowService borrowService = new BorrowService(reservationService);

		// 4. Member A borrows the copy
		Borrow borrowA = borrowService.borrowBook(
		    memberA, copy, LocalDate.now().plusDays(14)
		);

		System.out.println("Member A borrowed the copy.");
		System.out.println("Copy status: " + copy.getStatus());

		// 5. Member B tries to borrow the same copy
		try {
		    borrowService.borrowBook(
		        memberB, copy, LocalDate.now().plusDays(14)
		    );
		} catch (IllegalStateException e) {
		    System.out.println("Member B rejected: " + e.getMessage());
		}

		// 6. Verify the final state
		System.out.println("Final copy status: " + copy.getStatus());
		System.out.println("Total borrowings before reservation collection: "
		        + borrowService.getTotalBorrowings());
		// 7. Member B reserves the book
		Reservation reservation = reservationService.reserve(memberB, book);

		System.out.println("\nMember B reserved the book.");
		System.out.println(reservation);
		// 8. Member A returns the book
		borrowService.returnBook(borrowA);

		System.out.println("\nAfter Member A returns the book:");
		System.out.println("Borrow status: " + borrowA.getStatus());
		System.out.println("Copy status: " + copy.getStatus());
		System.out.println("Reservation status: " + reservation.getStatus());
		System.out.println("Assigned copy ID: " +
		    reservation.getAssignedCopy().getCopyId());
		// 9. Member B collects the reserved book
		Borrow borrowB = borrowService.collectReservation(
		    reservation,
		    LocalDate.now().plusDays(14)
		);

		System.out.println("\nAfter Member B collects the book:");
		System.out.println(borrowB);
		System.out.println("Reservation status: " + reservation.getStatus());
		System.out.println("Copy status: " + copy.getStatus());
		System.out.println("Total borrowings: " + borrowService.getTotalBorrowings());
	}
}
