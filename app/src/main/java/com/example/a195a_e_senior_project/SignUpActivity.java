package com.example.a195a_e_senior_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;

/**
 * User registers an account here.
 */
public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner selectRole;
    private String roleSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize mAuth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Cloud Firestore instance
        db = FirebaseFirestore.getInstance();
        selectRole = (Spinner) findViewById(R.id.selectRole);

        List<String> categories = new ArrayList<String>();
        categories.add("Student");
        categories.add("Faculty");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectRole.setAdapter(adapter);

        selectRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                roleSelected = parent.getItemAtPosition(pos).toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    /**
     * Registers user
     * @param view
     */
    public void signUp(View view) {
        EditText username = (EditText) findViewById(R.id.username);
        EditText firstName = (EditText) findViewById(R.id.firstName);
        EditText lastName = (EditText) findViewById(R.id.lastName);
        EditText password = (EditText) findViewById(R.id.password);
        EditText confirmPassword = (EditText) findViewById(R.id.confirmPassword);

        final String usernameString = username.getText().toString();
        final String passwordString = password.getText().toString();
        final String confirmPasswordString = confirmPassword.getText().toString();
        final String firstNameString = capitalize(firstName.getText().toString());
        final String lastNameString = capitalize(lastName.getText().toString());

        // Enforce constraints upon sign up.
        if (!passwordString.equals(confirmPasswordString)) {
            Toast.makeText(this, "Passwords must match", Toast.LENGTH_LONG).show();
        }

        if (usernameString.matches("") || passwordString.matches("")
        || firstNameString.matches("") || lastNameString.matches("")) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_LONG).show();
        }
        else {
            // Create new user with the information they provided
            mAuth.createUserWithEmailAndPassword(usernameString, passwordString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                Log.d("Create User: ", "createUserWithEmail:success");

                                // Add User into database
                                Map<String, Object> addUser = new HashMap<>();
                                addUser.put("first", firstNameString);
                                addUser.put("last", lastNameString);
                                addUser.put("isFaculty", false);
                                addUser.put("college", "Undeclared");
                                addUser.put("department", "Undeclared");
                                db.collection("users").document(usernameString)
                                        .set(addUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Doc: ", "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Doc: ", "Error writing document", e);
                                            }
                                        });


                                // Sign in user
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Change user's display name to their first name and last name
                                UserProfileChangeRequest.Builder changeRequest = new UserProfileChangeRequest.Builder();
                                changeRequest.setDisplayName(firstNameString + " " + lastNameString);
                                user.updateProfile(changeRequest.build());

                                // Redirect user back to main activity
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else {
                                // If sign in fails, display a message to the user.
                                Log.w("Create User: ", "createUserWithEmail:failure", task.getException());
                                try {
                                    throw task.getException();
                                }
                                catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(SignUpActivity.this, "Password must be 6 characters long",
                                            Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e) {
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }
}
