package com.example.soen345;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        db = FirebaseFirestore.getInstance();

        ticketCountInput = findViewById(R.id.ticketCountInput);
        eventDetailsText = findViewById(R.id.eventDetailsText);
        ticketsAvailableDisplay = findViewById(R.id.ticketsAvailableDisplay);
        btnConfirmReservation = findViewById(R.id.btnConfirmReservation);

        ensureSmsPermission();

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
                if (event != null) {
                    event.setId(snap.getId());
                    updateDisplay();
                }
            }
        });

        btnConfirmReservation.setOnClickListener(v -> onConfirm());
    }

    private void ensureSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        }
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

        String uid = getSharedPreferences("SOEN345_PREFS", MODE_PRIVATE)
                .getString("CURRENT_USER_ID", null);

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

            if (!sEvent.exists()) {
                throw new FirebaseFirestoreException(
                        "Event missing",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            Long remL = sEvent.getLong("remainingTickets");
            int remaining = remL == null ? 0 : remL.intValue();

            if (remaining < ticketsRequested) {
                throw new FirebaseFirestoreException(
                        "Not enough tickets",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

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
            sendReservationConfirmation(currentUserId, ticketsRequested);
        }).addOnFailureListener(e -> {
            String m = e.getMessage() != null ? e.getMessage() : "Reservation failed";
            Toast.makeText(this, m, Toast.LENGTH_LONG).show();
        });
    }

    private void sendReservationConfirmation(String currentUserId, int ticketsRequested) {
        UserContactHelper.getCurrentUser(db, currentUserId, new UserContactHelper.ContactCallback() {
            @Override
            public void onLoaded(User user) {
                String phone = user != null ? user.getPhoneNumber() : null;
                String message = "Your reservation is confirmed for "
                        + event.getTitle()
                        + ". Tickets reserved: "
                        + ticketsRequested
                        + ".";

                boolean sent = NotificationService.sendSms(
                        ReservationActivity.this,
                        phone,
                        message
                );

                if (!sent) {
                    Toast.makeText(
                            ReservationActivity.this,
                            "Confirmation may be delayed",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                showReservationSuccessDialog(ticketsRequested);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(
                        ReservationActivity.this,
                        "Confirmation may be delayed",
                        Toast.LENGTH_SHORT
                ).show();
                showReservationSuccessDialog(ticketsRequested);
            }
        });
    }

    private void showReservationSuccessDialog(int ticketsRequested) {
        new AlertDialog.Builder(this)
                .setTitle("Reservation confirmed")
                .setMessage("You reserved " + ticketsRequested + " ticket(s) for " + event.getTitle())
                .setPositiveButton("OK", (d, w) -> {
                    Intent intent = new Intent(this, EventListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}

