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
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import static android.view.View.VISIBLE;

/**
 * User can register for a specific college and department here.
 */
public class DepartmentRegistrationActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private Spinner collegeSearch;
    private Spinner departmentSearch;
    private Button submitButton;
    private String collegeSelected;
    private String departmentSelected;
    private CollectionReference collegesRef;
    private CollectionReference departmentsRef;
    private DocumentReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_registration);

        // Initialize global variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        collegeSelected = "";
        departmentSelected = "";
        collegeSearch = findViewById(R.id.collegeSearch);
        departmentSearch = findViewById(R.id.departmentSearch);
        submitButton = findViewById(R.id.submitButton);
        collegesRef = db.collection("universities")
                .document("SJSU").collection("colleges");
        departmentsRef = null;
        userRef = null;

        // Configure the Item Selected listener to do something when something gets selected.
        collegeSearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                                                   List<String> departments = new ArrayList<String>();
                                                   for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                                                       departments.add(document.getData().get("name").toString());
                                                   }
                                                   // Use array adapter to adapt items into the format we want them in.
                                                   ArrayAdapter<String> adapter = new ArrayAdapter<String>(DepartmentRegistrationActivity.this,
                                                           android.R.layout.simple_spinner_item, departments);
                                                   adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                                   // Load the adapter into a spinner (dropdown menu)
                                                   departmentSearch.setAdapter(adapter);
                                                   departmentSearch.setVisibility(VISIBLE);
                                               }
                                           }
                                        });
                            }
                });

            }
            public void onNothingSelected(AdapterView<?> parent) {
                //Method is required as defined by interface
            }
        });

        departmentSearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                departmentSelected = parent.getItemAtPosition(pos).toString();
                Toast.makeText(parent.getContext(), departmentSelected,
                        Toast.LENGTH_SHORT).show();
                submitButton.setVisibility(VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        collegesRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d("Result ", "onSuccess: LIST EMPTY");
                            return;
                        }
                        else {
                            List<String> colleges = new ArrayList<String>();
                            for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                                colleges.add(document.getData().get("name").toString());
                            }
                            // Use array adapter to adapt items into the format we want them in.
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(DepartmentRegistrationActivity.this, android.R.layout.simple_spinner_item,
                                    colleges);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            // Load the adapter into a spinner (dropdown menu)
                            collegeSearch.setAdapter(adapter);

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

    /**
     * User signs up for their respective College. Possibly add department registration to this as well.
     * @param view
     */
    public void registerCollege(View view) {
        userRef = db.collection("users").document(user.getEmail());
        userRef.update(
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
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }
}
