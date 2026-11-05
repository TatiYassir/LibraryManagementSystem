package com.library.ui;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import com.library.enums.BorrowStatus;
import com.library.enums.CopyStatus;
import com.library.enums.ReservationStatus;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.service.BorrowService;
import com.library.service.ReservationService;

public class BorrowingMenu extends Menu {

    private final Pickers pickers;
    private final BorrowService borrowService;
    private final ReservationService reservationService;

    public BorrowingMenu(ConsoleInput in, Pickers pickers,
            BorrowService borrowService,
            ReservationService reservationService) {
        super(in);
        this.pickers = pickers;
        this.borrowService = borrowService;
        this.reservationService = reservationService;
    }

    @Override
    protected String title() {
        return "BORROWING MENU";
    }

    @Override
    protected String[] options() {
        return new String[] {
            "Borrow a book", "Return a book", "Collect a reserved book",
            "Show all borrowings", "Show a member's borrowings",
            "Check overdue borrowings"
        };
    }

    @Override
    protected void handle(int choice) throws SQLException {
        switch (choice) {
            case 1 -> borrowBook();
            case 2 -> returnBook();
            case 3 -> collectReservation();
            case 4 -> borrowService.displayAllBorrowings();
            case 5 -> showMemberBorrowings();
            case 6 -> checkOverdue();
        }
    }

    private void borrowBook() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        Book book = pickers.selectBook();
        if (book == null) return;

        BookCopy copy = pickers.selectCopy(book, CopyStatus.AVAILABLE);
        if (copy == null) {
            System.out.println("You can reserve this book from the Reservation menu.");
            return;
        }

        LocalDate returnDate = in.readDate("Expected return date (YYYY-MM-DD): ");

        Borrow borrow = borrowService.borrowBook(member, copy, returnDate);
        System.out.println("\nBook borrowed successfully:\n" + borrow);
    }

    private void returnBook() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        Borrow borrow = chooseOutstandingBorrow(member);
        if (borrow == null) return;

        if (!in.confirm("Return this book? (y/n): ")) {
            System.out.println("Return cancelled.");
            return;
        }

        borrowService.returnBook(borrow);
        System.out.println("Book returned successfully.");
    }

    private void collectReservation() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        ArrayList<Reservation> ready = new ArrayList<>();
        for (Reservation r : reservationService
                .findReservationsByMember(member.getMemberId())) {
            if (r.getStatus() == ReservationStatus.READY_FOR_PICKUP) {
                ready.add(r);
            }
        }

        Reservation reservation = choose("READY FOR PICKUP", ready,
                "This member has no reservation ready for pickup.",
                "\nSelect a reservation (enter number): ");
        if (reservation == null) return;

        LocalDate returnDate = in.readDate("Expected return date (YYYY-MM-DD): ");

        Borrow borrow = borrowService.collectReservation(reservation, returnDate);
        System.out.println("\nReservation collected:\n" + borrow);
    }

    private void showMemberBorrowings() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        borrowService.displayBorrowingsByMember(member.getMemberId());
    }

    private void checkOverdue() throws SQLException {
        printList("OVERDUE BORROWINGS", borrowService.findOverdueBorrowings(),
                "No overdue borrowings.", "Total overdue");
    }

    // Lists a member's active/overdue borrowings and lets the user pick one
    private Borrow chooseOutstandingBorrow(Member member) throws SQLException {
        ArrayList<Borrow> outstanding = new ArrayList<>();

        for (Borrow b : borrowService.findBorrowingsByMember(member.getMemberId())) {
            if (b.getStatus() != BorrowStatus.RETURNED) {
                outstanding.add(b);
            }
        }

        return choose("OUTSTANDING BORROWINGS", outstanding,
                "This member has nothing to return.",
                "\nSelect a borrowing (enter number): ");
    }
}
