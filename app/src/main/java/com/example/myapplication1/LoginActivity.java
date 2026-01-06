package com.example.myapplication1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.myapplication1.firebase.FirebaseAuthManager;
import com.example.myapplication1.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final long SIGNUP_TIMEOUT = 15000; // 15 seconds timeout

    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    private TextView btnForgotPassword, btnNewUser;
    private CardView loginCard;
    private ProgressBar progressBar;
    private FirebaseAuthManager authManager;
    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = FirebaseAuthManager.getInstance(this);

        if (authManager.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        initializeViews();
        setupAnimations();
        setupClickListeners();
    }

    private void initializeViews() {
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnNewUser = findViewById(R.id.btnNewUser);
        loginCard = findViewById(R.id.loginCard);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupAnimations() {
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        loginCard.startAnimation(slideUp);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String pass = inputPassword.getText().toString().trim();

            if (!isValidEmail(email)) {
                showError(inputEmail, "Veuillez utiliser votre email @cyu.fr");
                return;
            }

            if (!isValidPassword(pass)) {
                showError(inputPassword, "Le mot de passe doit contenir au moins 6 caractères");
                return;
            }

            performLogin(email, pass);
        });

        btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        btnNewUser.setOnClickListener(v -> showRegistrationDialog());
    }

    private boolean isValidEmail(String email) {
        return email.endsWith("@cyu.fr") && email.length() > 10;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    private void performLogin(String email, String password) {
        btnLogin.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        authManager.signIn(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Login success");
                showSuccessAnimation();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    navigateToDashboard();
                }, 1000);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Login error: " + message);
                btnLogin.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                String errorMsg = message.contains("password") ?
                        "Mot de passe incorrect" :
                        message.contains("user") ?
                                "Compte non trouvé. Créez un compte d'abord." :
                                "Erreur de connexion: " + message;

                showError(inputPassword, errorMsg);
            }
        });
    }

    private void showRegistrationDialog() {
        Log.d(TAG, "Opening registration dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register, null);

        EditText inputName = dialogView.findViewById(R.id.inputRegisterName);
        EditText inputRegEmail = dialogView.findViewById(R.id.inputRegisterEmail);
        EditText inputRegPassword = dialogView.findViewById(R.id.inputRegisterPassword);

        builder.setView(dialogView)
                .setTitle("🌱 Créer un compte")
                .setPositiveButton("Créer", null)
                .setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Log.d(TAG, "Create button clicked");

            String name = inputName.getText().toString().trim();
            String email = inputRegEmail.getText().toString().trim();
            String password = inputRegPassword.getText().toString().trim();

            // Validation
            if (name.isEmpty()) {
                inputName.setError("Nom requis");
                inputName.requestFocus();
                return;
            }

            if (!isValidEmail(email)) {
                inputRegEmail.setError("Email @cyu.fr requis");
                inputRegEmail.requestFocus();
                return;
            }

            if (password.length() < 6) {
                inputRegPassword.setError("Minimum 6 caractères");
                inputRegPassword.requestFocus();
                return;
            }

            Log.d(TAG, "Starting DIRECT Firebase signup (bypassing database)");

            // FORCE close dialog
            dialog.cancel();
            dialog.dismiss();

            // Show progress
            runOnUiThread(() -> {
                btnLogin.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            });

            Toast.makeText(LoginActivity.this,
                    "Création du compte...",
                    Toast.LENGTH_SHORT).show();

            // Setup timeout protection
            timeoutRunnable = () -> {
                Log.e(TAG, "SIGNUP TIMEOUT - Force completing");
                runOnUiThread(() -> {
                    // Check if user was actually created
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        Log.d(TAG, "User exists in Firebase Auth - saving session manually");
                        saveUserSessionManually(email, name);
                        showSuccessAndNavigate();
                    } else {
                        Log.e(TAG, "Timeout and no user - showing error");
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this,
                                "Timeout - Veuillez réessayer",
                                Toast.LENGTH_LONG).show();
                    }
                });
            };
            timeoutHandler.postDelayed(timeoutRunnable, SIGNUP_TIMEOUT);

            // Tryingg Firebase signup with our manager
            authManager.signUp(email, password, name, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    Log.d(TAG, "Signup SUCCESS via callback!");
                    timeoutHandler.removeCallbacks(timeoutRunnable);

                    runOnUiThread(() -> {
                        showSuccessAndNavigate();
                    });
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Signup ERROR: " + message);
                    timeoutHandler.removeCallbacks(timeoutRunnable);

                    // Check if error is just database write failure but auth succeeded
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null && message.contains("database")) {
                        Log.d(TAG, "Auth succeeded but database failed - continuing anyway");
                        saveUserSessionManually(email, name);
                        runOnUiThread(() -> {
                            showSuccessAndNavigate();
                        });
                        return;
                    }

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setVisibility(View.VISIBLE);

                        String errorMsg;
                        if (message.contains("already in use") ||
                                message.contains("email-already-in-use")) {
                            errorMsg = "Ce compte existe déjà!";
                            inputEmail.setText(email);
                            inputPassword.requestFocus();
                        } else if (message.contains("network")) {
                            errorMsg = "Erreur réseau!";
                        } else {
                            errorMsg = "Erreur: " + message;
                        }

                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }


    private void saveUserSessionManually(String email, String name) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            SharedPreferences prefs = getSharedPreferences("GardenApp", MODE_PRIVATE);
            prefs.edit()
                    .putString("user_email", email)
                    .putString("user_uid", firebaseUser.getUid())
                    .putString("user_name", name)
                    .putString("user_role", "student")
                    .putBoolean("is_logged_in", true)
                    .apply();
            Log.d(TAG, "Manual session save completed");
        }
    }

    private void showSuccessAndNavigate() {
        Toast.makeText(LoginActivity.this,
                "Compte créé! Bienvenue!",
                Toast.LENGTH_SHORT).show();

        showSuccessAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Navigating to dashboard");
            navigateToDashboard();
        }, 1000);
    }

    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();

        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        if (shake != null) {
            field.startAnimation(shake);
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccessAnimation() {
        runOnUiThread(() -> {
            btnLogin.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            btnLogin.setText("Connexion réussie");
            btnLogin.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_green_dark)));
            btnLogin.setEnabled(false);
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔑 Mot de passe oublié");
        builder.setMessage("Pour réinitialiser votre mot de passe :\n\n" +
                "• Rendez-vous sur le portail ENT CYU\n" +
                "• Utilisez la fonction 'Mot de passe oublié'\n" +
                "• Ou contactez le support informatique");

        builder.setPositiveButton("Ouvrir ENT", (dialog, which) -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://ent.cyu.fr"));
            startActivity(browserIntent);
        });

        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void navigateToDashboard() {
        Log.d(TAG, "Navigation to dashboard");
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }
}