package com.example.soen345;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
        adapter = new ReservationAdapter(reservations);
        recyclerView.setAdapter(adapter);

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
            for (QueryDocumentSnapshot doc : q) {
                Reservation r = doc.toObject(Reservation.class);
                r.setId(doc.getId());
                reservations.add(r);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load wallet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}

