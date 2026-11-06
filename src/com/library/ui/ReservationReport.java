package com.library.ui;

import java.util.ArrayList;

import com.library.model.ExpiredReservationResult;
import com.library.model.Reservation;

public class ReservationReport {

    public static void displayExpiredReservations(
            ArrayList<ExpiredReservationResult> results) {

        if (results.isEmpty()) {
            System.out.println(
                    "No reservation has passed its pickup deadline.");
            return;
        }

        System.out.println("\n===== EXPIRED RESERVATIONS =====");

        for (ExpiredReservationResult result : results) {

            Reservation reservation = result.getReservation();

            System.out.println("\nReservation ID: "
                    + reservation.getReservationId());

            System.out.println("Member: "
                    + reservation.getMember().getFullName());

            System.out.println("Book title: "
                    + reservation.getBook().getTitle());

            System.out.println("Reservation date: "
                    + reservation.getReservationDate());

            System.out.println("Expiration date: "
                    + reservation.getExpirationDate());

            System.out.println("Reservation status: "
                    + reservation.getStatus());

            System.out.println("Assigned copy ID: "
                    + reservation.getAssignedCopy().getCopyId());

            if (result.isCopyReassigned()) {
                System.out.println(
                        "Copy reassigned to the next waiting member.");
            } else {
                System.out.println(
                        "Copy released; no waiting member was available.");
            }
        }

        System.out.println("\nTotal expired reservations: "
                + results.size());
    }
}