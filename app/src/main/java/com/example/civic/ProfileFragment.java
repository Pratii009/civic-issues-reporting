package com.example.civic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone, tvLogout, tvMyIssues;
    private Button btnEditProfile;
    private ImageView imgProfile;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currUid = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvLogout = view.findViewById(R.id.tvLogout);
        tvMyIssues = view.findViewById(R.id.tvMyIssues);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        imgProfile = view.findViewById(R.id.imgProfile);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            new Handler().postDelayed(() -> {
                FirebaseUser u = mAuth.getCurrentUser();
                if (u == null) {
                    Intent i = new Intent(getContext(), Main2Activity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    currUid = u.getUid();
                    loadUserProfile();
                }
            }, 400);
        } else {
            currUid = user.getUid();
            loadUserProfile();
        }

        btnEditProfile.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), EditProfileActivity.class);
            startActivity(i);
        });

        tvMyIssues.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), MyIssuesActivity.class);
            startActivity(i);
        });

        tvLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent i = new Intent(getContext(), Main2Activity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });

        return view;
    }

    // Use get() for one-time document fetch
    private void loadUserProfile() {
        DocumentReference docRef = db.collection("citizens").document(currUid);

        docRef.get().addOnSuccessListener(snapshot -> {
            if (!isAdded()) return;
            if (snapshot == null || !snapshot.exists()) {
                Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = snapshot.getString("name");
            String email = snapshot.getString("email");
            String phone = snapshot.getString("phone");
            String imageUrl = snapshot.getString("profileImage");

            tvName.setText(name != null ? name : "No Name");
            tvEmail.setText(email != null ? email : "No Email");
            tvPhone.setText(phone != null ? phone : "No Phone");

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_person)
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.ic_person);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show()
        );
    }
}
