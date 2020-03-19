package com.example.a195a_e_senior_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.a195a_e_senior_project.dialogs.CancelAppointmentDialog;
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
public class StudentViewAppointmentsActivity extends AppCompatActivity implements CancelAppointmentDialog.CancelAppointmentDialogListener{

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ListView appointmentsView;
    private DocumentReference userRef;
    private CollectionReference appointmentsRef;
    private String appointmentKey;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.bottom_nav_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent homeIntent = new Intent(this, FacultyDashboardActivity.class);
                startActivity(homeIntent);
                return true;

            case R.id.navigation_advising:
                return true;

            case R.id.navigation_forum:
                return true;

            case R.id.navigation_notifications:
                Intent notificationIntent = new Intent(this, NotificationsActivity.class);
                startActivity(notificationIntent);
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
        setContentView(R.layout.activity_student_view_appointments);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        appointmentsView = findViewById(R.id.appointmentsView);
        userRef = db.collection("users").document(user.getEmail());
        appointmentsRef = userRef.collection("appointments");

        final List<String> appointmentsList = new ArrayList<String>();
        // refer to ViewScheduleActivity to see how to do it.
        appointmentsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> removedAppointments = new ArrayList<String>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("DocGet", document.getId() + " => " + document.getData());
                                Date appointmentDate = ((Timestamp) document.get("date")).toDate();

                                // If the appointment has not expired yet, add it to the view.
                                if (appointmentDate.after(new Date())) {
                                    appointmentsList.add(document.get("name").toString() + "\n" +
                                            document.get("email") + "\n" +
                                            appointmentDate.toString());
                                }
                                // Otherwise, put it in the removed list so it can be deleted later.
                                else {
                                    removedAppointments.add(document.getId());
                                }
                            }

                            // Delete the expired appointments that got added in removedAppointments earlier.
                            for (String appointmentId : removedAppointments) {
                                deleteAppointment(appointmentId);
                            }
                            ArrayAdapter<String> appointmentsAdapter = new ArrayAdapter<String>(StudentViewAppointmentsActivity.this,
                                    android.R.layout.simple_selectable_list_item, appointmentsList);
                            appointmentsView.setAdapter(appointmentsAdapter);
                        } else {
                            Log.d("DocGet", "Error getting documents: ", task.getException());
                        }
                    }
                });

        appointmentsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // appointmentKey is the advisor's email
                int nIndex = appointmentsList.get(i).indexOf('\n');
                int nIndex2 = nIndex + appointmentsList.get(i).substring(nIndex + 1).indexOf('\n');
                appointmentKey = appointmentsList.get(i).substring(nIndex + 1,nIndex2 + 1);
                Log.d("Email", appointmentKey);
                showCancellationDialog();
            }
        });
    }

    public void deleteAppointment(String key) {
        final String facultyEmail = key;
        appointmentsRef.document(facultyEmail)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        final String thisEmail = user.getEmail();
                        Log.d("DeletionStatus", "Student copy DocumentSnapshot successfully deleted!");
                        DocumentReference facultyRef = db.collection("users").document(facultyEmail);
                        CollectionReference inboxRef = facultyRef.collection("inbox");

                        inboxRef.document(thisEmail)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DeletionStatus", "Student copy DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("DeletionStatus", "Error deleting student's document", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DeletionStatus", "Error deleting document", e);
                    }
                });
    }

    public void showCancellationDialog() {
        DialogFragment dialog = new CancelAppointmentDialog();
        dialog.show(getSupportFragmentManager(), "CancelDialogFragment");
    }

    @Override
    public void onCancelPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        deleteAppointment(appointmentKey);
    }

    @Override
    public void onCancelNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }
}
