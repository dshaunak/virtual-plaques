package com.example.virtualplaque;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

public class AddPlaque extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;
    String eMail;
    EditText longitudeInput, latitudeInput, plaqueTitle, plaqueDescription;
    TextView Address;
    Button backToMapButton, saveButton;

    public static final String TAG = "addPlaque";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plaque);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        eMail=fUser.getEmail();

        longitudeInput=findViewById(R.id.editTextLongitude);
        latitudeInput=findViewById(R.id.editTextLatitude);
        plaqueTitle=findViewById(R.id.editPlaqueTitle);
        plaqueDescription=findViewById(R.id.editPlaqueDescription);
        Address=findViewById(R.id.textAddress);

        saveButton=findViewById(R.id.savePlaqueBt);
        backToMapButton=findViewById(R.id.backToMapBt);

        Intent data= getIntent();
        String address = data.getStringExtra("address");
        Double longitude = data.getDoubleExtra("longitude",0.0);
        Double latitude = data.getDoubleExtra("latitude",0.0);

        String longS = Double.toString(longitude);
        String latS = Double.toString(latitude);

        longitudeInput.setText(longS);
        latitudeInput.setText(latS);
        Address.setText(address);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String longitude = longitudeInput.getText().toString().trim();
                String latitude = latitudeInput.getText().toString().trim();
                String plaqueName = plaqueTitle.getText().toString().trim();
                String plaqueText = plaqueDescription.getText().toString().trim();
                int flag =1;
                /*
                //longitude.isEmpty() || latitude.isEmpty() || plaqueName.isEmpty() || plaqueText.isEmpty()
                if(TextUtils.isEmpty(longitude)){
                    longitudeInput.setError("Longitude is required");
                    return;
                }

                if(TextUtils.isEmpty(latitude)){
                    latitudeInput.setError("Latitude is required");
                    return;
                }
                */

                if(TextUtils.isEmpty(plaqueName)){
                    plaqueTitle.setError("Plaque Title is required");
                }

                if(TextUtils.isEmpty(plaqueText)){
                    plaqueDescription.setError("Plaque Description is required");
                }

                DocumentReference fDocRef = fStore.collection("users").document(eMail).collection("plaques").document(plaqueName);

                Map<String, Object> plaque = new HashMap<>();
                plaque.put("loCord",longitude);
                plaque.put("laCord",latitude);
                plaque.put("plaqueTitle",plaqueName);
                plaque.put("plaqueDescription",plaqueText);

                fDocRef.set(plaque).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Plaque added to Map and Database:" + plaqueName);
                        Toast.makeText(AddPlaque.this,"Plaque Added", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Plaque not added: " + plaqueName);
                        Toast.makeText(AddPlaque.this,"Plaque Addition Failed", Toast.LENGTH_LONG).show();
                    }
                });

                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        backToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MapBoxMap.class));
            }
        });


    }

    public void backtoMap(View view) {
    }

    public void backtoMain(View view) {

    }
}
