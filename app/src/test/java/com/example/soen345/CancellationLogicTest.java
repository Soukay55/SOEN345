package com.example.soen345;

import org.junit.Test;

import static org.junit.Assert.*;

public class CancellationLogicTest {

    @Test
    public void canCancel_nullOrNotCancelled_true() {
        assertTrue(CancellationLogic.canCancel(null));
        assertTrue(CancellationLogic.canCancel("confirmed"));
        assertTrue(CancellationLogic.canCancel("CONFIRMED"));
    }

    @Test
    public void canCancel_cancelled_false() {
        assertFalse(CancellationLogic.canCancel("cancelled"));
        assertFalse(CancellationLogic.canCancel("CANCELLED"));
    }

    @Test
    public void computeNewRemaining_basic() {
        assertEquals(5, CancellationLogic.computeNewRemaining(2, 10, 3));
    }

    @Test
    public void computeNewRemaining_clampsToCapacity() {
        assertEquals(10, CancellationLogic.computeNewRemaining(8, 10, 5));
    }

    @Test
    public void computeNewRemaining_handlesNegativeInputs() {
        // negative capacity treated as 0
        assertEquals(0, CancellationLogic.computeNewRemaining(2, -5, 3));
        // negative remaining treated as 0
        assertEquals(3, CancellationLogic.computeNewRemaining(-2, 10, 3));
        // negative returnedTickets treated as 0
        assertEquals(2, CancellationLogic.computeNewRemaining(2, 10, -3));
    }
}

