package com.example.soen345;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserContactHelper {

    public interface ContactCallback {
        void onLoaded(User user);
        void onError(Exception e);
    }

    public static void getCurrentUser(FirebaseFirestore db, String userId, ContactCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("Missing user ID"));
            }
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        if (callback != null) {
                            callback.onError(new Exception("User not found"));
                        }
                        return;
                    }

                    User user = snapshot.toObject(User.class);
                    if (callback != null) {
                        callback.onLoaded(user);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }
}