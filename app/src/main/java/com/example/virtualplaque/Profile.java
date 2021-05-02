package com.example.virtualplaque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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

public class Profile extends AppCompatActivity {

    TextView fullName, email, notVerified, Verified;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference fCloudStorage;
    String userID;
    Button resendCode, resetPasswordBt, changeProfileBt;
    public static final String TAG = "Verification";
    public static final String TAG1 = "ProfileImage";
    FirebaseUser fUser;
    ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        userID = fAuth.getCurrentUser().getUid();
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

        DocumentReference docRef = fStore.collection("users").document(userID);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot docSnap, @Nullable FirebaseFirestoreException error) {
                fullName.setText(docSnap.getString("fName"));
                email.setText(docSnap.getString("email"));

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
                    // Open Gallery to let user change the profile image
                    Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(openGalleryIntent,1000);
                }
            });
    }
    //Just type 'onActivityResult' on this line to get the option to override onCreate. This is done to work on the Image URI obtained from the Gallery activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1000){
            // 1000 is our given code for the Intent invoking the Gallery activity. This is done to make sure the correct Intent is used, in case of a complex application.
            if(resultCode == Activity.RESULT_OK){
                //making user we have some data in the 'data' variable which is supposed to hold the URI link of the image selected from the Gallery
                Uri imageUri = data.getData();
                profileImage.setImageURI(imageUri);

                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        //Upload Image to Firebase Cloud Storage Function
        StorageReference newStore = fCloudStorage.child("profileImage.jpg");
        newStore.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Profile.this, "Profile Image successfully uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}