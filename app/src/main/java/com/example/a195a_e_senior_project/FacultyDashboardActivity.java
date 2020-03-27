package com.example.a195a_e_senior_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Dashboard for faculty members.
 */
public class FacultyDashboardActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private CollectionReference inboxRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_dashboard);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getEmail());
        inboxRef = userRef.collection("inbox");

        // Display Welcome Message
        TextView welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        welcomeMessage.setText("Welcome " + user.getDisplayName());

        // TODO: Delete all expired appointments when user starts up their dashboard
    }

    public void currentSchedule(View view) {
        Intent intent = new Intent(this, ViewScheduleActivity.class);
        startActivity(intent);
    }
    /**
     * Configures weekly open office hours for advisors
     * @param view
     */
    public void setSchedule(View view) {
        Intent intent = new Intent(this, SetScheduleActivity.class);
        startActivity(intent);
    }

    public void advisorRegistration(View view) {
        Intent intent = new Intent(this, AdvisorRegistrationActivity.class);
        startActivity(intent);
    }

    public void viewNotifications(View view) {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    public void viewForum(View view) {
        Intent intent = new Intent(this, ForumActivity.class);
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
