package com.example.virtualplaque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {

    TextView fullName, email, notVerified, Verified;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference fCloudStorage;
    String userID;
    String eMail;
    Button resendCode, resetPasswordBt, changeProfileBt;
    public static final String TAG = "Verification";
    public static final String TAG1 = "ProfileImage";
    FirebaseUser fUser;
    ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        fullName = findViewById(R.id.profUsername);
        email = findViewById(R.id.profEmail);
        notVerified = findViewById(R.id.textNotVerified);
        Verified = findViewById(R.id.textVerified);
        resendCode = findViewById(R.id.verifyBt);
        resetPasswordBt = findViewById(R.id.resetPassBt);
        profileImage = findViewById(R.id.profImage);
        changeProfileBt = findViewById(R.id.editProfileBt);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fCloudStorage = FirebaseStorage.getInstance().getReference();

        StorageReference fProfileImg = fCloudStorage.child("users/"+fAuth.getCurrentUser().getEmail()+"/profileImg.jpg");
        fProfileImg.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StorageReference defaultImg = fCloudStorage.child("defaultuser.png");
            }
        });

        userID = fAuth.getCurrentUser().getUid();
        eMail = fAuth.getCurrentUser().getEmail();
        fUser = fAuth.getCurrentUser();

        if(!fUser.isEmailVerified()){
            notVerified.setVisibility(View.VISIBLE);
            resendCode.setVisibility(View.VISIBLE);

            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "Verification E-mail has been sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"Failure: E-mail not sent"+ e.getMessage());
                        }
                    });
                }
            });
        }
        else{
            Verified.setVisibility(View.VISIBLE);
        }

        DocumentReference docRef = fStore.collection("users").document(eMail);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot docSnap, @Nullable FirebaseFirestoreException error) {
                if(docSnap.exists()){
                    fullName.setText(docSnap.getString("fName"));
                    email.setText(docSnap.getString("email"));

                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });
            //RESET Password Method
        resetPasswordBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Setting up the Alert Dialogue to pop up when prompted
                final EditText resetPassword = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter the new password. Must be > 6 characters");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Extract the E-mail and send reset link
                        String pass = resetPassword.getText().toString();

                        if(pass.length() < 6){
                            resetPassword.setError("Password must be greater than 6 characters");
                            Toast.makeText(v.getContext(), "Password must be of 6 or more Characters!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        fUser.updatePassword(pass).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(v.getContext(), "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(v.getContext(), "Password Update Failed." + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing to return to the Login page
                    }
                });
                passwordResetDialog.create().show();
            }
        });


            changeProfileBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(getApplicationContext(),EditProfile.class));
                    /*Intent i = new Intent(v.getContext(),EditProfile.class);
                    i.putExtra("username","Shaunak");
                    i.putExtra("email","shaunsea2@gmail.com");
                    startActivity(i);*/
                }
            });
    }
    //Just type 'onActivityResult' on this line to get the option to override onCreate. This is done to work on the Image URI obtained from the Gallery activity.

    public void back(View view) {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        //finish();
    }
}