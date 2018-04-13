package com.ninjageeksco.projects.aucinfo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    private EditText firstName,lastName,Country,dateOfBirth,Gender;
    private Button update,cancel;
    private CircleImageView profilePic;
    //FireBase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Students");

        firstName = findViewById(R.id.edit_profile_firstname);
        lastName = findViewById(R.id.edit_profile_lastname);
        Country = findViewById(R.id.edit_profile_country);
        dateOfBirth = findViewById(R.id.edit_profile_birthdate);
        Gender = findViewById(R.id.edit_profile_gender);
        update = findViewById(R.id.edit_profile_update);
        cancel = findViewById(R.id.edit_profile_cancel);
        profilePic = findViewById(R.id.edit_profile_image);

        UpdatingProfile();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AccountSettingsActivity.this, "Should Update the Profile", Toast.LENGTH_SHORT).show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); }});



    }

    private void UpdatingProfile() {
        final String CURRENT_USER_ID = mAuth.getCurrentUser().getUid();
        mDatabaseReference.child(CURRENT_USER_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String firstname = dataSnapshot.child("firstName").getValue().toString();
                firstName.setText(firstname);
                final String lastname = dataSnapshot.child("lastName").getValue().toString();
                lastName.setText(lastname);
                final String gender = dataSnapshot.child("gender").getValue().toString();
                Gender.setText(gender);
                final String country = dataSnapshot.child("country").getValue().toString();
                Country.setText(country);
                final String date = dataSnapshot.child("birthDate").getValue().toString();
                dateOfBirth.setText(date);
                final String image = dataSnapshot.child("profilePic").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.profile).into(profilePic);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
