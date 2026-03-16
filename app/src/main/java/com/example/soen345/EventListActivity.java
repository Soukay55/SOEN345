package com.example.soen345;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private ProgressBar loadingSpinner;
    private EventAdapter adapter;

    // Full list loaded from Firestore (never modified)
    private final List<Event> allEvents = new ArrayList<>();
    // Filtered list shown in the RecyclerView
    private final List<Event> filteredEvents = new ArrayList<>();

    private EditText searchInput, filterDate, filterLocation, filterCategory;

    // Track whether filters are currently active
    private boolean filtersActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.eventsRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        searchInput = findViewById(R.id.searchInput);
        filterDate = findViewById(R.id.filterDate);
        filterLocation = findViewById(R.id.filterLocation);
        filterCategory = findViewById(R.id.filterCategory);

        adapter = new EventAdapter(filteredEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnApplyFilters).setOnClickListener(v -> applyFilters());
        findViewById(R.id.btnClearFilters).setOnClickListener(v -> clearFilters());

        boolean isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);
        View btnAddEvent = findViewById(R.id.btnAddEvent);

        if (!isAdmin) {
            btnAddEvent.setVisibility(View.GONE);
        }

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, AddEventActivity.class);
            startActivity(intent);
        });

        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        db.collection("events").get().addOnCompleteListener(task -> {
            loadingSpinner.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                allEvents.clear();

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Event event = doc.toObject(Event.class);
                    event.setId(doc.getId());
                    allEvents.add(event);
                }

                Toast.makeText(this, "Loaded events: " + allEvents.size(), Toast.LENGTH_LONG).show();

                filteredEvents.clear();
                filteredEvents.addAll(allEvents);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            } else {
                Toast.makeText(this, "Failed to load events: " +
                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                emptyText.setText("Could not load events.");
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void applyFilters() {
        String search = searchInput.getText().toString();
        String date = filterDate.getText().toString();
        String location = filterLocation.getText().toString();
        String category = filterCategory.getText().toString();

        filtersActive = !search.trim().isEmpty() || !date.trim().isEmpty()
                || !location.trim().isEmpty() || !category.trim().isEmpty();

        filteredEvents.clear();
        filteredEvents.addAll(filterEvents(allEvents, search, date, location, category));

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void clearFilters() {
        searchInput.setText("");
        filterDate.setText("");
        filterLocation.setText("");
        filterCategory.setText("");
        filtersActive = false;

        filteredEvents.clear();
        filteredEvents.addAll(allEvents);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredEvents.isEmpty()) {
            emptyText.setText(filtersActive ? "No results found." : "No events available.");
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    static List<Event> filterEvents(
            List<Event> allEvents,
            String search,
            String date,
            String location,
            String category
    ) {
        String s = (search == null) ? "" : search.trim().toLowerCase();
        String d = (date == null) ? "" : date.trim().toLowerCase();
        String l = (location == null) ? "" : location.trim().toLowerCase();
        String c = (category == null) ? "" : category.trim().toLowerCase();

        List<Event> out = new ArrayList<>();

        for (Event event : allEvents) {
            String title = event.getTitle() == null ? "" : event.getTitle().toLowerCase();
            String eventDate = event.getDate() == null ? "" : event.getDate().toLowerCase();
            String eventLocation = event.getLocation() == null ? "" : event.getLocation().toLowerCase();
            String eventCategory = event.getCategory() == null ? "" : event.getCategory().toLowerCase();

            boolean matchesSearch = s.isEmpty() || title.contains(s);
            boolean matchesDate = d.isEmpty() || eventDate.contains(d);
            boolean matchesLocation = l.isEmpty() || eventLocation.contains(l);
            boolean matchesCategory = c.isEmpty() || eventCategory.contains(c);

            if (matchesSearch && matchesDate && matchesLocation && matchesCategory) {
                out.add(event);
            }
        }
        return out;
    }
}