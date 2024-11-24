package org.dieschnittstelle.mobile.android.todo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText inputPhoneNumber, inputUsername, inputVerificationCode;
    private Button btnSendCode, btnVerifyCode;
    private ProgressBar progressBar;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // UI-Elemente initialisieren
        inputPhoneNumber = findViewById(R.id.input_phone_number);
        inputUsername = findViewById(R.id.input_username);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnVerifyCode = findViewById(R.id.btn_verify_code);
        progressBar = findViewById(R.id.progress_bar);
        inputVerificationCode = findViewById(R.id.input_verification_code);

        btnSendCode.setOnClickListener(v -> {
            String phoneNumber = inputPhoneNumber.getText().toString().trim();
            if (TextUtils.isEmpty(phoneNumber)) {
                inputPhoneNumber.setError("Handynummer wird benötigt");
                return;
            }
            sendVerificationCode(phoneNumber);
        });

        btnVerifyCode.setOnClickListener(v -> {
            Log.i("authcLog","I reach this???");

            String code = inputVerificationCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                inputVerificationCode.setError("Code wird benötigt");
                return;
            }
            Log.i("authcLog","I reach this???");

            verifyCode(code);
        });


    }

    private void sendVerificationCode(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber) // Handynummer
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout
                        .setActivity(this) // Aktuelle Activity
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                // Automatische Verifizierung (z. B. bei SMS im gleichen Gerät)
                                Log.i("authcLog","I reach this");
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, "Verifizierung fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                // Code wurde gesendet
                                progressBar.setVisibility(View.GONE);
                                LoginActivity.this.verificationId = verificationId;
                                LoginActivity.this.resendToken = token;
                                // Sichtbarkeit ändern
                                inputVerificationCode.setVisibility(View.VISIBLE); // Eingabefeld für den Code sichtbar machen
                                btnVerifyCode.setVisibility(View.VISIBLE); // Button sichtbar machen
                                inputUsername.setVisibility(View.VISIBLE);

                                Toast.makeText(LoginActivity.this, "Code wurde gesendet", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        Log.i("authcLog","CODE iS:"+code);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Willkommen, " + user.getPhoneNumber(), Toast.LENGTH_SHORT).show();
                        saveUserToDatabase(user); // Benutzer speichern
                        navigateToOverview();
                    } else {
                        Toast.makeText(LoginActivity.this, "Anmeldung fehlgeschlagen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user) {
        // Beispiel: Benutzername und Telefonnummer in Firestore speichern
        String username = inputUsername.getText().toString().trim();
        String phoneNumber = user.getPhoneNumber();

        if (TextUtils.isEmpty(username)) {
            username = "Benutzer";
        }

        // Daten in Firestore speichern
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("phone", phoneNumber);

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("LoginActivity", "Benutzer gespeichert"))
                .addOnFailureListener(e -> Log.e("LoginActivity", "Fehler beim Speichern", e));
    }

    private void navigateToOverview() {
        Intent intent = new Intent(LoginActivity.this, OverviewActivity.class);
        startActivity(intent);
        finish();
    }
}
