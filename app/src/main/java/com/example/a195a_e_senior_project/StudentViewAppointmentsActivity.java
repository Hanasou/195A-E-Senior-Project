package com.example.a195a_e_senior_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
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

/**
 * Students viewing their registered appointments.
 */
public class StudentViewAppointmentsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private LinearLayout schedule;
    private DocumentReference userRef;
    private CollectionReference appointmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view_appointments);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        schedule = findViewById(R.id.scheduleLayout);
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getEmail());
        appointmentsRef = userRef.collection("appointments");

        appointmentsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> removedAppointments = new ArrayList<String>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("DocGet", document.getId() + " => " + document.getData());
                                Map<String, Object> appointmentData = document.getData();
                                Date appointmentDate = ((Timestamp) appointmentData.get("date")).toDate();

                                // If the appointment has not expired yet, add it to the view.
                                if (appointmentDate.after(new Date())) {
                                    TextView appointmentText = new TextView(getApplicationContext());
                                    appointmentText.setText(appointmentData.get("name").toString() + "\n" +
                                            appointmentDate.toString());
                                    schedule.addView(appointmentText);
                                }
                                // Otherwise, put it in the removed list so it can be deleted later.
                                else {
                                    removedAppointments.add(document.getId());
                                }
                            }

                            // Delete the expired appointments that got added in removedAppointments earlier.
                            for (String appointmentId : removedAppointments) {
                                appointmentsRef.document(appointmentId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("DeletionStatus", "DocumentSnapshot successfully deleted!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("DeletionStatus", "Error deleting document", e);
                                            }
                                        });
                            }
                        } else {
                            Log.d("DocGet", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
