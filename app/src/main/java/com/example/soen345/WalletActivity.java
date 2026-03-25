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

import java.util.ArrayList;
import java.util.List;

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

        // Back button to return to events
        findViewById(R.id.btnBackToEvents).setOnClickListener(v -> finish());

        loadWallet();
    }

    private void loadWallet() {
        String uid = getSharedPreferences("SOEN345_PREFS", MODE_PRIVATE).getString("CURRENT_USER_ID", null);
        if (uid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("reservations").whereEqualTo("userId", uid).get().addOnSuccessListener(q -> {
            reservations.clear();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : q) {
                Reservation r = doc.toObject(Reservation.class);
                r.setId(doc.getId());
                // Skip cancelled reservations so they disappear from the wallet
                if (r.getStatus() != null && r.getStatus().equalsIgnoreCase("cancelled")) continue;
                reservations.add(r);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
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
            if (!resSnap.exists()) throw new com.google.firebase.firestore.FirebaseFirestoreException("Reservation not found", com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
            String status = resSnap.getString("status");
            Long ticketsLong = resSnap.getLong("numberOfTickets");
            int tickets = ticketsLong == null ? 0 : ticketsLong.intValue();
            if (status != null && status.equals("cancelled")) throw new com.google.firebase.firestore.FirebaseFirestoreException("Already cancelled", com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);

            DocumentSnapshot eventSnap = tx.get(eventRef);
            if (!eventSnap.exists()) throw new com.google.firebase.firestore.FirebaseFirestoreException("Event not found", com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
            Long remainingLong = eventSnap.getLong("remainingTickets");
            Long capacityLong = eventSnap.getLong("capacity");
            int remaining = remainingLong == null ? 0 : remainingLong.intValue();
            int capacity = capacityLong == null ? 0 : capacityLong.intValue();

            int newRemaining = Math.min(capacity, remaining + tickets);

            // update reservation status and event remaining tickets
            java.util.Map<String, Object> updateRes = new java.util.HashMap<>();
            updateRes.put("status", "cancelled");
            tx.update(resRef, updateRes);

            tx.update(eventRef, "remainingTickets", newRemaining);

            return null;
        }).addOnSuccessListener(a -> {
            Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
            loadWallet();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
