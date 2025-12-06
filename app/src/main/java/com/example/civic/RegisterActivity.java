package com.example.civic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    Button btnRegister;
    private ImageView eyeIcon;
    private boolean passwordVisible = false;
    TextView tvLogin;
    FirebaseAuth Auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        eyeIcon = findViewById(R.id.ivEyeIcon);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        Auth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordVisible) {

                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeIcon.setImageResource(R.drawable.ic_eye_closed); // closed eye icon
                    passwordVisible = false;
                } else {

                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeIcon.setImageResource(R.drawable.ic_eye_open); // open eye icon
                    passwordVisible = true;
                }
                etPassword.setSelection(etPassword.length());
            }
        });

    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {

                    addToDatabase(name, email, phone, password);

                    Toast.makeText(RegisterActivity.this, "Registration Successful :)", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, Main2Activity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(RegisterActivity.this, "Registration Unsuccessful :(", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void addToDatabase(String name, String email, String phone, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("password", password);
//        db.collection("users").set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                etEmail.getText().clear();
//                etPassword.getText().clear();
//                etConfirmPassword.getText().clear();
//                etName.getText().clear();
//                etPhone.getText().clear();
//            }
//        });

        FirebaseUser user = Auth.getCurrentUser();

        if (user != null) {
            db.collection("citizens").add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    etEmail.getText().clear();
                    etPassword.getText().clear();
                    etConfirmPassword.getText().clear();
                    etName.getText().clear();
                    etPhone.getText().clear();
                }


            });


        }

//        Auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> { // ERROR 1: Compiler needs to know what 'task' is
//                    if (task.isSuccessful()) {
//                        // ... profile data logic starts here
//                        if (task.isSuccessful()) {
//                            String uid = Auth.getCurrentUser().getUid();
//
//                            // Get data from your input fields
//                            String nameFromInput = etName.getText().toString(); // Example input
//                            String phoneFromInput = etPhone.getText().toString(); // Example input
//                            //email = Auth.getCurrentUser().getEmail(); // Get email from Auth
//
//                            // Prepare the profile data map
//                            Map<String, Object> userData = new HashMap<>();
//                            userData.put("name", nameFromInput);
//                            userData.put("email", email);
//                            userData.put("phone", phoneFromInput);
//
//                            // CRITICAL: Write the document to Firestore using the UID as the document ID
//                            db.collection("citizens").document(uid).set(userData)
//                                    .addOnSuccessListener(aVoid -> {
//                                        Log.d("Registration", "Profile document created successfully.");
//                                        // Proceed to MainActivity/Home
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Log.e("Registration", "Error writing document: " + e.getMessage());
//                                        // Show error to user
//                                    });
//                        }
//
//                    }
//                });




    }
}


