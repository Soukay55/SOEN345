package com.example.soen345;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        db = FirebaseFirestore.getInstance();

        ensureSmsPermission();

        recyclerView = findViewById(R.id.walletRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter(reservations, this::onCancelRequested);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBackToEvents).setOnClickListener(v -> finish());

        loadWallet();
    }

    private void ensureSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }

    private void loadWallet() {
        String uid = getSharedPreferences("SOEN345_PREFS", MODE_PRIVATE)
                .getString("CURRENT_USER_ID", null);

        if (uid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("reservations")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(q -> {
                    reservations.clear();

                    for (QueryDocumentSnapshot doc : q) {
                        Reservation r = doc.toObject(Reservation.class);
                        r.setId(doc.getId());

                        if (r.getStatus() != null && r.getStatus().equalsIgnoreCase("cancelled")) {
                            continue;
                        }

                        reservations.add(r);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load wallet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onCancelRequested(Reservation r) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Reservation")
                .setMessage("Are you sure you want to cancel this reservation? This will return the tickets to the event.")
                .setPositiveButton("Yes", (dialog, which) -> performCancellation(r))
                .setNegativeButton("No", null)
                .show();
    }

    private void performCancellation(Reservation r) {
        if (r == null || r.getId() == null || r.getEventId() == null) {
            Toast.makeText(this, "Invalid reservation", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference resRef = db.collection("reservations").document(r.getId());
        DocumentReference eventRef = db.collection("events").document(r.getEventId());

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

            sendCancellationConfirmation(uid);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void sendCancellationConfirmation(String uid) {
        UserContactHelper.getCurrentUser(db, uid, new UserContactHelper.ContactCallback() {
            @Override
            public void onLoaded(User user) {
                String phone = user != null ? user.getPhoneNumber() : null;
                String message = "Your reservation has been cancelled successfully.";

                boolean sent = NotificationService.sendSms(
                        WalletActivity.this,
                        phone,
                        message
                );

                if (sent) {
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

            @Override
            public void onError(Exception e) {
                Toast.makeText(
                        WalletActivity.this,
                        "Reservation cancelled. Confirmation may be delayed",
                        Toast.LENGTH_SHORT
                ).show();
                loadWallet();
            }
        });
    }
}