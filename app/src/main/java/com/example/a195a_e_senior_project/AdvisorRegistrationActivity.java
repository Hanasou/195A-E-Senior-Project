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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Faculty members can register to be an advisor here.
 */
public class AdvisorRegistrationActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private Spinner advisorOptions;
    private Spinner colleges;
    private Spinner departments;
    private Button submitButton;
    private String advisorCategory;
    private String collegeSelected;
    private String departmentSelected;
    private CollectionReference collegesRef;
    private CollectionReference departmentsRef;
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advisor_registration);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        advisorCategory = "";
        collegeSelected = "";
        departmentSelected = "";
        advisorOptions = (Spinner) findViewById(R.id.advisorOptions);
        colleges = (Spinner) findViewById(R.id.colleges);
        departments = (Spinner) findViewById(R.id.departments);
        submitButton = (Button) findViewById(R.id.submit);

        // TODO: Currently hardcoded. Should retrieve category info from database.
        List<String> categories = new ArrayList<String>();
        categories.add("Athletics");
        categories.add("GE Requirements");
        categories.add("Department");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Load the adapter into a spinner (dropdown menu)
        advisorOptions.setAdapter(adapter);

        advisorOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                advisorCategory = parent.getItemAtPosition(pos).toString();
                if (advisorCategory.equals("Department")) {
                    collegesRef = db.collection("universities")
                            .document("SJSU").collection("colleges");
                    collegesRef.get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                public void onSuccess(QuerySnapshot documentSnapshots) {
                                    if (documentSnapshots.isEmpty()) {
                                        Log.d("Result ", "onSuccess: LIST EMPTY");
                                        return;
                                    }
                                    else {
                                        List<String> collegeList = new ArrayList<String>();
                                        for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                                            collegeList.add(document.getData().get("name").toString());
                                        }
                                        // Use array adapter to adapt items into the format we want them in.
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AdvisorRegistrationActivity.this, android.R.layout.simple_spinner_item,
                                                collegeList);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                        // Load the adapter into a spinner (dropdown menu)
                                        colleges.setAdapter(adapter);
                                        colleges.setVisibility(VISIBLE);
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
                else {
                    collegeSelected = "";
                    departmentSelected = "";
                    submitButton.setVisibility(VISIBLE);
                    colleges.setVisibility(INVISIBLE);
                    departments.setVisibility(INVISIBLE);
                }

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        colleges.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                collegeSelected = parent.getItemAtPosition(pos).toString();
                collegesRef.whereEqualTo("name", collegeSelected)
                        .get()
                        .addOnFailureListener(new OnFailureListener() {
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Could not retrieve data",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            public void onSuccess(QuerySnapshot documentSnapshots) {
                                // Only one document should be retrieved here. This represents the College.
                                DocumentSnapshot document = documentSnapshots.getDocuments().get(0);
                                String docId = document.getId();
                                departmentsRef = collegesRef.document(docId).collection("departments");

                                //Get all departments.
                                departmentsRef.get()
                                        .addOnFailureListener(new OnFailureListener() {
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Could not retrieve data",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            public void onSuccess(QuerySnapshot documentSnapshots) {
                                                if (documentSnapshots.isEmpty()) {
                                                    Log.d("Result ", "onSuccess: LIST EMPTY");
                                                    return;
                                                }
                                                else {
                                                    // Make a list of all the departments.
                                                    List<String> departmentsList = new ArrayList<String>();
                                                    for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                                                        departmentsList.add(document.getData().get("name").toString());
                                                    }
                                                    // Use array adapter to adapt items into the format we want them in.
                                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(AdvisorRegistrationActivity.this,
                                                            android.R.layout.simple_spinner_item, departmentsList);
                                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                                    // Load the adapter into a spinner (dropdown menu)
                                                    departments.setAdapter(adapter);
                                                    departments.setVisibility(VISIBLE);
                                                }
                                            }
                                        });
                            }
                        });
            }
            public void onNothingSelected(AdapterView<?> parent) {
                //This is required
            }
        });

        departments.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                departmentSelected = parent.getItemAtPosition(pos).toString();
                submitButton.setVisibility(VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void advisorRegistration(View v) {
        userRef = db.collection("users").document(user.getEmail());
        userRef.update(
                "advisor", advisorCategory,
                "college", collegeSelected,
                "department", departmentSelected)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Registered",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Could not register",
                                Toast.LENGTH_SHORT).show();
                        Log.w("UpdateError", "Error updating document", e);
                    }
                });
        Intent intent = new Intent(this, FacultyDashboardActivity.class);
        startActivity(intent);
    }
}
