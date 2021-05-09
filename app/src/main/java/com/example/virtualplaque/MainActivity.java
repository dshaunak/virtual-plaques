package com.example.virtualplaque;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button SetPlaqueBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        SetPlaqueBt =findViewById(R.id.addPlaqueBt);

        SetPlaqueBt.setVisibility(View.INVISIBLE);

        //Intent intent= new Intent(this, Login.class);
        //this.startActivity(intent);
    }

    //Open Profiles Page
    public void profile(View view){
        startActivity(new Intent(getApplicationContext(),Profile.class));
    }

    //Open MapBox Map Page


    //Open Logout Page
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut(); //Logout the user
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }


    public void mapPage(View view) {
        startActivity(new Intent(getApplicationContext(),MapPage.class));
    }

    public void mapBoxPage(View view) {
        startActivity(new Intent(getApplicationContext(),MapBoxMap.class));
    }

    public void addPlaquePage(View view) {
        startActivity(new Intent(getApplicationContext(),AddPlaque.class));
    }
}

/*
-------- Removed Map Page
XML command
        android:onClick="map"
* public void map(View view) {
        startActivity(new Intent(getApplicationContext(),Map.class));
    }
* */