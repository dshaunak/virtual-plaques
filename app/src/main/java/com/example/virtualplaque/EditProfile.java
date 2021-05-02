package com.example.virtualplaque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    EditText userName, eMail;
    ImageView imageView, editImageView;
    Button saveButton;
    public static final String TAG = "Initialized Edit Profile";

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;
    StorageReference fCloudStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userName = findViewById(R.id.editPersonName);
        eMail = findViewById(R.id.editTextEmail);
        saveButton=findViewById(R.id.saveBt);
        imageView=findViewById(R.id.imageProfImage);
        editImageView=findViewById(R.id.imageProfileEdit);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        fCloudStore = FirebaseStorage.getInstance().getReference();

        Intent data = getIntent();
        //String username = data.getStringExtra("username");
        //String email = data.getStringExtra("email");

        String userID = fUser.getUid();
        DocumentReference docRef = fStore.collection("users").document(userID);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot docSnap, @Nullable FirebaseFirestoreException error) {
                userName.setText(docSnap.getString("fName"));
                eMail.setText(docSnap.getString("email"));
            }
        });

        StorageReference fProfileImg = fCloudStore.child("users/"+fAuth.getCurrentUser().getEmail()+"/profileImg.jpg");
        fProfileImg.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StorageReference defaultImg = fCloudStore.child("defaultuser.png");
            }
        });

        //userName.setText(username);
        //eMail.setText(email);

        editImageView.setOnClickListener(new View.OnClickListener() {
            //-------------------Change Profile Image-----------------------
            @Override
            public void onClick(View v) {

                //Open Gallery to change profile Image
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userName.getText().toString().isEmpty() || eMail.getText().toString().isEmpty()){
                    Toast.makeText(v.getContext(),"Input fields should not be empty.",Toast.LENGTH_SHORT);
                    return;
                }

                String userN = userName.getText().toString();
                String eM = eMail.getText().toString();

                fUser.updateEmail(eM).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = fStore.collection("users").document(fUser.getUid());
                        Map<String, Object> editMap = new HashMap<>();
                        editMap.put("email",eM);
                        editMap.put("fName",userN);
                        docRef.update(editMap);
                        Toast.makeText(EditProfile.this, "User details updated successfully.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "User not updated."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                startActivity(new Intent(getApplicationContext(),Profile.class));
            }
        });

        //Log.d("TAG", "onCreate:" + username + " " + email);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == 1000){
                if(resultCode == Activity.RESULT_OK){
                    Uri imgUri = data.getData();
                    uploadImageToFirebase(imgUri);
                }
            }
    }

    private void uploadImageToFirebase(Uri imgUri){
        //Upload Image to Firebase Cloud Storage Function
        StorageReference updateImg = fCloudStore.child("users/"+fAuth.getCurrentUser().getEmail()+"/profileImg.jpg");
        updateImg.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(EditProfile.this, "Profile Image successfully uploaded.", Toast.LENGTH_SHORT).show();
                updateImg.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(EditProfile.this, "Profile Image downloading...", Toast.LENGTH_SHORT).show();
                        Picasso.get().load(uri).into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Profile Image download failed."+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void profileBack(View view) {
        startActivity(new Intent(getApplicationContext(),Profile.class));
    }
}