package com.example.soen345;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.RemoteException;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventSearchFilterSystemTest {

    @BeforeClass
    public static void disableAnimations() throws RemoteException, java.io.IOException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.executeShellCommand("settings put global window_animation_scale 0");
        device.executeShellCommand("settings put global transition_animation_scale 0");
        device.executeShellCommand("settings put global animator_duration_scale 0");
    }

    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    /**
     * TC-FILTER-01: Category filter "Music" shows only music events.
     *
     * Steps:
     *   1. Wait for the Firestore event list to load.
     *   2. Type "Music" into the filterCategory field.
     *   3. Tap "Apply Filters".
     *   4. Assert "Jazz Night" is visible in the RecyclerView (scoped to eventTitle).
     *   5. Assert "Tech Conference" title does NOT exist anywhere.
     */
    @Test
    public void categoryFilter_music_showsJazzNightHidesTechConference()
            throws InterruptedException {

        // Step 1 – allow Firestore to load all events
        Thread.sleep(5000);

        // Step 2 – enter the category filter
        onView(withId(R.id.filterCategory))
                .perform(typeText("Music"), closeSoftKeyboard());

        // Step 3 – apply filters
        onView(withId(R.id.btnApplyFilters)).perform(click());

        // Step 4 – check "Jazz Night" is displayed, and "Tech Conference" does not exist
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(R.id.eventTitle, "Jazz Night"))
                .check(RecyclerViewItemAssertion.doesNotContainItemWithText(R.id.eventTitle, "Tech Conference"));
    }

    /**
     * TC-FILTER-02: Title search "Tech" shows only tech-titled events.
     *
     * Steps:
     *   1. Wait for events to load.
     *   2. Type "Tech" into the searchInput field.
     *   3. Apply filters.
     *   4. Assert "Tech Conference" eventTitle is visible.
     *   5. Assert "Jazz Night" eventTitle does NOT exist.
     */
    @Test
    public void titleSearch_tech_showsTechConferenceHidesJazzNight()
            throws InterruptedException {

        // Step 1 – wait for data
        Thread.sleep(5000);

        // Step 2 – enter a title search
        onView(withId(R.id.searchInput))
                .perform(typeText("Tech"), closeSoftKeyboard());

        // Step 3 – apply
        onView(withId(R.id.btnApplyFilters)).perform(click());

        // Step 4 – check "Tech Conference" is displayed, and "Jazz Night" does not exist
        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(R.id.eventTitle, "Tech Conference"))
                .check(RecyclerViewItemAssertion.doesNotContainItemWithText(R.id.eventTitle, "Jazz Night"));
    }

    /**
     * TC-FILTER-03: Clear Filters restores the full event list.
     *
     * Steps:
     *   1. Apply a restrictive category filter.
     *   2. Tap "Clear Filters".
     *   3. Assert both "Jazz Night" and "Tech Conference" are visible again.
     */
    @Test
    public void clearFilters_restoresFullEventList() throws InterruptedException {

        // Step 1 – wait and apply a filter
        Thread.sleep(5000);

        onView(withId(R.id.filterCategory))
                .perform(typeText("Music"), closeSoftKeyboard());
        onView(withId(R.id.btnApplyFilters)).perform(click());
        onView(withId(R.id.btnClearFilters)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.eventsRecyclerView))
                .check(RecyclerViewItemAssertion.containsItemWithText(R.id.eventTitle, "Jazz Night"))
                .check(RecyclerViewItemAssertion.containsItemWithText(R.id.eventTitle, "Tech Conference"));
    }
}
