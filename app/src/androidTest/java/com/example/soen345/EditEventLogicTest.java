package com.example.soen345;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class EditEventLogicTest {

    private List<Event> sampleEvents() {
        return Arrays.asList(
                new Event("1", "Jazz Night", "2026-03-15", "Montreal", "Music"),
                new Event("2", "Tech Conference", "2026-04-01", "Montreal", "Technology"),
                new Event("3", "Food Fest", "2026-04-25", "Toronto", "Food")
        );
    }

    @Test
    public void canEditEvent_adminWithValidEvent_returnsTrue() {
        Event event = new Event("1", "Jazz Night", "2026-03-15", "Montreal", "Music");

        boolean result = EventListActivity.canEditEvent(true, event);

        assertTrue(result);
    }

    @Test
    public void canEditEvent_nonAdmin_returnsFalse() {
        Event event = new Event("1", "Jazz Night", "2026-03-15", "Montreal", "Music");

        boolean result = EventListActivity.canEditEvent(false, event);

        assertFalse(result);
    }

    @Test
    public void canEditEvent_nullEvent_returnsFalse() {
        boolean result = EventListActivity.canEditEvent(true, null);

        assertFalse(result);
    }

    @Test
    public void canEditEvent_nullEventId_returnsFalse() {
        Event event = new Event(null, "Jazz Night", "2026-03-15", "Montreal", "Music");

        boolean result = EventListActivity.canEditEvent(true, event);

        assertFalse(result);
    }

    @Test
    public void canEditEvent_blankEventId_returnsFalse() {
        Event event = new Event("   ", "Jazz Night", "2026-03-15", "Montreal", "Music");

        boolean result = EventListActivity.canEditEvent(true, event);

        assertFalse(result);
    }

    @Test
    public void updateEventInList_existingId_updatesCorrectEvent() {
        Event updatedEvent = new Event("2", "Updated Conference", "2026-04-02", "Quebec City", "Business");

        List<Event> updated = EventListActivity.updateEventInList(sampleEvents(), updatedEvent);

        assertEquals(3, updated.size());
        assertEquals("1", updated.get(0).getId());
        assertEquals("Updated Conference", updated.get(1).getTitle());
        assertEquals("2026-04-02", updated.get(1).getDate());
        assertEquals("Quebec City", updated.get(1).getLocation());
        assertEquals("Business", updated.get(1).getCategory());
        assertEquals("3", updated.get(2).getId());
    }

    @Test
    public void updateEventInList_nonExistingId_keepsOriginalEvents() {
        Event updatedEvent = new Event("999", "Updated Conference", "2026-04-02", "Quebec City", "Business");

        List<Event> updated = EventListActivity.updateEventInList(sampleEvents(), updatedEvent);

        assertEquals(3, updated.size());
        assertEquals("Jazz Night", updated.get(0).getTitle());
        assertEquals("Tech Conference", updated.get(1).getTitle());
        assertEquals("Food Fest", updated.get(2).getTitle());
    }

    @Test
    public void updateEventInList_nullList_returnsEmptyList() {
        Event updatedEvent = new Event("2", "Updated Conference", "2026-04-02", "Quebec City", "Business");

        List<Event> updated = EventListActivity.updateEventInList(null, updatedEvent);

        assertNotNull(updated);
        assertTrue(updated.isEmpty());
    }

    @Test
    public void updateEventInList_nullUpdatedEvent_keepsOriginalEvents() {
        List<Event> updated = EventListActivity.updateEventInList(sampleEvents(), null);

        assertEquals(3, updated.size());
        assertEquals("Jazz Night", updated.get(0).getTitle());
        assertEquals("Tech Conference", updated.get(1).getTitle());
        assertEquals("Food Fest", updated.get(2).getTitle());
    }

    @Test
    public void updateEventInList_skipsNullEventAndUpdatesMatchingOne() {
        List<Event> events = Arrays.asList(
                new Event("1", "Jazz Night", "2026-03-15", "Montreal", "Music"),
                null,
                new Event("2", "Tech Conference", "2026-04-01", "Montreal", "Technology")
        );

        Event updatedEvent = new Event("2", "Edited Tech Conference", "2026-04-03", "Ottawa", "Innovation");

        List<Event> updated = EventListActivity.updateEventInList(events, updatedEvent);

        assertEquals(2, updated.size());
        assertEquals("Jazz Night", updated.get(0).getTitle());
        assertEquals("Edited Tech Conference", updated.get(1).getTitle());
        assertEquals("2026-04-03", updated.get(1).getDate());
        assertEquals("Ottawa", updated.get(1).getLocation());
        assertEquals("Innovation", updated.get(1).getCategory());
    }
}