package com.example.soen345;

public class CancellationLogic {

    // returns true if a reservation with the given status can be cancelled
    public static boolean canCancel(String status) {
        if (status == null) return true; // treat null as cancellable
        return !"cancelled".equalsIgnoreCase(status);
    }

    // computes the new remaining tickets after adding returnedTickets back to remaining, clamped to capacity.
    // ***assumes capacity >= 0, remaining >= 0, returnedTickets >= 0
    public static int computeNewRemaining(int remaining, int capacity, int returnedTickets) {
        if (capacity < 0) capacity = 0;
        if (remaining < 0) remaining = 0;
        if (returnedTickets < 0) returnedTickets = 0;

        long sum = (long) remaining + (long) returnedTickets;
        long cap = capacity;
        long result = Math.min(sum, cap);
        if (result < 0) return 0;
        return (int) result;
    }
}

