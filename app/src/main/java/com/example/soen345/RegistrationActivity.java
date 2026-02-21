package com.example.soen345;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class RegistrationActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText emailInput, phoneInput;
    private CheckBox adminBox;
    private Button btnRegister, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        adminBox = findViewById(R.id.adminBox);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        btnRegister.setOnClickListener(v -> performRegistration());

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void performRegistration() {
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        boolean isAdmin = adminBox.isChecked();

        if (email.isEmpty() && phone.isEmpty()) {
            Toast.makeText(this, "Please enter an email or phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        if (!phone.isEmpty() && phone.length() < 10) {
            phoneInput.setError("Please enter a valid phone number");
            phoneInput.requestFocus();
            return;
        }

        // Check if email already exists
        if (!email.isEmpty()) {
            db.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        emailInput.setError("Email already exists");
                        emailInput.requestFocus();
                    } else {
                        checkPhoneAndRegister(phone, email, isAdmin);
                    }
                });
        } else {
            checkPhoneAndRegister(phone, email, isAdmin);
        }
    }

    private void checkPhoneAndRegister(String phone, String email, boolean isAdmin) {
        if (!phone.isEmpty()) {
            db.collection("users").whereEqualTo("phoneNumber", phone).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        phoneInput.setError("Phone number already exists");
                        phoneInput.requestFocus();
                    } else {
                        registerNewUser(email, phone, isAdmin);
                    }
                });
        } else {
            registerNewUser(email, phone, isAdmin);
        }
    }

    private void registerNewUser(String email, String phone, boolean isAdmin) {
        String userId = UUID.randomUUID().toString();
        User newUser = new User(userId, email, phone, isAdmin);

        db.collection("users").document(userId).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    // SUCCESS: Database confirmed the data is saved
                    Toast.makeText(this, "Account Created! Redirecting...", Toast.LENGTH_SHORT).show();

                    // AUTOMATIC NAVIGATION TO LOGIN
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}