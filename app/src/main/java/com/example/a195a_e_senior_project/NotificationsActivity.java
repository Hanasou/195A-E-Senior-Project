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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.INVISIBLE;

public class NotificationsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private CollectionReference notifRef;
    private ListView notifView;
    private TextView noMessage;

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
                Intent homeIntent = new Intent(this, MainActivity.class);
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
        setContentView(R.layout.activity_notifications);

        // Initialize global variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getEmail());
        notifRef = userRef.collection("notifications");
        notifView = findViewById(R.id.notifications);
        noMessage = findViewById(R.id.noMessage);

        notifRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> notifItems = new ArrayList<String>();
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                String notifContent = (String) document.get("content");
                                Log.d("DocGet ", notifContent);
                                notifItems.add(notifContent);
                            }
                            if (!notifItems.isEmpty()) {
                                noMessage.setVisibility(View.GONE);
                            }
                            ArrayAdapter<String> notifAdapter = new ArrayAdapter<String>(NotificationsActivity.this,
                                    android.R.layout.simple_list_item_1, notifItems);
                            notifView.setAdapter(notifAdapter);
                        } else {
                            Log.d("DocGet", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

}
