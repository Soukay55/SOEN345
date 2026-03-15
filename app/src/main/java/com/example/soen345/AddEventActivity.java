package com.example.soen345;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AddEventActivity extends AppCompatActivity {

    private EditText inputTitle, inputDate, inputLocation, inputCategory;
    private Button btnSaveEvent;
    private FirebaseFirestore db;

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

        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        String title = inputTitle.getText().toString().trim();
        String date = inputDate.getText().toString().trim();
        String location = inputLocation.getText().toString().trim();
        String category = inputCategory.getText().toString().trim();

        if (!isValidEventInput(title, date, location, category)) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = db.collection("events").document().getId();
        Event event = new Event(eventId, title, date, location, category);

        db.collection("events").document(eventId)
                .set(event)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event created successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create event.", Toast.LENGTH_SHORT).show();
                });
    }

    static boolean isValidEventInput(String title, String date, String location, String category) {
        return title != null && !title.trim().isEmpty()
                && date != null && !date.trim().isEmpty()
                && location != null && !location.trim().isEmpty()
                && category != null && !category.trim().isEmpty();
    }
}