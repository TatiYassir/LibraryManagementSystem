package com.library.ui;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

import com.library.enums.CopyStatus;
import com.library.model.BookCopy;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import com.library.service.BorrowService;
import com.library.service.ReservationService;

public class ReportsMenu extends Menu {

    private final BookRepository bookRepository = new BookRepository();
    private final MemberRepository memberRepository = new MemberRepository();
    private final BookCopyRepository copyRepository = new BookCopyRepository();
    private final BorrowService borrowService;
    private final ReservationService reservationService;

    public ReportsMenu(ConsoleInput in, BorrowService borrowService,
            ReservationService reservationService) {
        super(in);
        this.borrowService = borrowService;
        this.reservationService = reservationService;
    }

    @Override
    protected String title() {
        return "REPORTS MENU";
    }

    @Override
    protected String[] options() {
        return new String[] {
            "Library statistics", "Copies by status", "Overdue borrowings"
        };
    }

    @Override
    protected void handle(int choice) throws SQLException {
        switch (choice) {
            case 1 -> statistics();
            case 2 -> copiesByStatus();
            case 3 -> printList("OVERDUE BORROWINGS",
                    borrowService.findOverdueBorrowings(),
                    "No overdue borrowings.", "Total overdue");
        }
    }

    private void statistics() throws SQLException {
        System.out.println("\n===== LIBRARY STATISTICS =====");
        System.out.println("Books:        " + bookRepository.findAllBooks().size());
        System.out.println("Copies:       " + copyRepository.findAllBookCopies().size());
        System.out.println("Members:      " + memberRepository.findAllMembers().size());
        System.out.println("Borrowings:   " + borrowService.getTotalBorrowings());
        System.out.println("Reservations: " + reservationService.getTotalReservations());
    }

    private void copiesByStatus() throws SQLException {
        Map<CopyStatus, Integer> counts = new EnumMap<>(CopyStatus.class);

        for (CopyStatus status : CopyStatus.values()) {
            counts.put(status, 0);
        }

        for (BookCopy copy : copyRepository.findAllBookCopies()) {
            counts.merge(copy.getStatus(), 1, Integer::sum);
        }

        System.out.println("\n===== COPIES BY STATUS =====");
        counts.forEach((status, n) -> System.out.println(status + ": " + n));
    }
}
