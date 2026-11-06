package com.library.model;

public class ExpiredReservationResult {

    private final Reservation reservation;
    private final boolean copyReassigned;

    public ExpiredReservationResult(
            Reservation reservation,
            boolean copyReassigned) {

        this.reservation = reservation;
        this.copyReassigned = copyReassigned;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public boolean isCopyReassigned() {
        return copyReassigned;
    }
}