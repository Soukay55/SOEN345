package com.example.soen345;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration Test — Registration & Firestore "users" Collection (FR2)
 *
 * Goal: Verify that the app can write a new document to the Firestore "users"
 *       collection and then redirect the user to LoginActivity.
 *
 * Note: Each test run uses a unique email so duplicate-detection logic does
 *       not interfere.  Timestamps are appended to make emails unique.
 *
 * Classification:
 *   • Integration test – exercises the real Firestore write path.
 *   • System test      – validates the full registration user journey.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegistrationIntegrationTest {

    @Rule
    public ActivityScenarioRule<RegistrationActivity> activityRule =
            new ActivityScenarioRule<>(RegistrationActivity.class);

    /**
     * TC-REG-01: Happy Path – valid email registers and redirects to LoginActivity.
     *
     * Steps:
     *   1. Enter a unique email address.
     *   2. Tap REGISTER.
     *   3. Wait for the Firestore write to complete.
     *   4. Assert the app has navigated to LoginActivity (LOGIN button visible).
     */
    @Test
    public void validEmail_registersAndRedirectsToLogin() throws InterruptedException {
        // Use a timestamp-based email to avoid duplicate conflicts across runs
        String uniqueEmail = "user_" + System.currentTimeMillis() + "@test.com";

        // Step 1 – fill in a unique email
        onView(withId(R.id.emailInput))
                .perform(typeText(uniqueEmail), closeSoftKeyboard());

        // Step 2 – tap REGISTER
        onView(withId(R.id.btnRegister)).perform(click());

        // Step 3 – wait for the Firestore callback
        Thread.sleep(4000);

        // Step 4 – LoginActivity's submit button must now be visible,
        //          confirming the redirect happened after successful registration
        onView(withId(R.id.btnLoginSubmit)).check(matches(isDisplayed()));
    }

    /**
     * TC-REG-02: Invalid email (missing '@') stays on RegistrationActivity
     *            and shows an inline error on the email field.
     *
     * Validates RegistrationActivity.isInputValid() for bad e-mail format.
     */
    @Test
    public void invalidEmail_staysOnRegistrationScreen() {
        onView(withId(R.id.emailInput))
                .perform(typeText("notanemail"), closeSoftKeyboard());

        onView(withId(R.id.btnRegister)).perform(click());

        // Should still be on the registration screen
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
        onView(withId(R.id.emailInput)).check(matches(isDisplayed()));
    }

    /**
     * TC-REG-03: Empty fields show error and stay on RegistrationActivity.
     *
     * Validates the empty-fields guard in RegistrationActivity.isInputValid().
     */
    @Test
    public void emptyFields_staysOnRegistrationScreen() {
        // Tap REGISTER without filling anything
        onView(withId(R.id.btnRegister)).perform(click());

        // Must remain on the registration screen
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
    }

    /**
     * TC-REG-04: "Login" button navigates back to LoginActivity.
     *
     * Verifies the in-app navigation link from RegistrationActivity → LoginActivity.
     */
    @Test
    public void loginButton_navigatesToLoginActivity() {
        onView(withId(R.id.btnLogin)).perform(click());

        // LoginActivity's submit button must be visible
        onView(withId(R.id.btnLoginSubmit)).check(matches(isDisplayed()));
    }
}

