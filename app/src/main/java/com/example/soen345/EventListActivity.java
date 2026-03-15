package com.example.soen345;

import android.os.Bundle;
import android.text.TextUtils;
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
    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();
    private EditText searchInput, filterDate, filterLocation, filterCategory;
    private boolean filtersActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        db             = FirebaseFirestore.getInstance();
        recyclerView   = findViewById(R.id.eventsRecyclerView);
        emptyText      = findViewById(R.id.emptyText);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        searchInput    = findViewById(R.id.searchInput);
        filterDate     = findViewById(R.id.filterDate);
        filterLocation = findViewById(R.id.filterLocation);
        filterCategory = findViewById(R.id.filterCategory);

        adapter = new EventAdapter(filteredEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnApplyFilters).setOnClickListener(v -> applyFilters());
        findViewById(R.id.btnClearFilters).setOnClickListener(v -> clearFilters());

        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        DataSeeder.seedEventsAndTestUser(db, this::loadEvents);
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
                Toast.makeText(this, "Failed to load events. Check your connection.", Toast.LENGTH_LONG).show();
                emptyText.setText("Could not load events.");
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void applyFilters() {
        String search   = searchInput.getText().toString().trim().toLowerCase();
        String date     = filterDate.getText().toString().trim().toLowerCase();
        String location = filterLocation.getText().toString().trim().toLowerCase();
        String category = filterCategory.getText().toString().trim().toLowerCase();

        filtersActive = !search.isEmpty() || !date.isEmpty() || !location.isEmpty() || !category.isEmpty();

        filteredEvents.clear();

        for (Event event : allEvents) {
            boolean matchesSearch   = TextUtils.isEmpty(search)   || event.getTitle().toLowerCase().contains(search);
            boolean matchesDate     = TextUtils.isEmpty(date)     || event.getDate().toLowerCase().contains(date);
            boolean matchesLocation = TextUtils.isEmpty(location) || event.getLocation().toLowerCase().contains(location);
            boolean matchesCategory = TextUtils.isEmpty(category) || event.getCategory().toLowerCase().contains(category);

            if (matchesSearch && matchesDate && matchesLocation && matchesCategory) {
                filteredEvents.add(event);
            }
        }

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
}
