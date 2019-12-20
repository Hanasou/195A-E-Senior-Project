package com.example.a195a_e_senior_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check to see if user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void updateUI(FirebaseUser user) {
        if (user != null) {
            // Redirect user to their Dashboard if they are authenticated.
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        }
        else {
            // Use this page if they are not authenticated.
        }
    }

    /**
     * Method to redirect user to sign up.
     * @param view takes in as argument what is being clicked. I don't know the real reason this is here.
     */
    public void signUp(View view) {
        // Intents are used to redirect users to a new activity.
        // Params: packageContext is the activity we're currently at. Second parameter is the activity user should be redirected to.
        Intent intent = new Intent(this, SignUpActivity.class);
        // Call startActivity with the intent as a parameter to redirect the user.
        startActivity(intent);
    }

    /**
     * Method to redirect user to sign in.
     * @param view
     */
    public void signIn(View view) {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}
