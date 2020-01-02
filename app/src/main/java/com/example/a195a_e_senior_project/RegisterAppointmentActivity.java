package com.example.a195a_e_senior_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class RegisterAppointmentActivity extends AppCompatActivity {
    private Spinner advisingOptions;
    private Spinner advisors;
    private String advisingOptionSelected;
    private String advisorSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_appointment);

        // Instantiate global variables
        advisingOptions = (Spinner) findViewById(R.id.advisingOptions);
        advisors = (Spinner) findViewById(R.id.advisors);

        advisingOptions.setPrompt("Select Category");
        advisors.setPrompt("Select Advisor");

        List<String> categories = new ArrayList<String>();
        categories.add("Athletics");
        categories.add("GE Requirements");
        categories.add("Major");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Load the adapter into a spinner (dropdown menu)
        advisingOptions.setAdapter(adapter);

        List<String> advisorsList = new ArrayList<String>();
        categories.add("Test Advisor 1");
        categories.add("Test Advisor 2");
        categories.add("Test Advisor 3");

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                advisorsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Load the adapter into a spinner (dropdown menu)
        advisors.setAdapter(adapter2);
    }

    public void setDate(View view) {
        
    }
}
