package org.dieschnittstelle.mobile.android.todo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

    protected static final String LOG_TAG = LoginActivity.class.getName();
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

        //TextWatcher überpfüft ob Eingabefelder nicht leer sind und aktiviert/deaktiviert die Buttons
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if both fields are not empty
                boolean isEmailNotEmpty = !TextUtils.isEmpty(inputEmail.getText().toString().trim());
                boolean isPasswordNotEmpty = !TextUtils.isEmpty(inputPassword.getText().toString().trim());

                // Enable or disable the buttons based on field values
                btnRegister.setEnabled(isEmailNotEmpty && isPasswordNotEmpty);
                btnLogin.setEnabled(isEmailNotEmpty && isPasswordNotEmpty);
            }
        };

        inputEmail.addTextChangedListener(textWatcher);
        inputPassword.addTextChangedListener(textWatcher);


    }

    private void handleRegister() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if(validateEmailAndPassword(email, password)){

            progressBar.setVisibility(View.VISIBLE);

            registerUser(email, password);
        }

    }

    // Registrierung des Benutzers
    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    try {
                        Thread.sleep(4000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Benutzer erfolgreich registriert
                        FirebaseUser user = auth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();

                        // Weiterleitung zur nächsten Activity
                        Intent intent = new Intent(LoginActivity.this, OverviewActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Registrierung fehlgeschlagen
                        Toast.makeText(LoginActivity.this, "Registrierung fehlgeschlagen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Validierung der Eingabe
    private Boolean validateEmailAndPassword(String email, String password) {
        if (!isValidEmail(email) && !isValidPassword(password)) {
            inputPassword.setError(getString(R.string.PasswordInvalid));
            inputEmail.setError(getString(R.string.invalidMail));
            return false;
        }

        if (!isValidEmail(email)) {
            inputEmail.setError(getString(R.string.invalidMail));
            return false;
        }

        if (!isValidPassword(password)) {
            inputPassword.setError(getString(R.string.PasswordInvalid));
            return false;
        }
        return true;
    }

    //handled den Login
    private void handleLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        validateEmailAndPassword(email, password);

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    try {
                        Thread.sleep(4000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Anmeldung erfolgreich!", Toast.LENGTH_SHORT).show();

                        //zur nächsten Activity
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

    //Prüft ob der Benutzername verfügbar ist wurde aber nicht umgesetzt
    private void checkUsernameAvailability(String username, UsernameCallback callback) {
        // Simulierte Überprüfung des Benutzernamens (Firebase Firestore könnte hier integriert werden)
        callback.onResult(!"testuser".equalsIgnoreCase(username));
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        Log.i(LOG_TAG, "PaAsswordCheck" + (password.length() == 6 && password.matches("\\d{6}")));
        return password.length() == 6 && password.matches("\\d{6}");
    }

    interface UsernameCallback {
        void onResult(boolean isAvailable);
    }
}
