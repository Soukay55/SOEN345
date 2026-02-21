package com.example.soen345;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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

    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (email.isEmpty() && phone.isEmpty()) {
            Toast.makeText(this, "Enter Email or Phone", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean found = false;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    if ((!email.isEmpty() && email.equals(user.getEmail())) ||
                            (!phone.isEmpty() && phone.equals(user.getPhoneNumber()))) {
                        found = true;
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("IS_ADMIN", user.getIsAdmin());
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
                if (!found) Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}