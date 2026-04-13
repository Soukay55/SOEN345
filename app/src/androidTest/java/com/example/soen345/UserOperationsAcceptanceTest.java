package com.example.soen345;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.matcher.RootMatchers;
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
public class UserOperationsAcceptanceTest {

    private static final String EXISTING_USER_EMAIL = "user_accept_existing@test.com";
    private static final String EXISTING_USER_ID    = "accept_existing_user";

    private static final String EVENT_ID    = "accept_user_event_1";
    private static final String EVENT_TITLE = "User Accept Test Concert";

    @Rule
    public ActivityScenarioRule<RegistrationActivity> activityRule =
            new ActivityScenarioRule<>(RegistrationActivity.class);

    @BeforeClass
    public static void seedFirestore() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(2);

        Map<String, Object> user = new HashMap<>();
        user.put("email",       EXISTING_USER_EMAIL);
        user.put("phoneNumber", "5149990000");
        user.put("isAdmin",     false);
        db.collection("users").document(EXISTING_USER_ID)
                .set(user, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        Map<String, Object> event = new HashMap<>();
        event.put("id",               EVENT_ID);
        event.put("title",            EVENT_TITLE);
        event.put("date",             "2026-11-15 8:00 PM");
        event.put("location",         "User Accept Venue, Montreal");
        event.put("category",         "Music");
        event.put("capacity",         20);
        event.put("remainingTickets", 20);
        db.collection("events").document(EVENT_ID)
                .set(event, SetOptions.merge())
                .addOnCompleteListener(t -> latch.countDown());

        assertTrue("Timed out seeding Firestore for user acceptance tests",
                latch.await(20, TimeUnit.SECONDS));
    }


    @Test
    public void TC_USR_A_01_fullUserJourney_registerLoginReserve_confirmationShown()
            throws InterruptedException {
        String newEmail = "new_user_" + System.currentTimeMillis() + "@test.com";

        onView(withId(R.id.emailInput))
                .perform(typeText(newEmail), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(click());
        Thread.sleep(6000); 

        onView(withId(R.id.loginEmail))
                .perform(typeText(newEmail), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000); 

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000); 

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(
                        R.id.eventTitle, EVENT_TITLE));
        onView(withId(R.id.eventsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemWithTitle(
                        EVENT_TITLE, R.id.btnReserve));
        Thread.sleep(4000); 

        onView(withId(R.id.ticketCountInput))
                .perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmReservation)).perform(click());
        Thread.sleep(8000); 

        onView(withText(allOf(containsString("1"), containsString("ticket"))))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void TC_USR_A_02_registration_validEmail_redirectsToLogin()
            throws InterruptedException {
        String uniqueEmail = "accept_reg_" + System.currentTimeMillis() + "@test.com";

        onView(withId(R.id.emailInput))
                .perform(typeText(uniqueEmail), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(click());
        Thread.sleep(6000);

        onView(withId(R.id.btnLoginSubmit)).check(matches(isDisplayed()));
    }

    @Test
    public void TC_USR_A_03_registration_invalidEmailFormat_staysOnScreen() {
        onView(withId(R.id.emailInput))
                .perform(typeText("notvalidemail"), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(click());

        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
        onView(withId(R.id.emailInput)).check(matches(isDisplayed()));
    }

    @Test
    public void TC_USR_A_04_registration_emptyFields_staysOnScreen() {
        onView(withId(R.id.btnRegister)).perform(click());

        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
    }

    @Test
    public void TC_USR_A_05_registration_duplicateEmail_staysOnScreen()
            throws InterruptedException {
        onView(withId(R.id.emailInput))
                .perform(typeText(EXISTING_USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(click());
        Thread.sleep(6000);

        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
    }

    @Test
    public void TC_USR_A_06_login_validCredentials_showsWelcomeCustomer()
            throws InterruptedException {
        onView(withId(R.id.btnLogin)).perform(click());

        onView(withId(R.id.loginEmail))
                .perform(typeText(EXISTING_USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.welcomeText))
                .check(matches(withText("Welcome, Customer!")));
    }

    @Test
    public void TC_USR_A_07_customerEventList_reserveVisible_addEventHidden()
            throws InterruptedException {
        onView(withId(R.id.btnLogin)).perform(click());

        onView(withId(R.id.loginEmail))
                .perform(typeText(EXISTING_USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000);

        onView(withId(R.id.btnAddEvent))
                .check(matches(androidx.test.espresso.matcher.ViewMatchers
                        .withEffectiveVisibility(
                                androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.itemAtPositionHasVisibleChild(0, R.id.btnReserve));
    }

    @Test
    public void TC_USR_A_08_ticketReservation_confirmationDialogShown()
            throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch resetLatch = new CountDownLatch(1);
        Map<String, Object> reset = new HashMap<>();
        reset.put("remainingTickets", 20);
        db.collection("events").document(EVENT_ID)
                .update(reset)
                .addOnCompleteListener(t -> resetLatch.countDown());
        resetLatch.await(10, TimeUnit.SECONDS);

        onView(withId(R.id.btnLogin)).perform(click());

        onView(withId(R.id.loginEmail))
                .perform(typeText(EXISTING_USER_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLoginSubmit)).perform(click());
        Thread.sleep(9000);

        onView(withId(R.id.btnViewEvents)).perform(click());
        Thread.sleep(5000);

        onView(withId(R.id.eventsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemWithTitle(
                        EVENT_TITLE, R.id.btnReserve));
        Thread.sleep(4000);

        onView(withId(R.id.ticketCountInput))
                .perform(replaceText("2"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmReservation)).perform(click());
        Thread.sleep(8000);

        onView(withText(allOf(containsString("2"), containsString("ticket"))))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));
        onView(withText(allOf(containsString("2"), containsString(EVENT_TITLE))))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void TC_USR_A_09_loginScreen_registerButton_opensRegistrationScreen() {
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.btnGoToRegister)).perform(click());
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
        onView(withId(R.id.emailInput)).check(matches(isDisplayed()));
    }

    @Test
    public void TC_USR_A_10_registrationScreen_loginButton_opensLoginScreen() {
        onView(withId(R.id.btnLogin)).perform(click());

        onView(withId(R.id.btnLoginSubmit)).check(matches(isDisplayed()));
        onView(withId(R.id.loginEmail)).check(matches(isDisplayed()));
    }
}
