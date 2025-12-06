package com.example.civic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
  BottomNavigationView bnView;

//    private void getFCMToken() {
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(task -> {
//                    if (!task.isSuccessful()) {
//                        Log.w("FCM_Token", "Fetching FCM registration token failed", task.getException());
//                        return;
//                    }
//                    String token = task.getResult();
//                });
//    }

    private void getFCMToken() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_Token", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM_Token", "Token: " + token);

                    DatabaseReference userRef =
                            FirebaseDatabase.getInstance().getReference("users").child(uid);
                    userRef.child("fcmToken").setValue(token);
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFCMToken();
        handleReportIntent(getIntent());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent().getExtras() != null) {
            String reportId = getIntent().getStringExtra("reportId");
            if (reportId != null) {
                navigateToHomeAndOpenReport(reportId);
            }
        }
        bnView=findViewById(R.id.bottom_nav);

        bnView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id= menuItem.getItemId();
                if(id==R.id.nav_home){
                    loadFrag(new HomeMainFragment(),false);

                } else if (id==R.id.report) {
                    loadFrag(new ReportFragment(),false);

                } else if (id==R.id.issues) {
                    loadFrag(new NotificationFragment(),false);

                }else{
                   loadFrag(new ProfileFragment(),false);

                }


                return true;
            }
        });
        bnView.setSelectedItemId(R.id.nav_home);
    }

    public void loadFrag(Fragment fragment,boolean flag){
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft= fm.beginTransaction();
        if(flag)
        ft.add(R.id.mainFrame,fragment);
        else {
            ft.replace(R.id.mainFrame, fragment);
        }
        ft.commit();


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleReportIntent(intent);
    }

    private void handleReportIntent(Intent intent) {
        String reportId = intent.getStringExtra("reportId");

        if (reportId == null && intent.getData() != null &&
                intent.getData().getPath().startsWith("/report")) {
            reportId = intent.getData().getLastPathSegment();
        }

        if (reportId != null) {
            navigateToHomeAndOpenReport(reportId);
        }
    }

    public void navigateToHomeAndOpenReport(String reportId) {

        HomeMainFragment homeFragment = new HomeMainFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, homeFragment)
                .commit();
    }

}