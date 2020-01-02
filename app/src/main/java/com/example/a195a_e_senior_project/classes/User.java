package com.example.a195a_e_senior_project.classes;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Use this object to represent our currently authenticated user.
 */
public class User {
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private String college;
    private String department;
    private boolean isFaculty;

    /**
     * Upon object creation, we only have to access relevant information once so we don't have to load up our code with queries for user information.
     * @param user
     */
    public User(FirebaseUser user) {
        this.user = user;
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getEmail());

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        college = (String) document.getData().get("college");
                        department = (String) document.getData().get("department");
                        isFaculty = (boolean) document.getData().get("isFaculty");
                    } else {
                        Log.d("Document ", "Does not exist");
                    }
                } else {
                    Log.d("Failure ", "get failed with ", task.getException());
                }
            }
        });
    }

    public String getName() {
        return user.getDisplayName();
    }
}
