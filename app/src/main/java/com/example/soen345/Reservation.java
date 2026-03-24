package com.example.soen345;

import java.util.Date;

public class Reservation {
    private String id;
    private String userId;
    private String eventId;
    private int numberOfTickets;
    private Date reservationDate;
    private String status;

    public Reservation() {}

    public Reservation(String id, String userId, String eventId, int numberOfTickets) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.numberOfTickets = numberOfTickets;
        this.reservationDate = new Date();
        this.status = "confirmed";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public int getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(int numberOfTickets) { this.numberOfTickets = numberOfTickets; }

    public Date getReservationDate() { return reservationDate; }
    public void setReservationDate(Date reservationDate) { this.reservationDate = reservationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

