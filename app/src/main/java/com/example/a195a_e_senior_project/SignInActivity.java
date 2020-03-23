package com.example.a195a_e_senior_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * User can sign in with an account here.
 */
public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
    }

    public void logIn(View view) {
        EditText username = (EditText) findViewById(R.id.username);
        EditText password = (EditText) findViewById(R.id.password);

        String usernameString = username.getText().toString();
        String passwordString = password.getText().toString();

        // Make sure fields are full.
        if(TextUtils.isEmpty(usernameString)) {
            Toast.makeText(this, "Enter a username", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(passwordString)) {
            Toast.makeText(this, "Enter a password", Toast.LENGTH_LONG).show();
        }

        // Sign in method. Look at Firebase documentation for more info.
        mAuth.signInWithEmailAndPassword(usernameString, passwordString)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign In: ", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Redirect user back to main activity
                            Intent intent = new Intent(SignInActivity.this, Home.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Sign In", "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
