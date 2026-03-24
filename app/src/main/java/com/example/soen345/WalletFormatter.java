package com.example.soen345;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class WalletFormatter {
    public static List<String> format(List<Reservation> items) {
        List<String> out = new ArrayList<>();
        DateFormat df = DateFormat.getDateInstance();
        for (Reservation r : items) {
            String date = r.getReservationDate() == null ? "" : df.format(r.getReservationDate());
            out.add(String.format("%d ticket(s) - %s", r.getNumberOfTickets(), date));
        }
        return out;
    }
}

