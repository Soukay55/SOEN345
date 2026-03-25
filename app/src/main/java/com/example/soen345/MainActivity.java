package com.example.soen345;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // run seeding and deterministic capacity migration off the main thread
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            try {
                DataSeeder.seedEventsIfEmpty(db);
            } catch (Exception ignored) {
                // ignore errors during background seeding
            }
        });
        ex.shutdown();

        boolean isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText(isAdmin ? "Welcome, Admin!" : "Welcome, Customer!");

        Button btnViewEvents = findViewById(R.id.btnViewEvents);
        btnViewEvents.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventListActivity.class);
            intent.putExtra("IS_ADMIN", isAdmin);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
