package com.example.soen345;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ReservationActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Event event;
    private EditText ticketCountInput;
    private TextView eventDetailsText, ticketsAvailableDisplay;
    private Button btnConfirmReservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        db = FirebaseFirestore.getInstance();

        ticketCountInput = findViewById(R.id.ticketCountInput);
        eventDetailsText = findViewById(R.id.eventDetailsText);
        ticketsAvailableDisplay = findViewById(R.id.ticketsAvailableDisplay);
        btnConfirmReservation = findViewById(R.id.btnConfirmReservation);

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "No event provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("events").document(eventId).addSnapshotListener((snap, err) -> {
            if (err != null) {
                return;
            }

            if (snap != null && snap.exists()) {
                event = snap.toObject(Event.class);
                if (event != null) {
                    event.setId(snap.getId());
                    updateDisplay();
                }
            }
        });

        btnConfirmReservation.setOnClickListener(v -> onConfirm());
    }

    private void updateDisplay() {
        if (event == null) {
            return;
        }

        eventDetailsText.setText(
                event.getTitle() + "\n" + event.getDate() + "\n" + event.getLocation()
        );

        ticketsAvailableDisplay.setText(
                "Available: " + event.getRemainingTickets() + " / " + event.getCapacity()
        );
    }

    private void onConfirm() {
        if (event == null) {
            Toast.makeText(this, "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String s = ticketCountInput.getText().toString().trim();
        int tickets;

        try {
            tickets = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid number of tickets", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tickets <= 0) {
            Toast.makeText(this, "Ticket count must be positive", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = getSharedPreferences("SOEN345_PREFS", MODE_PRIVATE)
                .getString("CURRENT_USER_ID", null);

        if (uid == null || uid.trim().isEmpty()) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        final int ticketsRequested = tickets;
        final String currentUserId = uid;
        final String eventId = event.getId();
        final DocumentReference eventRef = db.collection("events").document(eventId);
        final DocumentReference reservationRef = db.collection("reservations").document();

        db.runTransaction(tx -> {
            DocumentSnapshot eventSnap = tx.get(eventRef);

            if (!eventSnap.exists()) {
                throw new FirebaseFirestoreException(
                        "Event missing",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            Long remainingLong = eventSnap.getLong("remainingTickets");
            int remaining = remainingLong == null ? 0 : remainingLong.intValue();

            if (remaining < ticketsRequested) {
                throw new FirebaseFirestoreException(
                        "Not enough tickets",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            Reservation reservation = new Reservation();
            reservation.setUserId(currentUserId);
            reservation.setEventId(eventId);
            reservation.setNumberOfTickets(ticketsRequested);
            reservation.setReservationDate(new java.util.Date());
            reservation.setStatus("confirmed");

            tx.set(reservationRef, reservation);
            tx.update(eventRef, "remainingTickets", remaining - ticketsRequested);

            return null;
        }).addOnSuccessListener(a -> {
            NotificationService.notifyBookingConfirmed(
                    db,
                    currentUserId,
                    event,
                    (success, error) -> {
                        if (!success) {
                            Toast.makeText(
                                    ReservationActivity.this,
                                    "Confirmation may be delayed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        showReservationSuccessDialog(ticketsRequested);
                    }
            );
        }).addOnFailureListener(e -> {
            String message = e.getMessage() != null ? e.getMessage() : "Reservation failed";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void showReservationSuccessDialog(int ticketsRequested) {
        new AlertDialog.Builder(this)
                .setTitle("Reservation confirmed")
                .setMessage("You reserved " + ticketsRequested + " ticket(s) for " + event.getTitle())
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(this, EventListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}

