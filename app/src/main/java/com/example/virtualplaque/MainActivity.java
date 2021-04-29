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

        //Intent intent= new Intent(this, Register.class);
        //this.startActivity(intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut(); //Logout the user
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
}