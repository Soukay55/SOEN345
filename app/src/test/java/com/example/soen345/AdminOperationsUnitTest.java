package com.example.soen345;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**

 * Test cases:
 *   TC-ADM-U-01  canDeleteEvent: admin + valid event → true
 *   TC-ADM-U-02  canDeleteEvent: non-admin → false
 *   TC-ADM-U-03  canDeleteEvent: null event → false
 *   TC-ADM-U-04  canDeleteEvent: event with null ID → false
 *   TC-ADM-U-05  canDeleteEvent: event with blank ID → false
 *   TC-ADM-U-06  canEditEvent: admin + valid event → true
 *   TC-ADM-U-07  canEditEvent: non-admin → false
 *   TC-ADM-U-08  canEditEvent: null event → false
 *   TC-ADM-U-09  removeEventFromList: removes correct event by ID
 *   TC-ADM-U-10  removeEventFromList: unknown ID leaves list unchanged
 *   TC-ADM-U-11  removeEventFromList: null list returns empty list
 *   TC-ADM-U-12  updateEventInList: updates correct event in place
 *   TC-ADM-U-13  updateEventInList: unknown ID leaves list unchanged
 *   TC-ADM-U-14  updateEventInList: null list returns empty list
 *   TC-ADM-U-15  isValidEventInput (Add): all fields filled → true
 *   TC-ADM-U-16  isValidEventInput (Add): empty title → false
 *   TC-ADM-U-17  isValidEventInput (Add): whitespace-only field → false
 *   TC-ADM-U-18  isValidEventInput (Add): null field → false
 *   TC-ADM-U-19  isValidEventInput (Edit): all fields filled → true
 *   TC-ADM-U-20  isValidEventInput (Edit): empty date → false
 */
public class AdminOperationsUnitTest {

    private Event event(String id) {
        return new Event(id, "Test Event", "2026-05-01", "Montreal", "Music");
    }

    private List<Event> threeEvents() {
        return Arrays.asList(
                event("evt_1"),
                event("evt_2"),
                event("evt_3")
        );
    }


    @Test
    public void TC_ADM_U_01_canDelete_adminValidEvent_returnsTrue() {
        assertTrue(EventListActivity.canDeleteEvent(true, event("evt_1")));
    }

    @Test
    public void TC_ADM_U_02_canDelete_nonAdmin_returnsFalse() {
        assertFalse(EventListActivity.canDeleteEvent(false, event("evt_1")));
    }


    @Test
    public void TC_ADM_U_03_canDelete_nullEvent_returnsFalse() {
        assertFalse(EventListActivity.canDeleteEvent(true, null));
    }


    @Test
    public void TC_ADM_U_04_canDelete_nullEventId_returnsFalse() {
        assertFalse(EventListActivity.canDeleteEvent(true, event(null)));
    }


    @Test
    public void TC_ADM_U_05_canDelete_blankEventId_returnsFalse() {
        assertFalse(EventListActivity.canDeleteEvent(true, event("   ")));
    }


    @Test
    public void TC_ADM_U_06_canEdit_adminValidEvent_returnsTrue() {
        assertTrue(EventListActivity.canEditEvent(true, event("evt_1")));
    }


    @Test
    public void TC_ADM_U_07_canEdit_nonAdmin_returnsFalse() {
        assertFalse(EventListActivity.canEditEvent(false, event("evt_1")));
    }


    @Test
    public void TC_ADM_U_08_canEdit_nullEvent_returnsFalse() {
        assertFalse(EventListActivity.canEditEvent(true, null));
    }


    @Test
    public void TC_ADM_U_09_removeEvent_existingId_removesCorrectEvent() {
        List<Event> result = EventListActivity.removeEventFromList(threeEvents(), "evt_2");

        assertEquals(2, result.size());
        assertEquals("evt_1", result.get(0).getId());
        assertEquals("evt_3", result.get(1).getId());
    }


    @Test
    public void TC_ADM_U_10_removeEvent_unknownId_listUnchanged() {
        List<Event> result = EventListActivity.removeEventFromList(threeEvents(), "evt_999");

        assertEquals(3, result.size());
    }


    @Test
    public void TC_ADM_U_11_removeEvent_nullList_returnsEmpty() {
        List<Event> result = EventListActivity.removeEventFromList(null, "evt_1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    public void TC_ADM_U_12_updateEvent_existingId_updatesCorrectEvent() {
        Event updated = new Event("evt_2", "Updated Title", "2026-06-01", "Quebec City", "Tech");
        List<Event> result = EventListActivity.updateEventInList(threeEvents(), updated);

        assertEquals(3, result.size());
        assertEquals("Updated Title",  result.get(1).getTitle());
        assertEquals("Quebec City",    result.get(1).getLocation());
        // neighbours untouched
        assertEquals("evt_1", result.get(0).getId());
        assertEquals("evt_3", result.get(2).getId());
    }


    @Test
    public void TC_ADM_U_13_updateEvent_unknownId_listUnchanged() {
        Event updated = new Event("evt_999", "Ghost Event", "2026-06-01", "Ottawa", "Other");
        List<Event> result = EventListActivity.updateEventInList(threeEvents(), updated);

        assertEquals(3, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
    }


    @Test
    public void TC_ADM_U_14_updateEvent_nullList_returnsEmpty() {
        Event updated = new Event("evt_1", "Title", "2026-06-01", "City", "Cat");
        List<Event> result = EventListActivity.updateEventInList(null, updated);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    public void TC_ADM_U_15_addEvent_allFieldsFilled_returnsTrue() {
        assertTrue(AddEventActivity.isValidEventInput(
                "Jazz Night", "2026-05-01", "Montreal", "Music"));
    }


    @Test
    public void TC_ADM_U_16_addEvent_emptyTitle_returnsFalse() {
        assertFalse(AddEventActivity.isValidEventInput(
                "", "2026-05-01", "Montreal", "Music"));
    }


    @Test
    public void TC_ADM_U_17_addEvent_whitespaceTitle_returnsFalse() {
        assertFalse(AddEventActivity.isValidEventInput(
                "   ", "2026-05-01", "Montreal", "Music"));
    }


    @Test
    public void TC_ADM_U_18_addEvent_nullField_returnsFalse() {
        assertFalse(AddEventActivity.isValidEventInput(
                null, "2026-05-01", "Montreal", "Music"));
    }


    @Test
    public void TC_ADM_U_19_editEvent_allFieldsFilled_returnsTrue() {
        assertTrue(EditEventActivity.isValidEventInput(
                "Tech Conf", "2026-04-10", "Palais des Congres", "Technology"));
    }


    @Test
    public void TC_ADM_U_20_editEvent_emptyDate_returnsFalse() {
        assertFalse(EditEventActivity.isValidEventInput(
                "Tech Conf", "", "Palais des Congres", "Technology"));
    }
}

