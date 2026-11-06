package com.library.ui;

import java.sql.SQLException;
import java.util.ArrayList;

import com.library.enums.CopyStatus;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.repository.BookCopyRepository;
import com.library.repository.BorrowRepository;
import com.library.service.ReservationService;

public class CopyMenu extends Menu {

    private final BookCopyRepository copyRepository = new BookCopyRepository();
    private final BorrowRepository borrowRepository = new BorrowRepository();
    private final Pickers pickers;
    private final ReservationService reservationService;

    public CopyMenu(ConsoleInput in, Pickers pickers,
            ReservationService reservationService) {
        super(in);
        this.pickers = pickers;
        this.reservationService = reservationService;
    }

 // In CopyMenu.java
    @Override
    protected String title() {
        return "BOOK COPIES MENU"; // Aligned with header style
    }

    @Override
    protected String[] options() {
        return new String[] {
            "Add a copy of a book", "Show copies of a book",
            "Show available copies of a book", "Search copies",
            "Show all copies", "Change a copy's status"
        };
    }

    @Override
    protected void handle(int choice) throws SQLException {
        switch (choice) {
            case 1 -> addCopy();
            case 2 -> showCopies(null);
            case 3 -> showCopies(CopyStatus.AVAILABLE);
            case 4 -> searchCopies();
            case 5 -> showAllCopies();
            case 6 -> changeStatus();
        }
    }

    private void addCopy() throws SQLException {
        Book book = pickers.selectBook();
        if (book == null) return;

        int id = copyRepository.addBookCopy(book, CopyStatus.AVAILABLE);
        System.out.println("Copy added with ID " + id + ".");

        // A new free copy may satisfy someone waiting for this book
        BookCopy copy = copyRepository.findBookCopyById(id);
        if (reservationService.assignNextReservation(book, copy)) {
            System.out.println("The new copy was assigned to a waiting reservation.");
        }
    }

    private void showCopies(CopyStatus onlyStatus) throws SQLException {
        Book book = pickers.selectBook();
        if (book == null) return;

        ArrayList<BookCopy> copies = new ArrayList<>();
        for (BookCopy copy : copyRepository.findCopiesByBookId(book.getBookId())) {
            if (onlyStatus == null || copy.getStatus() == onlyStatus) {
                copies.add(copy);
            }
        }

        printList("COPIES OF \"" + book.getTitle() + "\"", copies,
                "No matching copies.", "Total copies");
    }

    private void searchCopies() throws SQLException {
        String keyword = in.readNonEmpty("Search keyword: ");
        printList("SEARCH RESULTS", copyRepository.searchBookCopies(keyword),
                "No copies found.", "Total copies found");
    }

    private void showAllCopies() throws SQLException {
        printList("ALL COPIES", copyRepository.findAllBookCopies(),
                "No copies registered.", "Total copies");
    }

    private void changeStatus() throws SQLException {
        Book book = pickers.selectBook();
        if (book == null) return;

        BookCopy copy = pickers.selectCopy(book, null);
        if (copy == null) return;

        CopyStatus current = copy.getStatus();

        // Borrowed / reserved copies are managed by the borrowing and
        // reservation flows, never by hand
        if (current == CopyStatus.BORROWED || current == CopyStatus.RESERVED) {
            throw new IllegalStateException("A " + current
                    + " copy cannot be changed manually.");
        }

        System.out.println("1. AVAILABLE");
        System.out.println("2. UNDER_MAINTENANCE");
        System.out.println("3. LOST");

        CopyStatus target = switch (in.readIndex("New status: ", 3)) {
            case 0 -> CopyStatus.AVAILABLE;
            case 1 -> CopyStatus.UNDER_MAINTENANCE;
            default -> CopyStatus.LOST;
        };

        if (target == current) {
            System.out.println("The copy already has this status.");
            return;
        }

        borrowRepository.updateCopyStatus(copy.getCopyId(), target);
        copy.setStatus(target);
        System.out.println("Copy status changed to " + target + ".");

        // A copy that just became free may satisfy a waiting reservation
        if (target == CopyStatus.AVAILABLE
                && reservationService.assignNextReservation(book, copy)) {
            System.out.println("The copy was assigned to a waiting reservation.");
        }
    }
}
