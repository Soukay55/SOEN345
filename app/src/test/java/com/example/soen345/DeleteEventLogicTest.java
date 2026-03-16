package com.example.soen345;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DeleteEventLogicTest {

    private List<Event> sampleEvents() {
        return Arrays.asList(
                new Event("1", "Jazz Night", "2026-03-15", "Montreal", "Music"),
                new Event("2", "Tech Conference", "2026-04-01", "Montreal", "Technology"),
                new Event("3", "Food Fest", "2026-04-25", "Toronto", "Food")
        );
    }

    @Test
    public void canDeleteEvent_adminWithValidEvent_returnsTrue() {
        Event event = new Event("1", "Jazz Night", "2026-03-15", "Montreal", "Music");

        boolean result = EventListActivity.canDeleteEvent(true, event);

        assertTrue(result);
    }

    @Test
    public void canDeleteEvent_nonAdmin_returnsFalse() {
        Event event = new Event("1", "Jazz Night", "2026-03-15", "Montreal", "Music");

        boolean result = EventListActivity.canDeleteEvent(false, event);

        assertFalse(result);
    }

    @Test
    public void canDeleteEvent_nullEvent_returnsFalse() {
        boolean result = EventListActivity.canDeleteEvent(true, null);

        assertFalse(result);
    }

    @Test
    public void canDeleteEvent_nullEventId_returnsFalse() {
        Event event = new Event(null, "Jazz Night", "2026-03-15", "Montreal", "Music");

        boolean result = EventListActivity.canDeleteEvent(true, event);

        assertFalse(result);
    }

    @Test
    public void canDeleteEvent_blankEventId_returnsFalse() {
        Event event = new Event("   ", "Jazz Night", "2026-03-15", "Montreal", "Music");

        boolean result = EventListActivity.canDeleteEvent(true, event);

        assertFalse(result);
    }

    @Test
    public void removeEventFromList_existingId_removesCorrectEvent() {
        List<Event> updated = EventListActivity.removeEventFromList(sampleEvents(), "2");

        assertEquals(2, updated.size());
        assertEquals("1", updated.get(0).getId());
        assertEquals("3", updated.get(1).getId());
    }

    @Test
    public void removeEventFromList_nonExistingId_keepsAllEvents() {
        List<Event> original = sampleEvents();
        List<Event> updated = EventListActivity.removeEventFromList(original, "999");

        assertEquals(3, updated.size());
        assertEquals("1", updated.get(0).getId());
        assertEquals("2", updated.get(1).getId());
        assertEquals("3", updated.get(2).getId());
    }

    @Test
    public void removeEventFromList_nullList_returnsEmptyList() {
        List<Event> updated = EventListActivity.removeEventFromList(null, "1");

        assertNotNull(updated);
        assertTrue(updated.isEmpty());
    }

    @Test
    public void removeEventFromList_nullEventId_keepsAllEvents() {
        List<Event> updated = EventListActivity.removeEventFromList(sampleEvents(), null);

        assertEquals(3, updated.size());
    }

    @Test
    public void removeEventFromList_skipsNullEventAndRemovesMatchingId() {
        List<Event> events = Arrays.asList(
                new Event("1", "Jazz Night", "2026-03-15", "Montreal", "Music"),
                null,
                new Event("2", "Tech Conference", "2026-04-01", "Montreal", "Technology")
        );

        List<Event> updated = EventListActivity.removeEventFromList(events, "1");

        assertEquals(1, updated.size());
        assertEquals("2", updated.get(0).getId());
    }
}