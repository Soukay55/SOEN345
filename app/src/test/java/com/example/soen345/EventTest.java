package com.example.soen345;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        Event event = new Event(
                "1",
                "Career Fair",
                "2026-04-01",
                "Hall Building",
                "Education"
        );

        assertEquals("1", event.getId());
        assertEquals("Career Fair", event.getTitle());
        assertEquals("2026-04-01", event.getDate());
        assertEquals("Hall Building", event.getLocation());
        assertEquals("Education", event.getCategory());
    }

    @Test
    public void setters_workCorrectly() {
        Event event = new Event();

        event.setId("2");
        event.setTitle("Hackathon");
        event.setDate("2026-05-10");
        event.setLocation("Concordia");
        event.setCategory("Technology");

        assertEquals("2", event.getId());
        assertEquals("Hackathon", event.getTitle());
        assertEquals("2026-05-10", event.getDate());
        assertEquals("Concordia", event.getLocation());
        assertEquals("Technology", event.getCategory());
    }
}