package com.example.a195a_e_senior_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.a195a_e_senior_project.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private CollectionReference appointmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getEmail());
        appointmentsRef = userRef.collection("appointments");

        // Display Welcome Message
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
     * Navigates to advising hub activity.
     */
    public void advisingHub(View view) {
        Intent intent = new Intent(this, AdvisingHubActivity.class);
        startActivity(intent);
    }

    public void viewAppointments(View view) {
        Intent intent = new Intent(this, StudentViewAppointmentsActivity.class);
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
