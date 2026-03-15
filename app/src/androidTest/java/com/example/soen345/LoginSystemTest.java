package com.example.soen345;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginSystemTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @BeforeClass
    public static void seedTestUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        DataSeeder.seedEventsAndTestUser(FirebaseFirestore.getInstance(), latch::countDown);
        assertTrue("Timed out waiting for Firestore seed data", latch.await(15, TimeUnit.SECONDS));
    }

    @Test
    public void validEmailLogin_navigatesToMainActivity() throws InterruptedException {
        onView(withId(R.id.loginEmail))
                .perform(typeText("test@test.com"), closeSoftKeyboard());

        onView(withId(R.id.btnLoginSubmit)).perform(click());

        Thread.sleep(9000);

        onView(withId(R.id.welcomeText))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyCredentials_staysOnLoginScreen() {
        onView(withId(R.id.btnLoginSubmit)).perform(click());

        onView(withId(R.id.btnLoginSubmit)).check(matches(isDisplayed()));
        onView(withId(R.id.loginEmail)).check(matches(isDisplayed()));
    }

    @Test
    public void registerButton_opensRegistrationActivity() {
        onView(withId(R.id.btnGoToRegister)).perform(click());
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
    }
}
