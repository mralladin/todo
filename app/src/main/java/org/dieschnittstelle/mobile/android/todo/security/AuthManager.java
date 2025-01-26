package org.dieschnittstelle.mobile.android.todo.security;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//Liefert Authentifizierter User zurück und erlaubt Logout
public class AuthManager {

    private FirebaseAuth auth;

    public AuthManager() {
        this.auth = FirebaseAuth.getInstance();
    }



    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }


}
