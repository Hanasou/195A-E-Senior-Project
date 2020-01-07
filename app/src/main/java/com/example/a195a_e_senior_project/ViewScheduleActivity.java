package com.example.a195a_e_senior_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.resources.TextAppearance;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewScheduleActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private LinearLayout schedule;
    private DocumentReference userRef;
    private CollectionReference inboxRef;
    private List<String> removedBlocks;
    private List<String> removedAppointments;
    private String appointmentKey;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.schedule_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                return true;

            case R.id.navigation_dashboard:
                return true;

            case R.id.navigation_notifications:
                return true;

            case R.id.delete_block:
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, String> scheduleData = (HashMap<String, String>) document.getData().get("schedule");
                                for (String day : removedBlocks) {
                                    scheduleData.remove(day);
                                }
                                userRef.update(
                                        "schedule", scheduleData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Schedule Updated",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Schedule Update Failed",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.w("UpdateError", "Error updating document", e);
                                            }
                                        });
                            } else {
                                Toast.makeText(getApplicationContext(), "Document Doesn't Exist. Somehow.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Task Unsuccessful",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                for (String name : removedAppointments) {
                    inboxRef.document(name)
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
                Intent intent = new Intent(this, FacultyDashboardActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        schedule = findViewById(R.id.scheduleLayout);
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getEmail());
        inboxRef = userRef.collection("inbox");
        removedBlocks = new ArrayList<String>();
        removedAppointments = new ArrayList<String>();

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, String> scheduleData = (HashMap<String, String>) document.getData().get("schedule");
                        for (String day : scheduleData.keySet()) {
                            CheckBox scheduleBlock = new CheckBox(getApplicationContext());
                            scheduleBlock.setText(day + " " + scheduleData.get(day));
                            schedule.addView(scheduleBlock);
                            scheduleBlock.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    boolean checked = ((CheckBox) v).isChecked();
                                    String boxText = ((CheckBox) v).getText().toString();
                                    String firstWord = boxText.substring(0, boxText.indexOf(' '));
                                    if (checked) {
                                        removedBlocks.add(firstWord);
                                    }
                                    else {
                                        removedBlocks.remove(firstWord);
                                    }
                                }
                            });
                        }
                        TextView appointmentText = new TextView(getApplicationContext());
                        appointmentText.setText("Appointments");
                        appointmentText.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
                        schedule.addView(appointmentText);

                        inboxRef.get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d("DocGet", document.getId() + " => " + document.getData());
                                                Map<String, Object> appointmentData = document.getData();
                                                CheckBox appointmentText = new CheckBox(getApplicationContext());
                                                appointmentText.setText(appointmentData.get("name").toString() + "\n" +
                                                        appointmentData.get("date").toString());
                                                schedule.addView(appointmentText);
                                                appointmentText.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        boolean checked = ((CheckBox) v).isChecked();
                                                        String boxText = ((CheckBox) v).getText().toString();
                                                        appointmentKey = boxText.substring(0, boxText.indexOf('\n'));
                                                        if (checked) {
                                                            removedAppointments.add(appointmentKey);
                                                        }
                                                        else {
                                                            removedAppointments.remove(appointmentKey);
                                                        }
                                                    }
                                                });
                                            }
                                        } else {
                                            Log.d("DocGet", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Document Doesn't Exist. Somehow.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Task Unsuccessful",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
