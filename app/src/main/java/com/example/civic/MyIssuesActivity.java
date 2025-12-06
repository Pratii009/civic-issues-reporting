package com.example.civic;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyIssuesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IssuesAdapter issuesAdapter;
    private List<Issue> issueList;

    private DatabaseReference reportsRef;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_issues);

        recyclerView = findViewById(R.id.recyclerViewIssues);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        issueList = new ArrayList<>();
        issuesAdapter = new IssuesAdapter(issueList);
        recyclerView.setAdapter(issuesAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reportsRef = FirebaseDatabase.getInstance().getReference("reports");

        fetchUserIssues();
    }

    private void fetchUserIssues() {
        reportsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        issueList.clear();
                        if (!snapshot.exists()) {
                            Toast.makeText(MyIssuesActivity.this, "No issues found.", Toast.LENGTH_SHORT).show();
                            issuesAdapter.notifyDataSetChanged();
                            return;
                        }

                        for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                            String description = issueSnapshot.child("description").getValue(String.class);
                            String status = issueSnapshot.child("status").getValue(String.class);
                            if (status == null) status = "Pending";

                            issueList.add(new Issue(description, status));
                        }
                        issuesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MyIssuesActivity", "Failed to fetch issues", error.toException());
                        Toast.makeText(MyIssuesActivity.this, "Failed to fetch issues: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
