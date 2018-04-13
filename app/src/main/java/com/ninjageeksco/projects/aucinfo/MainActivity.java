package com.ninjageeksco.projects.aucinfo;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseUser muser;
    private DatabaseReference mDatabaseReference;
    private String currentUserId;
    private TextView navProfileUsername;
    private CircleImageView navProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckingOfGooglePlayService();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToAddEventActivity();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navview = navigationView.inflateHeaderView(R.layout.nav_header_main);

        navProfileImage = navview.findViewById(R.id.nav_imageView);
        navProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { SendUserToProfileActivity(); }});
        navProfileUsername = navview.findViewById(R.id.nav_username);
        navProfileUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { SendUserToProfileActivity(); }});

    }



    @Override
    protected void onStart() {
        super.onStart();
        CheckingOfGooglePlayService();
    }

    private void ChechUserExistance() {
        final String CURRENT_USER_ID = mAuth.getCurrentUser().getUid();
        mDatabaseReference.child(CURRENT_USER_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    SendUserToSetupProfileActivity();
                    finish();
                } else if (dataSnapshot.hasChildren()) {
                    String fullname = dataSnapshot.child("firstName").getValue().toString() + " "
                            + dataSnapshot.child("lastName").getValue().toString();
                    navProfileUsername.setText(fullname);
                    String image = dataSnapshot.child("profilePic").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_settings) {
            SendUserToAccountSettingsActivity();
            return true;
        } else if (id == R.id.menu_signout){
            mAuth.signOut();
            SendUserToLoginActivity();
        }

        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            SendUserToProfileActivity();
        } else if (id == R.id.nav_gallery) {
        } else if (id == R.id.nav_engine) {
            SendUserToChatActivity(0);
        } else if (id == R.id.nav_econ) {
            SendUserToChatActivity(1);
        } else if (id == R.id.nav_parking) {
            SendUserToChatActivity(2);
        } else if (id == R.id.nav_manage) {
            SendUserToAccountSettingsActivity();
        } else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_send) {
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            SendUserToLoginActivity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void CheckingOfGooglePlayService() {
    GoogleApiAvailability api = GoogleApiAvailability.getInstance();
    int code = api.isGooglePlayServicesAvailable(this);
        if(code ==ConnectionResult.SUCCESS)
    {
        mAuth = FirebaseAuth.getInstance();
        muser = mAuth.getCurrentUser();
        currentUserId = muser.getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Students");
        FirebaseUser currFirebaseUser = mAuth.getCurrentUser();
        if (currFirebaseUser == null) {
            SendUserToLoginActivity();
        } else {
            ChechUserExistance();
        }
    } else if (code == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
        ShowingDialogForGooglePlayService();
        }else if (code == ConnectionResult.SERVICE_MISSING){
            ShowingDialogForGooglePlayService();
        }else {
            ShowingDialogForGooglePlayService();
        }
}

    private void ShowingDialogForGooglePlayService() {
        AlertDialog.Builder alertDialog =
                new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        alertDialog.setMessage("You need to download Google Play Services in order to use this application");
        alertDialog.setTitle("Important");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SnedingUserToGooglePlayService();
                finish();}
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { finish();}
        });
        alertDialog.show();
    }


    private void SnedingUserToGooglePlayService(){
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms" + appPackageName)));
        }
    }


    private void Messagessss(){
        NotificationCompat.Builder mbBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Welcome")
                .setContentText("Welcome Sir");
        NotificationManager notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        notificationManager.notify(2,mbBuilder.build());
    }
    private void SendUserToSetupProfileActivity() {
        Intent intent = new Intent(this,SetupProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void SendUserToProfileActivity() {
        Intent intent = new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }
    private void SendUserToLoginActivity() {
        Intent intent = new Intent(this,LoginRegActivity.class);
        startActivity(intent);
    }
    private void SendUserToAccountSettingsActivity() {
        Intent intent = new Intent(this,AccountSettingsActivity.class);
        startActivity(intent);
    }
    private void SendUserToChatActivity(int value) {
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("Value",value);
        startActivity(intent);
    }
    private void SendUserToAddEventActivity() {
        Intent intent = new Intent(this,AddEvenActivity.class);
        startActivity(intent);
    }
}
