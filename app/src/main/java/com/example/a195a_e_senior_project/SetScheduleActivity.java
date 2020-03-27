package com.example.a195a_e_senior_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Faculty can configure their open office hours here.
 * Office hours designate when users can sign up for appointments.
 */
public class SetScheduleActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private List<String> checkedBoxes;
    private Button setTime;
    private TextView timeText;
    private Button setSchedule;
    private String timeSelected;
    private FirebaseFirestore db;
    private DocumentReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_schedule);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        checkedBoxes = new ArrayList<String>();
        setTime = (Button) findViewById(R.id.setTime);
        setSchedule = (Button) findViewById(R.id.setSchedule);
        timeText = (TextView) findViewById(R.id.selectedTime);
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getEmail());
    }

    /**
     * Call this when a checkbox is checked. Add all checked boxes into a List.
     * @param view
     */
    public void checkboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.monday:
                if (checked){
                    checkedBoxes.add("Monday");
                }
                else {
                    checkedBoxes.remove("Monday");
                }
                break;
            case R.id.tuesday:
                if (checked){
                    checkedBoxes.add("Tuesday");
                }
                else{
                    checkedBoxes.remove("Tuesday");
                }
                break;
            case R.id.wednesday:
                if (checked){
                    checkedBoxes.add("Wednesday");
                }
                else{
                    checkedBoxes.remove("Wednesday");
                }
                break;
            case R.id.thursday:
                if (checked){
                    checkedBoxes.add("Thursday");
                }
                else{
                    checkedBoxes.remove("Thursday");
                }
                break;
            case R.id.friday:
                if (checked){
                    checkedBoxes.add("Friday");
                }
                else{
                    checkedBoxes.remove("Friday");
                }
                break;
        }

        setTime.setVisibility(View.VISIBLE);
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
        setSchedule.setVisibility(View.VISIBLE);
    }

    public void setSchedule(View view) {
        timeSelected = (String) timeText.getText();
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, String> scheduleData = (HashMap<String, String>) document.getData().get("schedule");
                        for (String day : checkedBoxes) {
                            scheduleData.put(day, timeSelected);
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

        Intent intent = new Intent(this, FacultyDashboardActivity.class);
        startActivity(intent);
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            StringBuilder builder = new StringBuilder();
            builder.append(hour);
            builder.append(":");
            if (minute < 10) {
                builder.append(0);
            }
            builder.append(minute);
            TextView timeText = (TextView) getActivity().findViewById(R.id.selectedTime);
            timeText.setText(builder.toString());
            //timeSelected = builder.toString();
        }
    }
}
