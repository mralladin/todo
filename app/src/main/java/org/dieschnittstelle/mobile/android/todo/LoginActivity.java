package org.dieschnittstelle.mobile.android.todo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.dieschnittstelle.mobile.android.skeleton.R;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputUsername;
    private Button btnRegister, btnLogin;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth initialisieren
        auth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        //inputUsername = findViewById(R.id.input_username);
        btnRegister = findViewById(R.id.btn_register);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        btnRegister.setOnClickListener(v -> handleRegister());
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleRegister() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String username = inputUsername.getText().toString().trim();

        if (!isValidEmail(email)) {
            inputEmail.setError("Ungültige E-Mail-Adresse");
            return;
        }

        if (!isValidPassword(password)) {
            inputPassword.setError("Passwort muss genau 6 Zeichen lang sein");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            inputUsername.setError("Benutzername darf nicht leer sein");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        checkUsernameAvailability(username, isAvailable -> {
            if (isAvailable) {
                // Benutzer registrieren
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Benutzer erfolgreich registriert
                                FirebaseUser user = auth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();

                                // Weiterleitung zur nächsten Activity
                                Intent intent = new Intent(LoginActivity.this, OverviewActivity.class);
                                intent.putExtra("username", username);
                                startActivity(intent);
                                finish();
                            } else {
                                // Registrierung fehlgeschlagen
                                Toast.makeText(LoginActivity.this, "Registrierung fehlgeschlagen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                inputUsername.setError("Dieser Benutzername ist bereits vergeben");
            }
        });
    }

    private void handleLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            inputEmail.setError("Ungültige E-Mail-Adresse");
            return;
        }

        if (!isValidPassword(password)) {
            inputPassword.setError("Passwort muss genau 6 Zeichen lang sein");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Anmeldung erfolgreich
                        Toast.makeText(LoginActivity.this, "Anmeldung erfolgreich!", Toast.LENGTH_SHORT).show();

                        // Weiterleitung zur nächsten Activity
                        Intent intent = new Intent(LoginActivity.this, OverviewActivity.class);
                        FirebaseUser user = auth.getCurrentUser();
                        intent.putExtra("username", user != null ? user.getDisplayName() : "Unbekannt");
                        startActivity(intent);
                        finish();
                    } else {
                        // Anmeldung fehlgeschlagen
                        Toast.makeText(LoginActivity.this, "Anmeldung fehlgeschlagen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUsernameAvailability(String username, UsernameCallback callback) {
        // Simulierte Überprüfung des Benutzernamens (Firebase Firestore könnte hier integriert werden)
        if ("testuser".equalsIgnoreCase(username)) {
            callback.onResult(false);
        } else {
            callback.onResult(true);
        }
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() == 6;
    }

    interface UsernameCallback {
        void onResult(boolean isAvailable);
    }
}
