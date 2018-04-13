package com.ninjageeksco.projects.aucinfo;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName,profileCountry,profileGender,profileDate;
    private CircleImageView profilePic;
    //FireBase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Students");

        profileName = findViewById(R.id.profile_name);
        profileCountry = findViewById(R.id.profile_country_set);
        profileGender = findViewById(R.id.profile_gender_set);
        profileDate = findViewById(R.id.profile_dateofbirth_set);
        profilePic = findViewById(R.id.profile_profile_image);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToAccountSettingsActivity();
            }
        });

        UpdatingProfile();
    }

    private void SendUserToAccountSettingsActivity() {
        Intent intent = new Intent(this,AccountSettingsActivity.class);
        startActivity(intent);
    }

    private void UpdatingProfile() {
        final String CURRENT_USER_ID = mAuth.getCurrentUser().getUid();
        mDatabaseReference.child(CURRENT_USER_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                   final String fullname = dataSnapshot.child("firstName").getValue().toString() + " "
                            + dataSnapshot.child("lastName").getValue().toString();
                        profileName.setText(fullname);
                    final String gender = dataSnapshot.child("gender").getValue().toString();
                    profileGender.setText(gender);
                    final String country = dataSnapshot.child("country").getValue().toString();
                    profileCountry.setText(country);
                    final String date = dataSnapshot.child("birthDate").getValue().toString();
                    profileDate.setText(date);
                   final String image = dataSnapshot.child("profilePic").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(profilePic);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


}
