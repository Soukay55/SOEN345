package com.example.soen345;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.walletRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter(reservations, this::onCancelRequested);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBackToEvents).setOnClickListener(v -> finish());

        loadWallet();
    }

    private void loadWallet() {
        String uid = getSharedPreferences("SOEN345_PREFS", MODE_PRIVATE)
                .getString("CURRENT_USER_ID", null);

        if (uid == null || uid.trim().isEmpty()) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("reservations")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    reservations.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        Reservation reservation = doc.toObject(Reservation.class);
                        reservation.setId(doc.getId());

                        if (reservation.getStatus() != null
                                && reservation.getStatus().equalsIgnoreCase("cancelled")) {
                            continue;
                        }

                        reservations.add(reservation);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Failed to load wallet: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void onCancelRequested(Reservation reservation) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Reservation")
                .setMessage("Are you sure you want to cancel this reservation? This will return the tickets to the event.")
                .setPositiveButton("Yes", (dialog, which) -> performCancellation(reservation))
                .setNegativeButton("No", null)
                .show();
    }

    private void performCancellation(Reservation reservation) {
        if (reservation == null || reservation.getId() == null || reservation.getEventId() == null) {
            Toast.makeText(this, "Invalid reservation", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference resRef = db.collection("reservations").document(reservation.getId());
        DocumentReference eventRef = db.collection("events").document(reservation.getEventId());

        db.runTransaction(tx -> {
            DocumentSnapshot resSnap = tx.get(resRef);

            if (!resSnap.exists()) {
                throw new FirebaseFirestoreException(
                        "Reservation not found",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            String status = resSnap.getString("status");
            Long ticketsLong = resSnap.getLong("numberOfTickets");
            int tickets = ticketsLong == null ? 0 : ticketsLong.intValue();

            if (status != null && status.equalsIgnoreCase("cancelled")) {
                throw new FirebaseFirestoreException(
                        "Already cancelled",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            DocumentSnapshot eventSnap = tx.get(eventRef);

            if (!eventSnap.exists()) {
                throw new FirebaseFirestoreException(
                        "Event not found",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            Long remainingLong = eventSnap.getLong("remainingTickets");
            Long capacityLong = eventSnap.getLong("capacity");

            int remaining = remainingLong == null ? 0 : remainingLong.intValue();
            int capacity = capacityLong == null ? 0 : capacityLong.intValue();
            int newRemaining = Math.min(capacity, remaining + tickets);

            Map<String, Object> updateRes = new HashMap<>();
            updateRes.put("status", "cancelled");

            tx.update(resRef, updateRes);
            tx.update(eventRef, "remainingTickets", newRemaining);

            return null;
        }).addOnSuccessListener(a -> {
            String uid = getSharedPreferences("SOEN345_PREFS", MODE_PRIVATE)
                    .getString("CURRENT_USER_ID", null);

            db.collection("events").document(reservation.getEventId())
                    .get()
                    .addOnSuccessListener(eventSnap -> {
                        Event event = eventSnap.toObject(Event.class);

                        if (event == null) {
                            event = new Event();
                            event.setTitle("Event");
                            event.setDate("");
                        } else {
                            event.setId(eventSnap.getId());
                        }

                        NotificationService.notifyBookingCancelled(
                                db,
                                uid,
                                event,
                                (success, error) -> {
                                    if (success) {
                                        Toast.makeText(
                                                WalletActivity.this,
                                                "Reservation cancelled",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    } else {
                                        Toast.makeText(
                                                WalletActivity.this,
                                                "Reservation cancelled. Confirmation may be delayed",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    loadWallet();
                                }
                        );
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(
                                WalletActivity.this,
                                "Reservation cancelled. Confirmation may be delayed",
                                Toast.LENGTH_SHORT
                        ).show();
                        loadWallet();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}