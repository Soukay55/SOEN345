package com.example.soen345;

public class ReservationValidator {

    public static class Result {
        public final boolean isValid;
        public final String message;

        public Result(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }

    public static Result validateRequest(Event event, int ticketsRequested) {
        if (ticketsRequested <= 0) {
            return new Result(false, "Ticket count must be a positive number");
        }
        if (event == null) {
            return new Result(false, "Event not found");
        }
        int remaining = event.getRemainingTickets();
        if (remaining < ticketsRequested) {
            return new Result(false, String.format("Only %d ticket(s) remaining.", remaining));
        }
        return new Result(true, "");
    }
}

