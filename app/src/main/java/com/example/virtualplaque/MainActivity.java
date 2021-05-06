package com.example.virtualplaque;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intent intent= new Intent(this, Login.class);
        //this.startActivity(intent);
    }

    //Open Profiles Page
    public void profile(View view){
        startActivity(new Intent(getApplicationContext(),Profile.class));
    }

    //Open MapBox Map Page
    public void map(View view) {
        startActivity(new Intent(getApplicationContext(),Map.class));
    }

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
        startActivity(new Intent(getApplicationContext(),MapBMap.class));
    }

    public void addPlaquePage(View view) {
        startActivity(new Intent(getApplicationContext(),AddPlaque.class));
    }
}