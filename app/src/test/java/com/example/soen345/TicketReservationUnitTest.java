package com.example.soen345;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases:
 *   TC-RES-U-01  Validate: zero tickets rejected
 *   TC-RES-U-02  Validate: negative tickets rejected
 *   TC-RES-U-03  Validate: null event rejected
 *   TC-RES-U-04  Validate: request exceeds remaining tickets
 *   TC-RES-U-05  Validate: request exactly equals remaining tickets (boundary)
 *   TC-RES-U-06  Validate: request one less than remaining tickets (boundary)
 *   TC-RES-U-07  Validate: happy-path with ample tickets
 *   TC-RES-U-08  Validate: zero remaining tickets
 *   TC-RES-U-09  Reservation model stores all fields correctly
 *   TC-RES-U-10  Reservation default status is "confirmed"
 *   TC-RES-U-11  Reservation status can be updated to "cancelled"
 *   TC-RES-U-12  Error message contains remaining count when not enough tickets
 */
public class TicketReservationUnitTest {

    private Event makeEvent(int capacity, int remaining) {
        Event e = new Event("evt_1", "Jazz Night", "2026-04-10", "Montreal", "Music", capacity);
        e.setRemainingTickets(remaining);
        return e;

    @Test
    public void TC_RES_U_01_zeroTickets_isInvalid() {
        Event e = makeEvent(10, 10);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 0);

        assertFalse("0 tickets should be invalid", r.isValid);
        assertEquals("Ticket count must be a positive number", r.message);
    }

    @Test
    public void TC_RES_U_02_negativeTickets_isInvalid() {
        Event e = makeEvent(10, 10);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, -5);

        assertFalse("Negative ticket count should be invalid", r.isValid);
        assertEquals("Ticket count must be a positive number", r.message);
    }

    @Test
    public void TC_RES_U_03_nullEvent_isInvalid() {
        ReservationValidator.Result r = ReservationValidator.validateRequest(null, 2);

        assertFalse("Null event should be invalid", r.isValid);
        assertEquals("Event not found", r.message);
    }

    @Test
    public void TC_RES_U_04_requestExceedsRemaining_isInvalid() {
        Event e = makeEvent(10, 3);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 5);

        assertFalse("Requesting more than remaining should be invalid", r.isValid);
        assertTrue("Message should mention remaining count", r.message.contains("3"));
    }

    @Test
    public void TC_RES_U_05_requestEqualsRemaining_isValid() {
        Event e = makeEvent(10, 4);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 4);

        assertTrue("Requesting exactly the remaining count should be valid", r.isValid);
        assertTrue("No error message expected", r.message.isEmpty());
    }

    @Test
    public void TC_RES_U_06_requestOneLessThanRemaining_isValid() {
        Event e = makeEvent(10, 4);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 3);

        assertTrue("Requesting one less than remaining should be valid", r.isValid);
    }

    @Test
    public void TC_RES_U_07_happyPath_validRequest() {
        Event e = makeEvent(100, 80);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 2);

        assertTrue("Happy-path reservation should be valid", r.isValid);
        assertTrue("No error message expected", r.message.isEmpty());
    }

    @Test
    public void TC_RES_U_08_zeroRemainingTickets_isInvalid() {
        Event e = makeEvent(10, 0);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 1);

        assertFalse("No remaining tickets should make request invalid", r.isValid);
        assertTrue("Message should mention 0 remaining", r.message.contains("0"));
    }

    @Test
    public void TC_RES_U_09_reservationModel_storesFieldsCorrectly() {
        Reservation res = new Reservation("res_1", "user_1", "evt_1", 3);

        assertEquals("res_1",  res.getId());
        assertEquals("user_1", res.getUserId());
        assertEquals("evt_1",  res.getEventId());
        assertEquals(3,        res.getNumberOfTickets());
        assertNotNull("Reservation date should be set", res.getReservationDate());
    }

    @Test
    public void TC_RES_U_10_newReservation_defaultStatusIsConfirmed() {
        Reservation res = new Reservation("res_2", "user_2", "evt_2", 1);

        assertEquals("confirmed", res.getStatus());
    }

    @Test
    public void TC_RES_U_11_reservationStatus_canBeCancelled() {
        Reservation res = new Reservation("res_3", "user_3", "evt_3", 2);
        res.setStatus("cancelled");

        assertEquals("cancelled", res.getStatus());
    }

    @Test
    public void TC_RES_U_12_errorMessage_containsRemainingCount() {
        Event e = makeEvent(50, 7);
        ReservationValidator.Result r = ReservationValidator.validateRequest(e, 10);

        assertFalse(r.isValid);
        assertTrue("Error should state only 7 remaining", r.message.contains("7"));
    }
}

