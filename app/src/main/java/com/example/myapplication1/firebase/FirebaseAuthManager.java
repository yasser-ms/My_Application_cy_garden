package com.example.myapplication1.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.myapplication1.models.User;

public class FirebaseAuthManager {

    private static FirebaseAuthManager instance;
    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;
    private final Context context;

    private FirebaseAuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.auth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public static synchronized FirebaseAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAuthManager(context);
        }
        return instance;
    }

    public void signIn(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            loadUserData(firebaseUser.getUid(), callback);
                        }
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Login failed");
                    }
                });
    }

    public void signUp(String email, String password, String name, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String studentId = extractStudentId(email);
                            User user = new User(firebaseUser.getUid(), email, name, "student", studentId);
                            createUserInDatabase(user, callback);
                        } else {
                            callback.onError("Failed to get user information");
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Signup failed";
                        callback.onError(errorMessage);
                    }
                });
    }

    private void createUserInDatabase(User user, AuthCallback callback) {
        usersRef.child(user.getUid()).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserSession(user);
                        callback.onSuccess(user);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to create user profile";
                        callback.onError(errorMessage);
                    }
                });
    }

    private void loadUserData(String uid, AuthCallback callback) {
        usersRef.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    saveUserSession(user);
                    callback.onSuccess(user);
                } else {
                    callback.onError("User data not found");
                }
            } else {
                callback.onError("Failed to load user data");
            }
        });
    }

    private void saveUserSession(User user) {
        SharedPreferences prefs = context.getSharedPreferences("GardenApp", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("user_email", user.getEmail())
                .putString("user_uid", user.getUid())
                .putString("user_name", user.getName())
                .putString("user_role", user.getRole())
                .putBoolean("is_logged_in", true)
                .apply();
    }

    public void signOut() {
        auth.signOut();
        SharedPreferences prefs = context.getSharedPreferences("GardenApp", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    private String extractStudentId(String email) {
        // Extract student ID from email (e.g., firstname.lastname@cyu.fr -> ID based on name)
        String prefix = email.split("@")[0];
        return prefix.toUpperCase().replace(".", "");
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }
}