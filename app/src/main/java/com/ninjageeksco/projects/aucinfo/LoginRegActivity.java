package com.ninjageeksco.projects.aucinfo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginRegActivity extends AppCompatActivity {

    private Button driLogin,driRegister,loginGoogleButton;
    private EditText driEmail,driPassword;
    private ProgressDialog loadingbar;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String onlineDriverID;
    private GoogleApiClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtity_login_reg);

        mAuth = FirebaseAuth.getInstance();


        driLogin = findViewById(R.id.driv_login);
        driRegister = findViewById(R.id.driv_register);
        driEmail = findViewById(R.id.driv_email);
        driEmail.clearFocus();
        driPassword = findViewById(R.id.driv_pass);
        driPassword.clearFocus();
        loginGoogleButton = findViewById(R.id.login_google_button);


        loadingbar = new ProgressDialog(this);


        driLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = driEmail.getText().toString();
                String pass = driPassword.getText().toString();
                LogInDriver(email,pass);
            }
        });
        driRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = driEmail.getText().toString();
                String pass = driPassword.getText().toString();
                RegisterDriver(email,pass);
            }
        });
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginRegActivity.this, "Connection to Google Sign In Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        loginGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            loadingbar.setTitle("Google Sign in");
            loadingbar.setMessage("Please Wait!");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "Logging  into your Account", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(this, "Error while signing ing with Google account", Toast.LENGTH_SHORT).show();

            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            SendUserToMainActivity();
                            loadingbar.dismiss();

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(LoginRegActivity.this, "Error : " + error, Toast.LENGTH_SHORT).show();
                            SendUserToLoginActivity();
                            loadingbar.dismiss();
                        }
                    }
                });
    }

    private void LogInDriver(String email, String pass) {
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
        }else {
            loadingbar.setMessage("please Wait");
            loadingbar.show();
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(LoginRegActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            } else {
                                String message = task.getException().getMessage().toString();
                                Toast.makeText(LoginRegActivity.this, message, Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }
    }



    private void RegisterDriver(String email, String pass) {
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
        }else{
            loadingbar.setMessage("please Wait");
            loadingbar.show();
            mAuth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                onlineDriverID = mAuth.getCurrentUser().getUid();
                                databaseReference = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Students").child(onlineDriverID);
                                databaseReference.setValue(true);
                                SendUserToMainActivity();
                                Toast.makeText(LoginRegActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }else {
                                String message = task.getException().getMessage().toString();
                                Toast.makeText(LoginRegActivity.this, message, Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }
    }
    private void SendUserToLoginActivity() {
        Intent intent = new Intent(this,LoginRegActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
