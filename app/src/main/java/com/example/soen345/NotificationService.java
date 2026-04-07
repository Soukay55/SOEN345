package com.example.soen345;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {

    public interface Callback {
        void onComplete(boolean success, String error);
    }

    public static void notifyBookingConfirmed(
            FirebaseFirestore db,
            String userId,
            Event event,
            Callback callback
    ) {
        if (event == null) {
            if (callback != null) callback.onComplete(false, "Event required");
            return;
        }

        String subject = "Booking Confirmed";
        String message = "Your reservation for \"" + event.getTitle()
                + "\" on " + event.getDate()
                + " is confirmed.";

        fetchUserAndSend(db, userId, subject, message, callback);
    }

    public static void notifyBookingCancelled(
            FirebaseFirestore db,
            String userId,
            Event event,
            Callback callback
    ) {
        if (event == null) {
            if (callback != null) callback.onComplete(false, "Event required");
            return;
        }

        String subject = "Booking Cancelled";
        String message = "Your reservation for \"" + event.getTitle()
                + "\" on " + event.getDate()
                + " has been cancelled.";

        fetchUserAndSend(db, userId, subject, message, callback);
    }

    private static void fetchUserAndSend(
            FirebaseFirestore db,
            String userId,
            String subject,
            String message,
            Callback callback
    ) {
        String trimmedUserId = userId == null ? "" : userId.trim();

        if (trimmedUserId.isEmpty()) {
            if (callback != null) callback.onComplete(false, "User ID required");
            return;
        }

        db.collection("users").document(trimmedUserId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        if (callback != null) callback.onComplete(false, "User not found");
                        return;
                    }

                    User user = snapshot.toObject(User.class);
                    if (user == null) {
                        if (callback != null) callback.onComplete(false, "User not found");
                        return;
                    }

                    sendNotification(db, user, subject, message, callback);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onComplete(false, e.getMessage() != null ? e.getMessage() : "Failed to load user");
                    }
                });
    }

    private static void sendNotification(
            FirebaseFirestore db,
            User user,
            String subject,
            String message,
            Callback callback
    ) {
        boolean hasEmail = user.getEmail() != null && !user.getEmail().trim().isEmpty();
        boolean hasPhone = user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty();

        if (!hasEmail && !hasPhone) {
            if (callback != null) callback.onComplete(false, "User has no email or phone");
            return;
        }

        if (hasEmail) {
            queueEmail(db, user.getEmail(), subject, message, (emailSuccess, emailError) -> {
                if (emailSuccess) {
                    if (callback != null) callback.onComplete(true, null);
                } else if (hasPhone) {
                    queueSms(db, user.getPhoneNumber(), message, (smsSuccess, smsError) -> {
                        if (callback != null) {
                            callback.onComplete(
                                    smsSuccess,
                                    smsSuccess ? null : (smsError != null ? smsError : "Failed to send notification")
                            );
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onComplete(false, emailError != null ? emailError : "Failed to send email");
                    }
                }
            });
        } else {
            queueSms(db, user.getPhoneNumber(), message, (smsSuccess, smsError) -> {
                if (callback != null) {
                    callback.onComplete(
                            smsSuccess,
                            smsSuccess ? null : (smsError != null ? smsError : "Failed to send SMS")
                    );
                }
            });
        }
    }

    private static void queueEmail(
            FirebaseFirestore db,
            String to,
            String subject,
            String body,
            Callback callback
    ) {
        if (to == null || to.trim().isEmpty()) {
            if (callback != null) callback.onComplete(false, "Recipient email required");
            return;
        }

        if (subject == null || subject.trim().isEmpty()) {
            if (callback != null) callback.onComplete(false, "Subject required");
            return;
        }

        if (body == null || body.trim().isEmpty()) {
            if (callback != null) callback.onComplete(false, "Body required");
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("subject", subject);
        message.put("text", body);

        Map<String, Object> mailDoc = new HashMap<>();
        mailDoc.put("to", to.trim());
        mailDoc.put("message", message);

        db.collection("mail")
                .add(mailDoc)
                .addOnSuccessListener(docRef -> {
                    if (callback != null) callback.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onComplete(false, e.getMessage() != null ? e.getMessage() : "Failed to queue email");
                    }
                });
    }

    private static void queueSms(
            FirebaseFirestore db,
            String to,
            String message,
            Callback callback
    ) {
        if (to == null || to.trim().isEmpty()) {
            if (callback != null) callback.onComplete(false, "Recipient phone number required");
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            if (callback != null) callback.onComplete(false, "Message required");
            return;
        }

        Map<String, Object> smsDoc = new HashMap<>();
        smsDoc.put("to", to.trim());
        smsDoc.put("message", message);

        db.collection("sms")
                .add(smsDoc)
                .addOnSuccessListener(docRef -> {
                    if (callback != null) callback.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onComplete(false, e.getMessage() != null ? e.getMessage() : "Failed to queue SMS");
                    }
                });
    }
}