package com.example.soen345;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**

 * Test cases:
 *   TC-ADM-F-01  Add Event: all fields filled → event saved, returns to list
 *   TC-ADM-F-02  Add Event: missing title → stays on Add screen
 *   TC-ADM-F-03  Add Event: missing date → stays on Add screen
 *   TC-ADM-F-04  Add Event: missing location → stays on Add screen
 *   TC-ADM-F-05  Add Event: missing category → stays on Add screen
 *   TC-ADM-F-06  Edit Event: change title → updated title appears in Firestore
 *   TC-ADM-F-07  Edit Event: clear a field → stays on Edit screen
 *   TC-ADM-F-08  Delete Event: confirm delete → event removed from list
 *   TC-ADM-F-09  Delete Event: cancel dialog → event stays in list
 *   TC-ADM-F-10  Admin UI: "Add Event" button visible for admin
 *   TC-ADM-F-11  Admin UI: "My Wallet" button hidden for admin
 *   TC-ADM-F-12  Admin UI: "Edit Event" and "Delete Event" buttons visible on cards
 *   TC-ADM-F-13  Non-admin UI: "Add Event" button hidden for regular user
 *   TC-ADM-F-14  Non-admin UI: "Reserve Tickets" button visible for regular user
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminOperationsFunctionalTest {

    private static final String ADMIN_EVENT_ID   = "adm_func_event_1";
    private static final String ADMIN_EVENT_TITLE = "Admin Func Test Event";

    private ActivityScenario<?> scenario;


    @BeforeClass
    public static void seedFirestore() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Object> event = new HashMap<>();
        event.put("id",               ADMIN_EVENT_ID);
        event.put("title",            ADMIN_EVENT_TITLE);
        event.put("date",             "2026-08-01 6:00 PM");
        event.put("location",         "Admin Test Venue");
        event.put("category",         "Testing");
        event.put("capacity",         50);
        event.put("remainingTickets", 50);
        db.collection("events").document(ADMIN_EVENT_ID)
                .set(event, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        assertTrue("Timed out seeding Firestore", latch.await(15, TimeUnit.SECONDS));
    }


    private ActivityScenario<EventListActivity> launchAsAdmin() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EventListActivity.class);
        intent.putExtra("IS_ADMIN", true);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<EventListActivity> launchAsCustomer() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EventListActivity.class);
        intent.putExtra("IS_ADMIN", false);
        return ActivityScenario.launch(intent);
    }

    /** Restore the seed event so tests that mutate Firestore don't break later tests. */
    private void restoreSeedEvent() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> event = new HashMap<>();
        event.put("id",               ADMIN_EVENT_ID);
        event.put("title",            ADMIN_EVENT_TITLE);
        event.put("date",             "2026-08-01 6:00 PM");
        event.put("location",         "Admin Test Venue");
        event.put("category",         "Testing");
        event.put("capacity",         50);
        event.put("remainingTickets", 50);
        db.collection("events").document(ADMIN_EVENT_ID)
                .set(event)
                .addOnCompleteListener(t -> latch.countDown());
        latch.await(10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }


    @Test
    public void TC_ADM_F_01_addEvent_allFieldsValid_returnsToList()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(4000); // wait for event list to load

        onView(withId(R.id.btnAddEvent)).perform(scrollTo(), click());

        String uniqueTitle = "NewEvent_" + System.currentTimeMillis();
        onView(withId(R.id.inputTitle))
                .perform(replaceText(uniqueTitle), closeSoftKeyboard());
        onView(withId(R.id.inputDate))
                .perform(replaceText("2026-09-01"), closeSoftKeyboard());
        onView(withId(R.id.inputLocation))
                .perform(replaceText("Test City"), closeSoftKeyboard());
        onView(withId(R.id.inputCategory))
                .perform(replaceText("TestCat"), closeSoftKeyboard());

        onView(withId(R.id.btnSaveEvent)).perform(click());

        Thread.sleep(5000); // wait for Firestore write + finish()

        // After finish(), we are back on EventListActivity
        onView(withId(R.id.btnAddEvent)).check(matches(isDisplayed()));
    }


    @Test
    public void TC_ADM_F_02_addEvent_missingTitle_staysOnScreen()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(4000);

        onView(withId(R.id.btnAddEvent)).perform(scrollTo(), click());

        // Leave title empty
        onView(withId(R.id.inputDate))
                .perform(replaceText("2026-09-01"), closeSoftKeyboard());
        onView(withId(R.id.inputLocation))
                .perform(replaceText("Test City"), closeSoftKeyboard());
        onView(withId(R.id.inputCategory))
                .perform(replaceText("TestCat"), closeSoftKeyboard());

        onView(withId(R.id.btnSaveEvent)).perform(click());

        // Still on Add screen
        onView(withId(R.id.btnSaveEvent)).check(matches(isDisplayed()));
    }


    @Test
    public void TC_ADM_F_03_addEvent_missingDate_staysOnScreen()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(4000);

        onView(withId(R.id.btnAddEvent)).perform(scrollTo(), click());

        onView(withId(R.id.inputTitle))
                .perform(replaceText("Some Event"), closeSoftKeyboard());
        // Leave date empty
        onView(withId(R.id.inputLocation))
                .perform(replaceText("Test City"), closeSoftKeyboard());
        onView(withId(R.id.inputCategory))
                .perform(replaceText("TestCat"), closeSoftKeyboard());

        onView(withId(R.id.btnSaveEvent)).perform(click());

        onView(withId(R.id.btnSaveEvent)).check(matches(isDisplayed()));
    }


    @Test
    public void TC_ADM_F_04_addEvent_missingLocation_staysOnScreen()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(4000);

        onView(withId(R.id.btnAddEvent)).perform(scrollTo(), click());

        onView(withId(R.id.inputTitle))
                .perform(replaceText("Some Event"), closeSoftKeyboard());
        onView(withId(R.id.inputDate))
                .perform(replaceText("2026-09-01"), closeSoftKeyboard());
        // Leave location empty
        onView(withId(R.id.inputCategory))
                .perform(replaceText("TestCat"), closeSoftKeyboard());

        onView(withId(R.id.btnSaveEvent)).perform(click());

        onView(withId(R.id.btnSaveEvent)).check(matches(isDisplayed()));
    }


    @Test
    public void TC_ADM_F_05_addEvent_missingCategory_staysOnScreen()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(4000);

        onView(withId(R.id.btnAddEvent)).perform(scrollTo(), click());

        onView(withId(R.id.inputTitle))
                .perform(replaceText("Some Event"), closeSoftKeyboard());
        onView(withId(R.id.inputDate))
                .perform(replaceText("2026-09-01"), closeSoftKeyboard());
        onView(withId(R.id.inputLocation))
                .perform(replaceText("Test City"), closeSoftKeyboard());
        // Leave category empty

        onView(withId(R.id.btnSaveEvent)).perform(click());

        onView(withId(R.id.btnSaveEvent)).check(matches(isDisplayed()));
    }


    @Test
    public void TC_ADM_F_06_editEvent_validFields_returnsToList()
            throws InterruptedException {
        // Launch EditEventActivity directly with the seed event's data
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EditEventActivity.class);
        intent.putExtra("eventId",  ADMIN_EVENT_ID);
        intent.putExtra("title",    ADMIN_EVENT_TITLE);
        intent.putExtra("date",     "2026-08-01 6:00 PM");
        intent.putExtra("location", "Admin Test Venue");
        intent.putExtra("category", "Testing");
        scenario = ActivityScenario.launch(intent);
        Thread.sleep(1000);

        // Change the title
        onView(withId(R.id.inputTitle))
                .perform(replaceText("Updated Admin Event"), closeSoftKeyboard());

        onView(withId(R.id.btnSaveEvent)).perform(click());

        Thread.sleep(5000); // wait for Firestore write + finish()

        // Activity should have finished — verify by checking Firestore directly
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] titleUpdated = {false};
        db.collection("events").document(ADMIN_EVENT_ID).get()
                .addOnSuccessListener(snap -> {
                    String title = snap.getString("title");
                    titleUpdated[0] = "Updated Admin Event".equals(title);
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());
        latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firestore title was not updated", titleUpdated[0]);

        // Restore the seed event so other tests aren't affected
        restoreSeedEvent();
    }


    @Test
    public void TC_ADM_F_07_editEvent_clearField_staysOnScreen()
            throws InterruptedException {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EditEventActivity.class);
        intent.putExtra("eventId",  ADMIN_EVENT_ID);
        intent.putExtra("title",    ADMIN_EVENT_TITLE);
        intent.putExtra("date",     "2026-08-01 6:00 PM");
        intent.putExtra("location", "Admin Test Venue");
        intent.putExtra("category", "Testing");
        scenario = ActivityScenario.launch(intent);
        Thread.sleep(1000);

        // Clear the title field
        onView(withId(R.id.inputTitle))
                .perform(replaceText(""), closeSoftKeyboard());

        onView(withId(R.id.btnSaveEvent)).perform(click());

        // Still on Edit screen (button text is "Update Event")
        onView(withId(R.id.btnSaveEvent)).check(matches(withText("Update Event")));
    }


    @Test
    public void TC_ADM_F_08_deleteEvent_confirm_eventRemovedFromList()
            throws InterruptedException {
        // Seed a disposable event specifically for this delete test
        String deleteId = "adm_delete_target";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch seedLatch = new CountDownLatch(1);
        Map<String, Object> ev = new HashMap<>();
        ev.put("id", deleteId);
        ev.put("title", "Delete Me Event");
        ev.put("date", "2026-08-10");
        ev.put("location", "Nowhere");
        ev.put("category", "Temp");
        ev.put("capacity", 5);
        ev.put("remainingTickets", 5);
        db.collection("events").document(deleteId)
                .set(ev)
                .addOnCompleteListener(t -> seedLatch.countDown());
        assertTrue("Seed timed out", seedLatch.await(10, TimeUnit.SECONDS));

        scenario = launchAsAdmin();
        Thread.sleep(5000); // wait for list to load

        // Find the "Delete Me Event" card and click its delete button
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(R.id.eventTitle, "Delete Me Event"));

        // Use RecyclerView action helper to click the delete button on the matching card
        onView(withId(R.id.eventsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemWithTitle("Delete Me Event", R.id.btnDeleteEvent));

        // Confirm the dialog
        onView(withText("Delete"))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));
        onView(withText("Delete"))
                .inRoot(RootMatchers.isDialog())
                .perform(click());

        Thread.sleep(5000); // wait for Firestore delete + reload

        // "Delete Me Event" must no longer be in the list
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.doesNotContainItemWithText(
                        R.id.eventTitle, "Delete Me Event"));
    }


    @Test
    public void TC_ADM_F_09_deleteEvent_cancel_eventStaysInList()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(5000);

        // Confirm the seed event is present
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(
                        R.id.eventTitle, ADMIN_EVENT_TITLE));

        // Click delete on the seed event card
        onView(withId(R.id.eventsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemWithTitle(
                        ADMIN_EVENT_TITLE, R.id.btnDeleteEvent));

        // Tap CANCEL in the dialog
        onView(withText("Cancel"))
                .inRoot(RootMatchers.isDialog())
                .perform(click());

        Thread.sleep(1000);

        // Event must still be visible
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(
                        R.id.eventTitle, ADMIN_EVENT_TITLE));
    }


    @Test
    public void TC_ADM_F_10_adminUI_addEventButtonVisible()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(3000);

        onView(withId(R.id.btnAddEvent))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }


    @Test
    public void TC_ADM_F_11_adminUI_walletButtonHidden()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(3000);

        onView(withId(R.id.btnMyWallet))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }


    @Test
    public void TC_ADM_F_12_adminUI_editAndDeleteButtonsVisibleOnCards()
            throws InterruptedException {
        scenario = launchAsAdmin();
        Thread.sleep(5000);

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(
                        R.id.eventTitle, ADMIN_EVENT_TITLE));

        // Check first card has Edit and Delete buttons visible
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnEditEvent));
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnDeleteEvent));
    }


    @Test
    public void TC_ADM_F_13_customerUI_addEventButtonHidden()
            throws InterruptedException {
        scenario = launchAsCustomer();
        Thread.sleep(3000);

        onView(withId(R.id.btnAddEvent))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }


    @Test
    public void TC_ADM_F_14_customerUI_reserveButtonVisibleOnCards()
            throws InterruptedException {
        scenario = launchAsCustomer();
        Thread.sleep(5000);

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnReserve));
    }
}

