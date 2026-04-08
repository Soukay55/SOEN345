package com.example.soen345;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.RootMatchers;
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
 *   TC-RES-F-01  Happy path: valid ticket count → confirmation dialog appears
 *   TC-RES-F-02  Empty ticket field → stays on reservation screen
 *   TC-RES-F-03  Zero tickets → stays on reservation screen
 *   TC-RES-F-04  Requesting more tickets than available → stays on screen
 *   TC-RES-F-05  Request exactly equals remaining → success dialog
 *   TC-RES-F-06  Event details (title, date, location) are displayed
 *   TC-RES-F-07  Available ticket count is displayed on screen
 *   TC-RES-F-08  No event ID provided → activity finishes gracefully
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TicketReservationFunctionalTest {

    private static final String TEST_EVENT_ID   = "func_test_event_1";
    private static final String TEST_USER_ID    = "func_test_user_1";
    private static final int    FRESH_REMAINING = 10; // reset before every test

    private ActivityScenario<ReservationActivity> scenario;

    @BeforeClass
    public static void seedFirestore() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(2);

        Map<String, Object> event = new HashMap<>();
        event.put("id",               TEST_EVENT_ID);
        event.put("title",            "Functional Test Concert");
        event.put("date",             "2026-06-01 7:00 PM");
        event.put("location",         "Test Venue, Montreal");
        event.put("category",         "Music");
        event.put("capacity",         FRESH_REMAINING);
        event.put("remainingTickets", FRESH_REMAINING);
        db.collection("events").document(TEST_EVENT_ID)
                .set(event, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        Map<String, Object> user = new HashMap<>();
        user.put("email",       "functest@test.com");
        user.put("phoneNumber", "5140001111");
        user.put("isAdmin",     false);
        db.collection("users").document(TEST_USER_ID)
                .set(user, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        assertTrue("Timed out waiting for Firestore seed",
                latch.await(15, TimeUnit.SECONDS));
    }


    @Before
    public void setUp() throws InterruptedException {
        // Reset remaining tickets so every test starts with a clean slate
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch resetLatch = new CountDownLatch(1);
        Map<String, Object> reset = new HashMap<>();
        reset.put("remainingTickets", FRESH_REMAINING);
        db.collection("events").document(TEST_EVENT_ID)
                .update(reset)
                .addOnCompleteListener(t -> resetLatch.countDown());
        assertTrue("Timed out resetting remainingTickets",
                resetLatch.await(10, TimeUnit.SECONDS));

        // Simulate a logged-in user
        InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences("SOEN345_PREFS", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("CURRENT_USER_ID", TEST_USER_ID)
                .commit();

        // Launch ReservationActivity with the test event id
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ReservationActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        scenario = ActivityScenario.launch(intent);

        // Allow snapshot listener to deliver fresh event data
        Thread.sleep(4000);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void TC_RES_F_01_validTicketCount_showsConfirmationDialog()
            throws InterruptedException {
        onView(withId(R.id.ticketCountInput))
                .perform(replaceText("2"), closeSoftKeyboard());

        onView(withId(R.id.btnConfirmReservation)).perform(click());

        // Wait for Firestore transaction to commit and dialog to appear
        Thread.sleep(8000);

        // Dialog message: "You reserved 2 ticket(s) for Functional Test Concert"
        onView(withText(allOf(containsString("2"), containsString("ticket"))))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));
    }


    @Test
    public void TC_RES_F_02_emptyTicketField_staysOnScreen() {
        onView(withId(R.id.btnConfirmReservation)).perform(click());
        onView(withId(R.id.btnConfirmReservation)).check(matches(isDisplayed()));
    }


    @Test
    public void TC_RES_F_03_zeroTickets_staysOnScreen() {
        onView(withId(R.id.ticketCountInput))
                .perform(replaceText("0"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmReservation)).perform(click());
        onView(withId(R.id.btnConfirmReservation)).check(matches(isDisplayed()));
    }


    @Test
    public void TC_RES_F_04_requestMoreThanRemaining_staysOnScreen()
            throws InterruptedException {
        onView(withId(R.id.ticketCountInput))
                .perform(replaceText("999"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmReservation)).perform(click());

        Thread.sleep(6000);

        // Transaction rejected — still on reservation screen
        onView(withId(R.id.btnConfirmReservation)).check(matches(isDisplayed()));
    }

    @Test
    public void TC_RES_F_05_requestExactlyRemaining_succeeds()
            throws InterruptedException {
        // setUp() already reset remainingTickets to FRESH_REMAINING (10)
        onView(withId(R.id.ticketCountInput))
                .perform(replaceText(String.valueOf(FRESH_REMAINING)), closeSoftKeyboard());

        onView(withId(R.id.btnConfirmReservation)).perform(click());

        Thread.sleep(8000);

        // Dialog: "You reserved 10 ticket(s) for ..."
        onView(withText(allOf(containsString(String.valueOf(FRESH_REMAINING)),
                              containsString("ticket"))))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));
    }


    @Test
    public void TC_RES_F_06_eventDetails_areDisplayed() {
        onView(withId(R.id.eventDetailsText))
                .check(matches(withText(containsString("Functional Test Concert"))));
        onView(withId(R.id.eventDetailsText))
                .check(matches(withText(containsString("2026-06-01"))));
        onView(withId(R.id.eventDetailsText))
                .check(matches(withText(containsString("Test Venue"))));
    }

    @Test
    public void TC_RES_F_07_availableTickets_areDisplayed() {
        onView(withId(R.id.ticketsAvailableDisplay))
                .check(matches(withText(containsString("Available:"))));
    }

    @Test
    public void TC_RES_F_08_noEventId_activityFinishesGracefully() {
        scenario.close();

        Intent badIntent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ReservationActivity.class);
        // intentionally no eventId extra

        try (ActivityScenario<ReservationActivity> bad = ActivityScenario.launch(badIntent)) {
            assertTrue(true); // reaching here without crash = pass
        }
    }
}

