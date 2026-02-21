package com.example.soen345;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText emailInput, phoneInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        emailInput = findViewById(R.id.loginEmail);
        phoneInput = findViewById(R.id.loginPhone);

        findViewById(R.id.btnLoginSubmit).setOnClickListener(v -> performLogin());
        findViewById(R.id.btnGoToRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
        });
    }

    @VisibleForTesting
    public void setFirestore(FirebaseFirestore firestore) {
        this.db = firestore;
    }

    public static boolean isInputValid(String email, String phone) {
        return !email.trim().isEmpty() || !phone.trim().isEmpty();
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (!isInputValid(email, phone)) {
            Toast.makeText(this, "Enter Email or Phone", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean found = false;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    if (matchUser(user, email, phone)) {
                        found = true;
                        navigateToMain(user.getIsAdmin());
                        break;
                    }
                }
                if (!found) Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean matchUser(User user, String email, String phone) {
        if (user == null) return false;
        boolean emailMatch = !email.isEmpty() && email.equals(user.getEmail());
        boolean phoneMatch = !phone.isEmpty() && phone.equals(user.getPhoneNumber());
        return emailMatch || phoneMatch;
    }

    private void navigateToMain(boolean isAdmin) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("IS_ADMIN", isAdmin);
        startActivity(intent);
        finish();
    }
}
