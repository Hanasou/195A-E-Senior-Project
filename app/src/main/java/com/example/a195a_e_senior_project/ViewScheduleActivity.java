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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a195a_e_senior_project.dialogs.CancelAppointmentDialog;
import com.example.a195a_e_senior_project.dialogs.DeleteAdvisingBlockDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for Faculty viewing their schedule.
 */
public class ViewScheduleActivity extends BaseActivity implements CancelAppointmentDialog.CancelAppointmentDialogListener,
        DeleteAdvisingBlockDialog.DeleteAdvisingBlockListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ListView blocksView;
    private ListView appointmentsView;
    private DocumentReference userRef;
    private CollectionReference inboxRef;
    private String blockKey;
    private String appointmentKey;
    private HashMap<String, String> scheduleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        blocksView = findViewById(R.id.blocks);
        appointmentsView = findViewById(R.id.appointmentsView);
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getEmail());
        inboxRef = userRef.collection("inbox");

        final List<String> blocksList = new ArrayList<String>();
        final List<String> appointmentsList = new ArrayList<String>();
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        scheduleData = (HashMap<String, String>) document.getData().get("schedule");
                        for (String day : scheduleData.keySet()) {
                            blocksList.add(day + " " + scheduleData.get(day));
                        }
                        inboxRef.get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            List<String> expiredAppointments = new ArrayList<String>();
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d("DocGet", document.getId() + " => " + document.getData());
                                                Date appointmentDate = ((Timestamp) document.get("date")).toDate();

                                                // If the appointment has not expired yet, add it to the view.
                                                if (appointmentDate.after(new Date())) {
                                                    appointmentsList.add(document.get("name").toString() + "\n" +
                                                            document.get("email").toString() + "\n" +
                                                            appointmentDate.toString());
                                                }
                                                // Otherwise, add it to the list of expired appointments.
                                                else {
                                                    expiredAppointments.add(document.getId());
                                                }
                                            }

                                            // Delete all expired appointments
                                            for (String appointmentId : expiredAppointments) {
                                                deleteAppointment(appointmentId);
                                            }
                                        } else {
                                            Log.d("DocGet", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });

                        ArrayAdapter<String> blocksAdapter = new ArrayAdapter<String>(ViewScheduleActivity.this,
                                android.R.layout.simple_selectable_list_item, blocksList);
                        ArrayAdapter<String> appointmentsAdapter = new ArrayAdapter<String>(ViewScheduleActivity.this,
                                android.R.layout.simple_selectable_list_item, appointmentsList);
                        blocksView.setAdapter(blocksAdapter);
                        appointmentsView.setAdapter(appointmentsAdapter);
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

        blocksView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                blockKey = blocksList.get(i);
                showDeleteBlockDialog();
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

    /**
     * Deletes an appointment by docID
     * @param key document id of the appointment you want to delete
     */
    public void deleteAppointment(String key) {
        final String studentEmail = key;
        inboxRef.document(studentEmail)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        final String thisEmail = user.getEmail();
                        Log.d("DeletionStatus", "Faculty copy DocumentSnapshot successfully deleted!");
                        DocumentReference studentRef = db.collection("users").document(studentEmail);
                        CollectionReference appointmentsRef = studentRef.collection("appointments");

                        appointmentsRef.document(thisEmail)
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
                        Log.w("DeletionStatus", "Error deleting faculty's document", e);
                    }
                });

    }

    /**
     * Removes one of the open appointment blocks.
     */
    public void removeBlock(String key) {
        final String deleteBlock = key;
        final String day = deleteBlock.substring(0, deleteBlock.indexOf(' '));
        final String time = deleteBlock.substring(deleteBlock.indexOf(' ') + 1);
        scheduleData.remove(day, time);
        Log.d("SCHEDULE day", day);
        Log.d("SCHEDULE day", time);
        userRef
                .update("schedule", scheduleData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("SUCCESS", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FAILURE", "Error updating document", e);
                    }
                });
    }

    /**
     * Create and show the appointment cancellation dialog.
     */
    public void showCancellationDialog() {
        DialogFragment dialog = new CancelAppointmentDialog();
        dialog.show(getSupportFragmentManager(), "CancelDialogFragment");
    }

    /**
     * Create and show the delete open block dialog.
     */
    public void showDeleteBlockDialog() {
        DialogFragment dialog = new DeleteAdvisingBlockDialog();
        dialog.show(getSupportFragmentManager(), "DeleteBlockDialogFragment");
    }


    @Override
    public void onBlockDeletePositiveClick(DialogFragment dialog) {
        removeBlock(blockKey);
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onBlockDeleteNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onCancelPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        deleteAppointment(appointmentKey);
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onCancelNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }
}
