package com.example.soen345;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();

    private EditText searchInput, filterDate, filterLocation, filterCategory;
    private boolean filtersActive = false;
    private boolean isAdmin = false;

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

        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        adapter = new EventAdapter(filteredEvents, isAdmin, new EventAdapter.OnEventActionListener() {
            @Override
            public void onEditClick(Event event) {
                openEditEventScreen(event);
            }

            @Override
            public void onDeleteClick(Event event) {
                showDeleteConfirmation(event);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnApplyFilters).setOnClickListener(v -> applyFilters());
        findViewById(R.id.btnClearFilters).setOnClickListener(v -> clearFilters());

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

                filteredEvents.clear();
                filteredEvents.addAll(allEvents);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            } else {
                String errorMessage = task.getException() != null
                        ? task.getException().getMessage()
                        : "Unknown error";

                Toast.makeText(this, "Failed to load events: " + errorMessage, Toast.LENGTH_LONG).show();
                emptyText.setText("Could not load events.");
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openEditEventScreen(Event event) {
        Intent intent = new Intent(EventListActivity.this, EditEventActivity.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("title", event.getTitle());
        intent.putExtra("date", event.getDate());
        intent.putExtra("location", event.getLocation());
        intent.putExtra("category", event.getCategory());
        startActivity(intent);
    }

    private void showDeleteConfirmation(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event) {
        if (!canDeleteEvent(isAdmin, event)) {
            Toast.makeText(this, "You are not allowed to delete this event.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(event.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                    loadEvents();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        String search = searchInput.getText().toString();
        String date = filterDate.getText().toString();
        String location = filterLocation.getText().toString();
        String category = filterCategory.getText().toString();

        filtersActive = !search.trim().isEmpty()
                || !date.trim().isEmpty()
                || !location.trim().isEmpty()
                || !category.trim().isEmpty();

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

    static boolean canDeleteEvent(boolean isAdmin, Event event) {
        return isAdmin
                && event != null
                && event.getId() != null
                && !event.getId().trim().isEmpty();
    }

    static List<Event> removeEventFromList(List<Event> events, String eventId) {
        List<Event> updated = new ArrayList<>();

        if (events == null) {
            return updated;
        }

        for (Event event : events) {
            if (event == null) {
                continue;
            }

            String currentId = event.getId();
            if (eventId == null || currentId == null || !currentId.equals(eventId)) {
                updated.add(event);
            }
        }

        return updated;
    }

    static boolean canEditEvent(boolean isAdmin, Event event) {
        return isAdmin
                && event != null
                && event.getId() != null
                && !event.getId().trim().isEmpty();
    }

    static List<Event> updateEventInList(List<Event> events, Event updatedEvent) {
        List<Event> updated = new ArrayList<>();

        if (events == null) {
            return updated;
        }

        for (Event event : events) {
            if (event == null) {
                continue;
            }

            if (updatedEvent != null
                    && updatedEvent.getId() != null
                    && updatedEvent.getId().equals(event.getId())) {
                updated.add(updatedEvent);
            } else {
                updated.add(event);
            }
        }

        return updated;
    }
}