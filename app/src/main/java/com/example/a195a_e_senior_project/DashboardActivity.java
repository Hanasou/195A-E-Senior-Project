package com.example.a195a_e_senior_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Display a welcome message for the user.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        TextView welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        welcomeMessage.setText("Welcome " + user.getDisplayName());
    }

    /**
     * Navigate to the register department activity.
     * @param view
     */
    public void registerDepartment(View view) {
        Intent intent = new Intent(this, DepartmentRegistrationActivity.class);
        startActivity(intent);
    }

    /**
     * Signs the user out
     * @param view
     */
    public void signOut(View view) {
        // This signOut method is part of the FirebaseAuth object.
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
