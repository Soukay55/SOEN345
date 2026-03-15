package com.example.soen345;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Seeds the Firestore database with sample events.
 * Uses fixed document IDs (event_1, event_2, ...) so running this multiple
 * times is safe — it will update existing docs but never create duplicates.
 *
 * TO ADD A NEW EVENT: add a new line below and increment the ID.
 */
public class DataSeeder {

    public static void seedEventsIfEmpty(FirebaseFirestore db) {
        seedEventsAndTestUser(db, null);
    }

    public static void seedEventsAndTestUser(FirebaseFirestore db, Runnable onComplete) {
        List<Event> sampleEvents = Arrays.asList(
            new Event("event_1", "Jazz Night",         "2026-03-15 8:00 PM",  "Montreal Jazz Club",     "Music"),
            new Event("event_2", "Comedy Show",        "2026-03-20 9:00 PM",  "Comedy Works, Downtown", "Comedy"),
            new Event("event_3", "Tech Conference",    "2026-04-01 10:00 AM", "Palais des Congrès",     "Technology"),
            new Event("event_4", "Art Exhibition",     "2026-04-10 11:00 AM", "Musée des Beaux-Arts",   "Art"),
            new Event("event_5", "Food & Wine Fest",   "2026-04-25 12:00 PM", "Old Port, Montreal",     "Food"),
            new Event("event_6", "Rock Concert",       "2026-05-10 7:00 PM",  "Bell Centre, Montreal",  "Music"),
            new Event("event_7", "Startup Networking", "2026-05-18 6:00 PM",  "Notman House",           "Technology")
            // ← Add new events here, increment the ID each time
        );

        List<com.google.android.gms.tasks.Task<?>> writes = new ArrayList<>();
        for (Event event : sampleEvents) {
            writes.add(db.collection("events").document(event.getId()).set(event, SetOptions.merge()));
        }

        // Seed a fixed test user so LoginSystemTest (TC-LGN-01) always has a valid account.
        // Uses a fixed document ID so this is safe to run multiple times.
        User testUser = new User("test_user_seed", "test@test.com", "5140000000", false);
        writes.add(db.collection("users").document("test_user_seed").set(testUser, SetOptions.merge()));

        Tasks.whenAllComplete(writes).addOnCompleteListener(task -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
