package com.example.soen345;

public class Event {
    private String id;
    private String title;
    private String date;
    private String location;
    private String category;
    private int capacity;
    private int remainingTickets;

    public Event() {} // Required for Firestore

    public Event(String id, String title, String date, String location, String category) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
        this.category = category;
        this.capacity = 0;
        this.remainingTickets = 0;
    }

    // optional constructor including capacity
    public Event(String id, String title, String date, String location, String category, int capacity) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
        this.category = category;
        this.capacity = capacity;
        this.remainingTickets = capacity;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public int getCapacity() { return capacity; }
    public int getRemainingTickets() { return remainingTickets; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setRemainingTickets(int remainingTickets) { this.remainingTickets = remainingTickets; }
}
