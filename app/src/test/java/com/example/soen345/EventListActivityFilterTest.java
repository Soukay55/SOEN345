package com.example.soen345;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class EventListActivityFilterTest {

    private List<Event> sampleEvents() {
        return Arrays.asList(
                new Event("1", "Jazz Night", "2026-03-15 8:00 PM", "Montreal Jazz Club", "Music"),
                new Event("2", "Comedy Show", "2026-03-20 9:00 PM", "Comedy Works, Downtown", "Comedy"),
                new Event("3", "Tech Conference", "2026-04-01 10:00 AM", "Palais des Congrès", "Technology"),
                new Event("4", "Food & Wine Fest", "2026-04-25 12:00 PM", "Old Port, Montreal", "Food"),
                new Event("5", "Rock Concert", "2026-05-10 7:00 PM", "Bell Centre, Montreal", "Music")
        );
    }

    @Test
    public void filterByTitle_searchFindsTech() {
        List<Event> out = EventListActivity.filterEvents(
                sampleEvents(),
                "tech",
                "", "", ""
        );

        assertEquals(1, out.size());
        assertEquals("Tech Conference", out.get(0).getTitle());
    }

    @Test
    public void filterByDate_onlyThatDateShown() {
        List<Event> out = EventListActivity.filterEvents(
                sampleEvents(),
                "",
                "2026-04-01",
                "", ""
        );

        assertEquals(1, out.size());
        assertTrue(out.get(0).getDate().contains("2026-04-01"));
    }

    @Test
    public void filterByLocation_onlyMontrealReturned() {
        List<Event> out = EventListActivity.filterEvents(
                sampleEvents(),
                "",
                "",
                "montreal",
                ""
        );

        assertEquals(3, out.size()); // Jazz Night, Food & Wine Fest, Rock Concert
        for (Event e : out) {
            assertTrue(e.getLocation().toLowerCase().contains("montreal"));
        }
    }


    @Test
    public void filterByCategory_onlyMusicReturned() {
        List<Event> out = EventListActivity.filterEvents(
                sampleEvents(),
                "",
                "",
                "",
                "music"
        );

        assertEquals(2, out.size());
        for (Event e : out) {
            assertTrue(e.getCategory().toLowerCase().contains("music"));
        }
    }

    @Test
    public void combinedFilters_ANDLogic() {
        // Must match all: title contains "fest", location contains "montreal", category contains "food"
        List<Event> out = EventListActivity.filterEvents(
                sampleEvents(),
                "fest",
                "",
                "montreal",
                "food"
        );

        assertEquals(1, out.size());
        assertEquals("Food & Wine Fest", out.get(0).getTitle());
    }

    @Test
    public void noResults_returnsEmptyList() {
        List<Event> out = EventListActivity.filterEvents(
                sampleEvents(),
                "does-not-exist",
                "",
                "",
                ""
        );

        assertTrue(out.isEmpty());
    }

    @Test
    public void emptyInputs_returnsAllEvents() {
        List<Event> all = sampleEvents();
        List<Event> out = EventListActivity.filterEvents(all, "", "", "", "");

        assertEquals(all.size(), out.size());
    }
}