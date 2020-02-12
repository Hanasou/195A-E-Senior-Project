package com.example.a195a_e_senior_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * User can register for appointments with an advisor here.
 */
public class RegisterAppointmentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private Spinner advisingOptions;
    private Spinner advisors;
    private Spinner advisingBlock;
    private Button setDate;
    private Button makeAppointment;
    private TextView dateText;
    private String advisingOptionSelected;
    private String advisorSelected;
    private String blockSelected;
    private String dateSelected;
    private Map<String, String> advisorSchedule;
    private DocumentReference userRef;
    private DocumentReference advisorRef;
    private CollectionReference usersRef;
    private CollectionReference inboxRef;
    private CollectionReference userAppointmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_appointment);

        // Instantiate global variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        advisingOptions = (Spinner) findViewById(R.id.advisingOptions);
        advisors = (Spinner) findViewById(R.id.advisors);
        advisingBlock = (Spinner) findViewById(R.id.advisingBlock);
        setDate = (Button) findViewById(R.id.setDate);
        makeAppointment = (Button) findViewById(R.id.makeAppointment);
        dateText = (TextView) findViewById(R.id.selectedDate);
        userRef = db.collection("users").document(user.getEmail());
        usersRef = db.collection("users");

        advisingOptions.setPrompt("Select Category");
        advisors.setPrompt("Select Advisor");

        List<String> categories = new ArrayList<String>();
        categories.add("Athletics");
        categories.add("GE Requirements");
        categories.add("Department");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Load the adapter into a spinner (dropdown menu)
        advisingOptions.setAdapter(adapter);

        advisingOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                advisingOptionSelected = parent.getItemAtPosition(pos).toString();
                if (advisingOptionSelected.equals("Department")) {

                }
                else {
                    usersRef.whereEqualTo("advisor", advisingOptionSelected)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                public void onSuccess(QuerySnapshot documentSnapshots) {
                                    if (documentSnapshots.isEmpty()) {
                                        Log.d("Result ", "onSuccess: No Snapshots");
                                        advisorSelected = null;
                                        blockSelected = null;
                                        dateSelected = null;
                                        advisors.setVisibility(INVISIBLE);
                                        advisingBlock.setVisibility(INVISIBLE);
                                        setDate.setVisibility(INVISIBLE);
                                        return;
                                    }
                                    else {
                                        Log.d("Result ", "onSuccess: List contains something");
                                        List<String> advisorList = new ArrayList<String>();
                                        for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                                            String fullName = document.getData().get("first").toString() + " " +
                                                    document.getData().get("last").toString();
                                            advisorList.add(fullName);
                                        }
                                        // Use array adapter to adapt items into the format we want them in.
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegisterAppointmentActivity.this, android.R.layout.simple_spinner_item,
                                                advisorList);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                        // Load the adapter into a spinner (dropdown menu)
                                        advisors.setAdapter(adapter);
                                        advisors.setVisibility(VISIBLE);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Could not retrieve data",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
            public void onNothingSelected(AdapterView<?> parent) {
                //Method is required as defined by interface
            }
        });

        advisors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                advisorSelected = parent.getItemAtPosition(pos).toString();
                Log.d("Last", advisorSelected.substring(advisorSelected.indexOf(' ') + 1));
                usersRef.whereEqualTo("first", advisorSelected.substring(0, advisorSelected.indexOf(' ')))
                        .whereEqualTo("last", advisorSelected.substring(advisorSelected.indexOf(' ') + 1))
                        .whereEqualTo("advisor", advisingOptionSelected)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            public void onSuccess(QuerySnapshot documentSnapshots) {
                                if (documentSnapshots.isEmpty()) {
                                    Log.d("Result ", "onSuccess: No advisors to meet criteria");
                                    return;
                                }
                                else {
                                    String advisorId = documentSnapshots.getDocuments().get(0).getId();
                                    advisorRef = usersRef.document(advisorId);
                                    advisorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    advisorSchedule = (HashMap<String, String>) document.getData().get("schedule");
                                                    List<String> timeBlocks = new ArrayList<String>();
                                                    for (String block : advisorSchedule.keySet()) {
                                                        String timeBlock = block + " " + advisorSchedule.get(block);
                                                        timeBlocks.add(timeBlock);
                                                    }
                                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegisterAppointmentActivity.this, android.R.layout.simple_spinner_item,
                                                            timeBlocks);
                                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    // Load the adapter into a spinner (dropdown menu)
                                                    advisingBlock.setAdapter(adapter);
                                                    advisingBlock.setVisibility(VISIBLE);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Doesn't exist.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Could not retrieve data",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Could not retrieve data",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

            }
            public void onNothingSelected(AdapterView<?> parent) {
                //Method is required as defined by interface
            }
        });

        advisingBlock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                blockSelected = parent.getItemAtPosition(pos).toString();
                setDate.setVisibility(VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void makeAppointment(View view) throws ParseException {
        dateSelected = (String) dateText.getText() + blockSelected.substring(blockSelected.indexOf(' '));
        // Use LocalDate object to check if DayOfWeek matches advisor's office hours
        // Use Date object to add into Database

        // Use LocalDate object to see if the date that the user chose is valid.
        LocalDate checkDate = LocalDate.parse(dateText.getText().toString(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String openDay = (String) advisingBlock.getSelectedItem();
        openDay = openDay.substring(0, openDay.indexOf(' ')).toUpperCase();
        Log.d("Day of Appointment", openDay);
        Log.d("Day Selected", checkDate.getDayOfWeek().toString());
        if (!checkDate.getDayOfWeek().toString().equals(openDay)){
            Toast.makeText(getApplicationContext(), "Choose Correct Day Of Week",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkDate.isBefore(LocalDate.now())) {
            Toast.makeText(getApplicationContext(), "Choose Date After Now",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("Time String", dateSelected);
        Date date = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(dateSelected);
        Log.d("Date", date.toString());

        // Add the appointment information into the database. One for the advisor, one for the user.
        inboxRef = advisorRef.collection("inbox");
        userAppointmentsRef = userRef.collection("appointments");
        Map<String, Object> appointmentRequest = new HashMap<String, Object>();
        appointmentRequest.put("name", user.getDisplayName());
        appointmentRequest.put("date", new Timestamp(date));

        Map<String, Object> appointmentUser = new HashMap<String, Object>();
        appointmentUser.put("name", advisorSelected);
        appointmentUser.put("date", new Timestamp(date));

        inboxRef.document(user.getDisplayName())
                .set(appointmentRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DocWrite", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DocWrite", "Error writing document", e);
                    }
                });

        userAppointmentsRef.document(advisorSelected)
                .set(appointmentUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DocWrite", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DocWrite", "Error writing document", e);
                    }
                });

        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
        makeAppointment.setVisibility(VISIBLE);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            StringBuilder builder = new StringBuilder();
            int actualMonth = month + 1;
            if (actualMonth < 10) {
                builder.append(0);
            }
            builder.append(actualMonth + "/");
            if (day < 10) {
                builder.append(0);
            }
            builder.append(day + "/");
            builder.append(year);
            TextView dateText = (TextView) getActivity().findViewById(R.id.selectedDate);
            dateText.setText(builder.toString());
            dateText.setVisibility(VISIBLE);
        }
    }
}
