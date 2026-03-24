package com.example.soen345;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReservationValidatorTest {

    @Test
    public void returnsError_whenTicketsNotPositive() {
        Event e = new Event("id","t","d","l","c", 10);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 0);
        assertFalse(r.isValid);
        assertEquals("Ticket count must be a positive number", r.message);

        r = ReservationValidator.validateRequest(e, -1);
        assertFalse(r.isValid);
    }

    @Test
    public void returnsError_whenNotEnoughTickets() {
        Event e = new Event("id","t","d","l","c", 5);
        // set remaining lower than capacity
        e.setRemainingTickets(2);

        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 3);
        assertFalse(r.isValid);
        assertTrue(r.message.contains("Only"));
    }

    @Test
    public void returnsValid_whenEnoughTickets() {
        Event e = new Event("id","t","d","l","c", 100);
        e.setRemainingTickets(50);

        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 10);
        assertTrue(r.isValid);
    }
}

