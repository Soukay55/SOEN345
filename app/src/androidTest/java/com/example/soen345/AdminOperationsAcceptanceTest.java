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

        Map<String, Object> admin = new HashMap<>();
        admin.put("email",       ADMIN_EMAIL);
        admin.put("phoneNumber", "5140001234");
        admin.put("isAdmin",     true);
        db.collection("users").document(ADMIN_USER_ID)
                .set(admin, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        Map<String, Object> customer = new HashMap<>();
        customer.put("email",       CUSTOMER_EMAIL);
        customer.put("phoneNumber", "5140005678");
        customer.put("isAdmin",     false);
        db.collection("users").document(CUSTOMER_USER_ID)
                .set(customer, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

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


    @Test
    public void TC_ADM_A_01_adminLogin_validEmail_showsWelcomeAdmin()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());

        onView(withId(R.id.btnLoginSubmit)).perform(click());

        Thread.sleep(9000); 

        onView(withId(R.id.welcomeText))
                .check(matches(withText("Welcome, Admin!")));
    }

     */
    @Test
    public void TC_ADM_A_02_adminLogin_viewEvents_adminControlsVisible()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000); 

        onView(withId(R.id.btnAddEvent))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnEditEvent));
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnDeleteEvent));
    }


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
        Thread.sleep(5000); 

        onView(withId(R.id.btnAddEvent)).check(matches(isDisplayed()));
    }

   
    @Test
    public void TC_ADM_A_05_adminFullFlow_editEvent_titleUpdatedInFirestore()
            throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText(ADMIN_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000);

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

        CountDownLatch restoreLatch = new CountDownLatch(1);
        Map<String, Object> restore = new HashMap<>();
        restore.put("title", SEED_EVENT_TITLE);
        db.collection("events").document(SEED_EVENT_ID)
                .update(restore)
                .addOnCompleteListener(t -> restoreLatch.countDown());
        restoreLatch.await(10, TimeUnit.SECONDS);
    }


    @Test
    public void TC_ADM_A_06_adminFullFlow_deleteEvent_eventRemovedFromList()
            throws InterruptedException {
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
