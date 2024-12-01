package org.dieschnittstelle.mobile.android.todo.security;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public AuthManager() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    // Registrierung eines Benutzers mit E-Mail, Passwort und Benutzernamen
    public void registerUser(String email, String password, String username, AuthCallback callback) {
        checkUsernameAvailability(username, isAvailable -> {
            if (isAvailable) {
                // Benutzername verfügbar, registriere Benutzer
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            Log.d("LoginLog", "User registered successfully!");

                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    // Speichere Benutzername in der Firestore-Datenbank
                                    saveUsernameToFirestore(user.getUid(), username, dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Log.d("LoginLog", "User registered successfully!");

                                            callback.onSuccess(user);
                                        } else {
                                            callback.onFailure(dbTask.getException());
                                        }
                                    });
                                }
                            } else {
                                callback.onFailure(task.getException());
                            }
                        });
            } else {
                // Benutzername nicht verfügbar
                callback.onFailure(new Exception("Benutzername ist bereits vergeben"));
            }
        });
    }

    // Anmeldung eines Benutzers
    public void loginUser(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(auth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    // Überprüfung, ob der Benutzername verfügbar ist
    public void checkUsernameAvailability(String username, UsernameCallback callback) {
        firestore.collection("usernames")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        callback.onResult(snapshot == null || snapshot.isEmpty());
                    } else {
                        callback.onResult(false);
                    }
                });
    }

    // Speichere den Benutzernamen in Firestore
    private void saveUsernameToFirestore(String userId, String username, FirestoreCallback callback) {
        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("username", username);
        usernameData.put("userId", userId);

        firestore.collection("usernames")
                .document(userId)
                .set(usernameData)
                .addOnCompleteListener(callback::onComplete);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);

        void onFailure(Exception e);
    }

    public interface UsernameCallback {
        void onResult(boolean isAvailable);
    }

    public interface FirestoreCallback {
        void onComplete(com.google.android.gms.tasks.Task<Void> task);
    }
}
