package com.example.soen345;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class EventListActivityTest {

    @Test
    public void filterEvents_allEmptyFilters_returnsAllEvents() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.filterEvents(events, "", "", "", "");

        assertEquals(3, result.size());
        assertEquals("e1", result.get(0).getId());
        assertEquals("e2", result.get(1).getId());
        assertEquals("e3", result.get(2).getId());
    }

    @Test
    public void filterEvents_searchMatchesTitle_caseInsensitive() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.filterEvents(events, "jAzZ", "", "", "");

        assertEquals(1, result.size());
        assertEquals("e1", result.get(0).getId());
    }

    @Test
    public void filterEvents_dateMatchesSubstring() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.filterEvents(events, "", "2026-03", "", "");

        assertEquals(2, result.size());
        assertEquals("e1", result.get(0).getId());
        assertEquals("e2", result.get(1).getId());
    }

    @Test
    public void filterEvents_locationMatchesSubstring() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.filterEvents(events, "", "", "montreal", "");

        assertEquals(2, result.size());
        assertEquals("e1", result.get(0).getId());
        assertEquals("e3", result.get(1).getId());
    }

    @Test
    public void filterEvents_categoryMatchesSubstring() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.filterEvents(events, "", "", "", "music");

        assertEquals(2, result.size());
        assertEquals("e1", result.get(0).getId());
        assertEquals("e3", result.get(1).getId());
    }

    @Test
    public void filterEvents_multipleFilters_returnsOnlyMatchingEvents() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.filterEvents(
                events,
                "rock",
                "2026-05",
                "bell",
                "music"
        );

        assertEquals(1, result.size());
        assertEquals("e3", result.get(0).getId());
    }

    @Test
    public void filterEvents_noMatches_returnsEmptyList() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.filterEvents(events, "ballet", "", "", "");

        assertTrue(result.isEmpty());
    }

    @Test
    public void filterEvents_nullFilters_treatedAsEmpty() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.filterEvents(events, null, null, null, null);

        assertEquals(3, result.size());
    }

    @Test
    public void filterEvents_nullEventFields_doNotCrash() {
        List<Event> events = new ArrayList<>();

        Event event = new Event();
        event.setId("e1");
        event.setTitle(null);
        event.setDate(null);
        event.setLocation(null);
        event.setCategory(null);
        events.add(event);

        List<Event> result = EventListActivity.filterEvents(events, "", "", "", "");

        assertEquals(1, result.size());
        assertEquals("e1", result.get(0).getId());
    }

    @Test
    public void canDeleteEvent_adminWithValidEvent_returnsTrue() {
        Event event = new Event();
        event.setId("e1");

        assertTrue(EventListActivity.canDeleteEvent(true, event));
    }

    @Test
    public void canDeleteEvent_nonAdmin_returnsFalse() {
        Event event = new Event();
        event.setId("e1");

        assertFalse(EventListActivity.canDeleteEvent(false, event));
    }

    @Test
    public void canDeleteEvent_nullEvent_returnsFalse() {
        assertFalse(EventListActivity.canDeleteEvent(true, null));
    }

    @Test
    public void canDeleteEvent_blankId_returnsFalse() {
        Event event = new Event();
        event.setId("   ");

        assertFalse(EventListActivity.canDeleteEvent(true, event));
    }

    @Test
    public void removeEventFromList_removesMatchingEvent() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.removeEventFromList(events, "e2");

        assertEquals(2, result.size());
        assertEquals("e1", result.get(0).getId());
        assertEquals("e3", result.get(1).getId());
    }

    @Test
    public void removeEventFromList_nullList_returnsEmptyList() {
        List<Event> result = EventListActivity.removeEventFromList(null, "e1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void removeEventFromList_nullEventInList_isIgnored() {
        List<Event> events = new ArrayList<>();
        events.add(sampleEvent("e1", "Jazz Night", "2026-03-15", "Montreal Jazz Club", "Music"));
        events.add(null);
        events.add(sampleEvent("e2", "Comedy Show", "2026-03-20", "Downtown", "Comedy"));

        List<Event> result = EventListActivity.removeEventFromList(events, "e2");

        assertEquals(1, result.size());
        assertEquals("e1", result.get(0).getId());
    }

    @Test
    public void removeEventFromList_nullEventId_keepsAllValidEvents() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.removeEventFromList(events, null);

        assertEquals(3, result.size());
    }

    @Test
    public void canEditEvent_adminWithValidEvent_returnsTrue() {
        Event event = new Event();
        event.setId("e1");

        assertTrue(EventListActivity.canEditEvent(true, event));
    }

    @Test
    public void canEditEvent_nonAdmin_returnsFalse() {
        Event event = new Event();
        event.setId("e1");

        assertFalse(EventListActivity.canEditEvent(false, event));
    }

    @Test
    public void canEditEvent_nullEvent_returnsFalse() {
        assertFalse(EventListActivity.canEditEvent(true, null));
    }

    @Test
    public void canEditEvent_nullId_returnsFalse() {
        Event event = new Event();
        event.setId(null);

        assertFalse(EventListActivity.canEditEvent(true, event));
    }

    @Test
    public void updateEventInList_replacesMatchingEvent() {
        List<Event> events = sampleEvents();
        Event updatedEvent = sampleEvent("e2", "Updated Comedy Show", "2026-03-21", "New Venue", "Comedy");

        List<Event> result = EventListActivity.updateEventInList(events, updatedEvent);

        assertEquals(3, result.size());
        assertEquals("e1", result.get(0).getId());
        assertEquals("e2", result.get(1).getId());
        assertEquals("Updated Comedy Show", result.get(1).getTitle());
        assertEquals("2026-03-21", result.get(1).getDate());
        assertEquals("New Venue", result.get(1).getLocation());
        assertEquals("e3", result.get(2).getId());
    }

    @Test
    public void updateEventInList_nullUpdatedEvent_keepsOriginalList() {
        List<Event> events = sampleEvents();

        List<Event> result = EventListActivity.updateEventInList(events, null);

        assertEquals(3, result.size());
        assertEquals("e1", result.get(0).getId());
        assertEquals("e2", result.get(1).getId());
        assertEquals("e3", result.get(2).getId());
    }

    @Test
    public void updateEventInList_nullList_returnsEmptyList() {
        Event updatedEvent = sampleEvent("e2", "Updated Comedy Show", "2026-03-21", "New Venue", "Comedy");

        List<Event> result = EventListActivity.updateEventInList(null, updatedEvent);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void updateEventInList_nullEventInsideList_isIgnored() {
        List<Event> events = new ArrayList<>();
        events.add(sampleEvent("e1", "Jazz Night", "2026-03-15", "Montreal Jazz Club", "Music"));
        events.add(null);
        events.add(sampleEvent("e2", "Comedy Show", "2026-03-20", "Downtown", "Comedy"));

        Event updatedEvent = sampleEvent("e2", "Updated Comedy Show", "2026-03-21", "New Venue", "Comedy");

        List<Event> result = EventListActivity.updateEventInList(events, updatedEvent);

        assertEquals(2, result.size());
        assertEquals("e1", result.get(0).getId());
        assertEquals("Updated Comedy Show", result.get(1).getTitle());
    }

    private List<Event> sampleEvents() {
        List<Event> events = new ArrayList<>();
        events.add(sampleEvent("e1", "Jazz Night", "2026-03-15", "Montreal Jazz Club", "Music"));
        events.add(sampleEvent("e2", "Comedy Show", "2026-03-20", "Comedy Works, Downtown", "Comedy"));
        events.add(sampleEvent("e3", "Rock Concert", "2026-05-10", "Bell Centre, Montreal", "Music"));
        return events;
    }

    private Event sampleEvent(String id, String title, String date, String location, String category) {
        Event event = new Event();
        event.setId(id);
        event.setTitle(title);
        event.setDate(date);
        event.setLocation(location);
        event.setCategory(category);
        return event;
    }
}