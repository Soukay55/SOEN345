package com.example.soen345;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditEventActivity extends AppCompatActivity {

    private EditText inputTitle, inputDate, inputLocation, inputCategory;
    private Button btnSaveEvent;
    private FirebaseFirestore db;

    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        db = FirebaseFirestore.getInstance();

        inputTitle = findViewById(R.id.inputTitle);
        inputDate = findViewById(R.id.inputDate);
        inputLocation = findViewById(R.id.inputLocation);
        inputCategory = findViewById(R.id.inputCategory);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);

        btnSaveEvent.setText("Update Event");

        eventId = getIntent().getStringExtra("eventId");

        inputTitle.setText(getIntent().getStringExtra("title"));
        inputDate.setText(getIntent().getStringExtra("date"));
        inputLocation.setText(getIntent().getStringExtra("location"));
        inputCategory.setText(getIntent().getStringExtra("category"));

        btnSaveEvent.setOnClickListener(v -> updateEvent());
    }

    private void updateEvent() {
        String title = inputTitle.getText().toString().trim();
        String date = inputDate.getText().toString().trim();
        String location = inputLocation.getText().toString().trim();
        String category = inputCategory.getText().toString().trim();

        if (!isValidEventInput(title, date, location, category)) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid event ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        Event updatedEvent = new Event(eventId, title, date, location, category);

        db.collection("events").document(eventId)
                .set(updatedEvent)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event updated successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update event.", Toast.LENGTH_SHORT).show();
                });
    }

    static boolean isValidEventInput(String title, String date, String location, String category) {
        return title != null && !title.trim().isEmpty()
                && date != null && !date.trim().isEmpty()
                && location != null && !location.trim().isEmpty()
                && category != null && !category.trim().isEmpty();
    }
}