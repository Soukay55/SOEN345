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
        if (eventId == null) {
            Toast.makeText(this, "No event provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("events").document(eventId).addSnapshotListener((snap, err) -> {
            if (err != null) return;
            if (snap != null && snap.exists()) {
                event = snap.toObject(Event.class);
                event.setId(snap.getId());
                updateDisplay();
            }
        });

        btnConfirmReservation.setOnClickListener(v -> onConfirm());
    }

    private void updateDisplay() {
        if (event == null) return;
        eventDetailsText.setText(event.getTitle() + "\n" + event.getDate() + "\n" + event.getLocation());
        ticketsAvailableDisplay.setText("Available: " + event.getRemainingTickets() + " / " + event.getCapacity());
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

        // get current user id from SharedPreferences
        String uid = getSharedPreferences("SOEN345_PREFS", MODE_PRIVATE).getString("CURRENT_USER_ID", null);
        if (uid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        final int ticketsRequested = tickets;
        final String currentUserId = uid;
        final String eventId = event.getId();
        final DocumentReference eventRef = db.collection("events").document(eventId);
        final DocumentReference reservationsRef = db.collection("reservations").document();

        db.runTransaction(tx -> {
            DocumentSnapshot sEvent = tx.get(eventRef);
            if (!sEvent.exists()) throw new FirebaseFirestoreException("Event missing", FirebaseFirestoreException.Code.ABORTED);
            Long remL = sEvent.getLong("remainingTickets");
            int remaining = remL == null ? 0 : remL.intValue();
            if (remaining < ticketsRequested) throw new FirebaseFirestoreException("Not enough tickets", FirebaseFirestoreException.Code.ABORTED);

            // create reservation
            Reservation r = new Reservation();
            r.setUserId(currentUserId);
            r.setEventId(eventId);
            r.setNumberOfTickets(ticketsRequested);
            r.setReservationDate(new java.util.Date());
            r.setStatus("confirmed");

            tx.set(reservationsRef, r);
            tx.update(eventRef, "remainingTickets", remaining - ticketsRequested);
            return null;
        }).addOnSuccessListener(a -> {

            // confirmation dialog then return to event list
            new AlertDialog.Builder(this)
                    .setTitle("Reservation confirmed")
                    .setMessage("You reserved " + ticketsRequested + " ticket(s) for " + event.getTitle())
                    .setPositiveButton("OK", (d, w) -> {
                        // return to event list (finish)
                        Intent intent = new Intent(this, EventListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }).show();
        }).addOnFailureListener(e -> {
            String m = e.getMessage() != null ? e.getMessage() : "Reservation failed";
            Toast.makeText(this, m, Toast.LENGTH_LONG).show();
        });
    }
}

