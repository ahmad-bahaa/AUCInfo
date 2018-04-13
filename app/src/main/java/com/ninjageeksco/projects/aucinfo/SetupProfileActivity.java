package com.ninjageeksco.projects.aucinfo;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupProfileActivity extends Activity {

    private EditText firstName,lastName;
    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener dote;
    private CircleImageView mcircleImageView;
    private Button button;
    private TextView birthDate;
    private ProgressDialog loadingBar;
    private Spinner spinner;
    private RadioButton male,female;
    //Firebas
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private String currentUserId,downloadUrl,userCountry;
    final static int PROFILE_PICTURE_PICK = 1;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Students").child(currentUserId);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

             myCalendar = Calendar.getInstance();

             firstName = findViewById(R.id.setup_first_name);
             firstName.clearFocus();
             lastName = findViewById(R.id.setup_last_name);
            lastName.clearFocus();
             birthDate = findViewById(R.id.setup_birthdate);
             mcircleImageView = findViewById(R.id.setup_profile_image);
              spinner = findViewById(R.id.spinner);
              male = findViewById(R.id.setup_male);
            female = findViewById(R.id.setup_female);
             button = findViewById(R.id.setup_button);
              loadingBar = new ProgressDialog(this);

        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (country.trim().length() > 0 && !countries.contains(country)) {
                countries.add(country);
            }
        }
        Collections.sort(countries);
        for (String country : countries) {
            // System.out.println(country);
        }
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the your spinner
        spinner.setAdapter(countryAdapter);
        userCountry = spinner.getSelectedItem().toString();


             dote = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
                    }
                };

                birthDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(SetupProfileActivity.this,dote,
                            myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                         }});
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SaveAccountSetupInformation();
                    }
                });
             mcircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PROFILE_PICTURE_PICK);
                }
        });
                     }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_PICTURE_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Preparing Your Picture");
                loadingBar.setMessage("Please Wait!");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                Uri resultUri = result.getUri();
                //mcircleImageView.setImageURI(resultUri);
                StorageReference filePath = mStorageReference.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SetupProfileActivity.this, "Your Profile Picture Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            downloadUrl = task.getResult().getDownloadUrl().toString();
                            mDatabaseReference.child("profilePic").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Intent intent = new Intent(SetupActivity.this,SetupActivity.class );
                                        // startActivity(intent);
                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.profile).into(mcircleImageView);
                                        Toast.makeText(SetupProfileActivity.this, "Profile Picture Stored", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SetupProfileActivity.this, message , Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(SetupProfileActivity.this, message , Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Image Can't Be Cropped Try Again!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        birthDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void SaveAccountSetupInformation() {
        final String mfirstname = firstName.getText().toString();
        final String mlastname = lastName.getText().toString();
        final String birthdate = birthDate.getText().toString();
        final String countrytext = userCountry;
        String mgender = "";
        if (female.isChecked()) {
            mgender = "Female";
        } else if (male.isChecked()) {
            mgender = "Male";
        }

        if (TextUtils.isEmpty(mfirstname)) {
            Toast.makeText(this, "Please Write Your First Name", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(mlastname)) {
            Toast.makeText(this, "Please Write Your Last Name", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(birthdate)) {
            Toast.makeText(this, "Please Pick Your BirthDate", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(countrytext)) {
            Toast.makeText(this, "Please Pick Your Country", Toast.LENGTH_LONG).show();
        } else if (downloadUrl == null){
            Toast.makeText(this, "Please Set Your Profile Picture", Toast.LENGTH_LONG).show();
         }else{
            HashMap userMap = new HashMap();
            userMap.put("firstName",mfirstname);
            userMap.put("lastName",mlastname);
            userMap.put("birthDate",birthdate);
            userMap.put("country",countrytext);
            userMap.put("gender",mgender);
            mDatabaseReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SetupProfileActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupProfileActivity.this, "Error : " + message,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
