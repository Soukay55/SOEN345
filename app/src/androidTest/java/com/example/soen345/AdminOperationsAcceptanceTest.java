package com.example.soen345;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Acceptance tests for the Admin Operations user story.
 *
 * These tests drive the full end-to-end workflow starting from the Login screen,
 * exactly as a real administrator would use the application. They validate:
 *   - Administrator account access (login authentication)
 *   - Full administrative workflow (add / edit / delete events)
 *   - Admin-specific UI controls (visible/hidden elements by role)
 *   - Non-admin experience is unaffected
 *
 * Test cases:
 *   TC-ADM-A-01  Admin login with valid email → lands on "Welcome, Admin!" screen
 *   TC-ADM-A-02  Admin login → View Events → admin controls visible (Add Event, Edit, Delete)
 *   TC-ADM-A-03  Admin login → View Events → Wallet button hidden
 *   TC-ADM-A-04  Full add-event flow: login → view events → add event → returns to event list
 *   TC-ADM-A-05  Full edit-event flow: login → view events → edit seed event → title updated
 *   TC-ADM-A-06  Full delete-event flow: login → view events → delete event → event gone
 *   TC-ADM-A-07  Non-admin login → View Events → Add Event button hidden, Reserve button visible
 *   TC-ADM-A-08  Invalid login (unknown email) → stays on login screen
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminOperationsAcceptanceTest {

    private static final String ADMIN_EMAIL    = "admin_accept@test.com";
    private static final String ADMIN_USER_ID  = "accept_admin_user";
    private static final String CUSTOMER_EMAIL = "customer_accept@test.com";
    private static final String CUSTOMER_USER_ID = "accept_customer_user";

    private static final String SEED_EVENT_ID    = "accept_seed_event_1";
    private static final String SEED_EVENT_TITLE = "Accept Test Event";

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @BeforeClass
    public static void seedFirestore() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(3);

        // Admin user
        Map<String, Object> admin = new HashMap<>();
        admin.put("email",       ADMIN_EMAIL);
        admin.put("phoneNumber", "5140001234");
        admin.put("isAdmin",     true);
        db.collection("users").document(ADMIN_USER_ID)
                .set(admin, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        // Regular customer
        Map<String, Object> customer = new HashMap<>();
        customer.put("email",       CUSTOMER_EMAIL);
        customer.put("phoneNumber", "5140005678");
        customer.put("isAdmin",     false);
        db.collection("users").document(CUSTOMER_USER_ID)
                .set(customer, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        // Seed event for edit/delete/visibility tests
        Map<String, Object> event = new HashMap<>();
        event.put("id",               SEED_EVENT_ID);
        event.put("title",            SEED_EVENT_TITLE);
        event.put("date",             "2026-10-01 7:00 PM");
        event.put("location",         "Acceptance Test Venue");
        event.put("category",         "Testing");
        event.put("capacity",         30);
        event.put("remainingTickets", 30);
        db.collection("events").document(SEED_EVENT_ID)
                .set(event, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        assertTrue("Timed out seeding Firestore for acceptance tests",
                latch.await(20, TimeUnit.SECONDS));
    }

    // -------------------------------------------------------------------------
    // TC-ADM-A-01
    // -------------------------------------------------------------------------

    /**
     * Scenario: Administrator logs in with a valid email address.
     * Given the login screen is displayed
     * When the admin enters their registered email and submits
     * Then the application navigates to the main screen showing "Welcome, Admin!"
     */
    @Test
    public void TC_ADM_A_01_adminLogin_validEmail_showsWelcomeAdmin()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());

        onView(withId(R.id.btnLoginSubmit)).perform(click());

        Thread.sleep(9000); // wait for Firestore user lookup + navigation

        onView(withId(R.id.welcomeText))
                .check(matches(withText("Welcome, Admin!")));
    }

    // -------------------------------------------------------------------------
    // TC-ADM-A-02
    // -------------------------------------------------------------------------

    /**
     * Scenario: Administrator navigates to the event list and sees admin controls.
     * Given the admin is logged in and on the main screen
     * When the admin taps "View Available Events"
     * Then the event list shows the "Add Event" button
     *  And each event card shows "Edit Event" and "Delete Event" buttons
     */
    @Test
    public void TC_ADM_A_02_adminLogin_viewEvents_adminControlsVisible()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000); // wait for event list to load

        onView(withId(R.id.btnAddEvent))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnEditEvent));
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnDeleteEvent));
    }

    // -------------------------------------------------------------------------
    // TC-ADM-A-03
    // -------------------------------------------------------------------------

    /**
     * Scenario: The "My Wallet" button is not shown to administrators.
     * Given the admin is on the event list screen
     * Then the "My Wallet" button must not be visible
     */
    @Test
    public void TC_ADM_A_03_adminLogin_viewEvents_walletButtonHidden()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.btnMyWallet))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    // -------------------------------------------------------------------------
    // TC-ADM-A-04
    // -------------------------------------------------------------------------

    /**
     * Scenario: Administrator adds a new event through the full UI flow.
     * Given the admin is on the event list screen
     * When the admin taps "Add Event", fills in all required fields, and saves
     * Then the application returns to the event list screen
     *  And the "Add Event" button is still visible (confirming the admin is on the list)
     */
    @Test
    public void TC_ADM_A_04_adminFullFlow_addEvent_returnsToEventList()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000);

        onView(withId(R.id.btnAddEvent)).perform(scrollTo(), click());

        String uniqueTitle = "AcceptEvent_" + System.currentTimeMillis();
        onView(withId(R.id.inputTitle))
                .perform(replaceText(uniqueTitle), closeSoftKeyboard());
        onView(withId(R.id.inputDate))
                .perform(replaceText("2026-11-01"), closeSoftKeyboard());
        onView(withId(R.id.inputLocation))
                .perform(replaceText("Acceptance City"), closeSoftKeyboard());
        onView(withId(R.id.inputCategory))
                .perform(replaceText("AcceptCat"), closeSoftKeyboard());

        onView(withId(R.id.btnSaveEvent)).perform(click());
        Thread.sleep(5000); // wait for Firestore write + finish()

        // Back on EventListActivity — Add Event button confirms we are on the list screen
        onView(withId(R.id.btnAddEvent)).check(matches(isDisplayed()));
    }

    // -------------------------------------------------------------------------
    // TC-ADM-A-05
    // -------------------------------------------------------------------------

    /**
     * Scenario: Administrator edits an existing event through the full UI flow.
     * Given the admin is on the event list and the seed event is visible
     * When the admin taps "Edit" on the seed event card and changes the title
     * Then Firestore reflects the updated title
     */
    @Test
    public void TC_ADM_A_05_adminFullFlow_editEvent_titleUpdatedInFirestore()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000);

        // Confirm seed event is present, then click its Edit button
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(
                        R.id.eventTitle, SEED_EVENT_TITLE));

        onView(withId(R.id.eventsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemWithTitle(
                        SEED_EVENT_TITLE, R.id.btnEditEvent));

        Thread.sleep(1000);

        onView(withId(R.id.inputTitle))
                .perform(replaceText("Accept Updated Title"), closeSoftKeyboard());

        onView(withId(R.id.btnSaveEvent)).perform(click());
        Thread.sleep(5000);

        // Verify Firestore was updated
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] updated = {false};
        db.collection("events").document(SEED_EVENT_ID).get()
                .addOnSuccessListener(snap -> {
                    updated[0] = "Accept Updated Title".equals(snap.getString("title"));
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());
        latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firestore title was not updated by acceptance edit flow", updated[0]);

        // Restore the seed event so subsequent tests are not affected
        CountDownLatch restoreLatch = new CountDownLatch(1);
        Map<String, Object> restore = new HashMap<>();
        restore.put("title", SEED_EVENT_TITLE);
        db.collection("events").document(SEED_EVENT_ID)
                .update(restore)
                .addOnCompleteListener(t -> restoreLatch.countDown());
        restoreLatch.await(10, TimeUnit.SECONDS);
    }

    // -------------------------------------------------------------------------
    // TC-ADM-A-06
    // -------------------------------------------------------------------------

    /**
     * Scenario: Administrator deletes an event through the full UI flow.
     * Given the admin is on the event list and a disposable event is present
     * When the admin taps "Delete" and confirms the dialog
     * Then the event is no longer visible in the list
     */
    @Test
    public void TC_ADM_A_06_adminFullFlow_deleteEvent_eventRemovedFromList()
            throws InterruptedException {
        // Seed a disposable event just for this test
        String deleteId    = "accept_delete_target";
        String deleteTitle = "Accept Delete Me";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch seedLatch = new CountDownLatch(1);
        Map<String, Object> ev = new HashMap<>();
        ev.put("id",               deleteId);
        ev.put("title",            deleteTitle);
        ev.put("date",             "2026-12-01");
        ev.put("location",         "Nowhere");
        ev.put("category",         "Temp");
        ev.put("capacity",         5);
        ev.put("remainingTickets", 5);
        db.collection("events").document(deleteId)
                .set(ev)
                .addOnCompleteListener(t -> seedLatch.countDown());
        assertTrue("Delete seed timed out", seedLatch.await(10, TimeUnit.SECONDS));

        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000);

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(
                        R.id.eventTitle, deleteTitle));

        onView(withId(R.id.eventsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemWithTitle(
                        deleteTitle, R.id.btnDeleteEvent));

        onView(withText("Delete"))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));
        onView(withText("Delete"))
                .inRoot(RootMatchers.isDialog())
                .perform(click());

        Thread.sleep(5000);

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.doesNotContainItemWithText(
                        R.id.eventTitle, deleteTitle));
    }

    // -------------------------------------------------------------------------
    // TC-ADM-A-07
    // -------------------------------------------------------------------------

    /**
     * Scenario: Non-admin user logs in and sees customer-specific UI.
     * Given a regular customer logs in
     * When they navigate to the event list
     * Then the "Add Event" button is hidden
     *  And the "Reserve Tickets" button is visible on event cards
     */
    @Test
    public void TC_ADM_A_07_customerLogin_viewEvents_noAdminControls()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(CUSTOMER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000);

        onView(withId(R.id.btnAddEvent))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnReserve));
    }

    // -------------------------------------------------------------------------
    // TC-ADM-A-08
    // -------------------------------------------------------------------------

    /**
     * Scenario: Login attempt with an unregistered email stays on the login screen.
     * Given the login screen is displayed
     * When the user enters an email that does not exist in Firestore
     * Then the application remains on the login screen
     */
    @Test
    public void TC_ADM_A_08_invalidLogin_unknownEmail_staysOnLoginScreen()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText("nobody_accept@notexist.com"), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());

        Thread.sleep(9000);

        onView(withId(R.id.btnLoginSubmit)).check(matches(isDisplayed()));
        onView(withId(R.id.loginEmail)).check(matches(isDisplayed()));
    }
}
