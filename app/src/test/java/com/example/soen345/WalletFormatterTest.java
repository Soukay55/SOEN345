package com.example.soen345;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class WalletFormatterTest {

    @Test
    public void formatsReservationsCorrectly() {
        List<Reservation> list = new ArrayList<>();
        Reservation r1 = new Reservation();
        r1.setNumberOfTickets(2);
        r1.setReservationDate(new Date(1600000000000L));
        list.add(r1);

        Reservation r2 = new Reservation();
        r2.setNumberOfTickets(1);
        r2.setReservationDate(null);
        list.add(r2);

        List<String> out = WalletFormatter.format(list);
        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("2 ticket(s)"));
        assertTrue(out.get(1).contains("1 ticket(s)"));
    }
}

