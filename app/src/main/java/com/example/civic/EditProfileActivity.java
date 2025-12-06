package com.example.civic;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etMobile, etCity;
    private Button btnSaveProfile, btnChangePhoto, btnChangePassword;
    private ImageView imgProfile;
    private Switch switchDarkMode;
    private Uri imageUri;

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private final ActivityResultLauncher<Intent> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            imgProfile.setImageURI(imageUri);
                            uploadImageToFirebase();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etCity = findViewById(R.id.etCity);
        btnSaveProfile = findViewById(R.id.btnSave);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        imgProfile = findViewById(R.id.imgProfile);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profileImages");

        db.collection("citizens").document(user.getUid()).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                etName.setText(snapshot.getString("name"));
                etEmail.setText(snapshot.getString("email"));
                etMobile.setText(snapshot.getString("phone"));
                etCity.setText(snapshot.getString("city"));
                String imageUrl = snapshot.getString("profileImage");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_person).into(imgProfile);
                }
            }
        });

        btnChangePhoto.setOnClickListener(v -> checkPhotoPermissionsAndPick());

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean dark = prefs.getBoolean("darkMode", false);

        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void checkPhotoPermissionsAndPick() {

        String mediaPermission;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            mediaPermission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            mediaPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, mediaPermission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{mediaPermission},
                    100
            );
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission required to access photos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null && user != null) {
            StorageReference fileRef = storageRef.child(user.getUid() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                db.collection("citizens").document(user.getUid())
                                        .update("profileImage", uri.toString())
                                        .addOnSuccessListener(aVoid -> {
                                            Glide.with(this).load(uri).placeholder(R.drawable.ic_person).into(imgProfile);
                                            Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Firestore update failed", Toast.LENGTH_SHORT).show());
                            }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String mobile = etMobile.getText().toString();
        String city = etCity.getText().toString();

        db.collection("citizens").document(user.getUid())
                .update("name", name, "email", email, "phone", mobile, "city", city)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}