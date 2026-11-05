package com.library;

import java.sql.SQLException;

import com.library.model.Borrow;
import com.library.repository.BorrowRepository;

public class Main {

    public static void main(String[] args) {

        try {
            BorrowRepository borrowRepository = new BorrowRepository();

            // Retrieve the active borrowing
            Borrow borrow = borrowRepository.findBorrowById(2);

            if (borrow == null) {
                System.out.println("Borrowing not found.");
                return;
            }

            // Return the book
            borrowRepository.returnBorrow(borrow);

            System.out.println("Book returned successfully!");

            // Retrieve the updated borrowing
            Borrow updatedBorrow =
                    borrowRepository.findBorrowById(2);

            System.out.println("Borrow status: "
                    + updatedBorrow.getStatus());

            System.out.println("Actual return date: "
                    + updatedBorrow.getActualReturnDate());

            System.out.println("Copy status: "
                    + updatedBorrow.getBookCopy().getStatus());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}