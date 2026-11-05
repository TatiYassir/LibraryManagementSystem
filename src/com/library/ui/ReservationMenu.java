package com.library.ui;

import java.sql.SQLException;
import java.util.ArrayList;

import com.library.enums.ReservationStatus;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.service.BorrowService;
import com.library.service.ReservationService;

public class ReservationMenu extends Menu {

    private final Pickers pickers;
    private final ReservationService reservationService;
    private final BorrowService borrowService;

    public ReservationMenu(ConsoleInput in, Pickers pickers,
            ReservationService reservationService,
            BorrowService borrowService) {
        super(in);
        this.pickers = pickers;
        this.reservationService = reservationService;
        this.borrowService = borrowService;
    }

    @Override
    protected String title() {
        return "RESERVATION MENU";
    }

    @Override
    protected String[] options() {
        return new String[] {
            "Reserve a book", "Show all reservations",
            "Show a member's reservations", "Cancel a reservation",
            "Expire overdue pickups"
        };
    }

    @Override
    protected void handle(int choice) throws SQLException {
        switch (choice) {
            case 1 -> reserveBook();
            case 2 -> reservationService.displayAllReservations();
            case 3 -> showMemberReservations();
            case 4 -> cancelReservation();
            case 5 -> expirePickups();
        }
    }

    private void reserveBook() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        Book book = pickers.selectBook();
        if (book == null) return;

        // Reserving a book the member already holds makes no sense
        if (borrowService.hasOutstandingBorrowOfBook(
                member.getMemberId(), book.getBookId())) {
            throw new IllegalStateException(
                    "This member currently has this book borrowed");
        }

        Reservation reservation = reservationService.reserve(member, book);

        System.out.println("\nReservation created:\n" + reservation);

        if (reservation.getStatus() == ReservationStatus.READY_FOR_PICKUP) {
            System.out.println("\nA copy was free, so it is ready for pickup.");
        } else {
            System.out.println("\nNo copy is free; the member is in the waiting queue.");
        }
    }

    private void showMemberReservations() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        reservationService.displayReservationsByMember(member.getMemberId());
    }

    private void cancelReservation() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        ArrayList<Reservation> open = new ArrayList<>();
        for (Reservation r : reservationService
                .findReservationsByMember(member.getMemberId())) {
            if (r.getStatus() == ReservationStatus.WAITING
                    || r.getStatus() == ReservationStatus.READY_FOR_PICKUP) {
                open.add(r);
            }
        }

        Reservation reservation = choose("OPEN RESERVATIONS", open,
                "This member has no open reservations.",
                "\nSelect a reservation (enter number): ");
        if (reservation == null) return;

        if (!in.confirm("Cancel this reservation? (y/n): ")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        reservationService.cancelReservation(reservation);
        System.out.println("Reservation cancelled.");
    }

    private void expirePickups() throws SQLException {
        int count = reservationService.expireOverdueReservations();
        System.out.println(count == 0
                ? "No reservation has passed its pickup deadline."
                : count + " reservation(s) expired and their copies released.");
    }
}
