package com.example.civic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageView imagePreview;
    private VideoView videoPreview;
    private EditText etDescription;
    private Button btnSubmit;

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private String locationName = "";

    private DatabaseReference databaseRef;

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> photoLauncher;
    private ActivityResultLauncher<Intent> videoLauncher;

    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        databaseRef = FirebaseDatabase.getInstance().getReference("reports");

        imagePreview = view.findViewById(R.id.imagePreview);
        videoPreview = view.findViewById(R.id.videoPreview);
        etDescription = view.findViewById(R.id.etDescription);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        Button btnPhoto = view.findViewById(R.id.btnPhoto);
        Button btnVideo = view.findViewById(R.id.btnVideo);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean camera = result.getOrDefault(Manifest.permission.CAMERA, false);
                    if (!Boolean.TRUE.equals(fine) || !Boolean.TRUE.equals(camera)) {
                        Toast.makeText(getContext(), "Permissions denied.", Toast.LENGTH_SHORT).show();
                    } else {
                        getDeviceLocation();
                    }
                });

        photoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                        imagePreview.setImageBitmap((android.graphics.Bitmap) result.getData().getExtras().get("data"));
                        imagePreview.setVisibility(View.VISIBLE);
                        videoPreview.setVisibility(View.GONE);
                    }
                });

        videoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        videoPreview.setVideoURI(result.getData().getData());
                        videoPreview.setVisibility(View.VISIBLE);
                        imagePreview.setVisibility(View.GONE);
                        videoPreview.start();
                    }
                });

        btnPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoLauncher.launch(intent);
        });
        btnVideo.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            videoLauncher.launch(intent);
        });
        btnSubmit.setOnClickListener(v -> saveReportToFirebase());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.reportMap);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        requestPermissions();
        return view;
    }

    private void requestPermissions() {
        permissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        });
    }

//    private void getDeviceLocation() {
//        try {
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                fusedLocationClient.getLastLocation()
//                        .addOnSuccessListener(location -> {
//                            if (location != null) {
//                                currentLocation = location;
//                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                                if (mMap != null) {
//                                    mMap.clear();
//                                    mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
//                                }
//                                getLocationNameFromCoordinates(location.getLatitude(), location.getLongitude());
//                            }
//                        });
//            }
//        } catch (SecurityException e) {
//            Toast.makeText(getContext(), "Location permissions missing!", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                // 1. First try last known location (fast, may be accurate)
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                processLocation(location);
                            } else {
                                // 2. If last location is unavailable/null, request fresh GPS/WiFi fix
                                LocationRequest locationRequest = LocationRequest.create()
                                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                        .setInterval(3000)         // Try every 3s
                                        .setFastestInterval(1000)
                                        .setNumUpdates(1);         // Only one update needed

                                LocationCallback locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        if (locationResult == null) {
                                            Toast.makeText(getContext(), "Couldn't fetch location.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Location freshLocation = locationResult.getLastLocation();
                                        if (freshLocation != null) {
                                            processLocation(freshLocation);
                                        }
                                        // Stop after one update
                                        fusedLocationClient.removeLocationUpdates(this);
                                    }
                                };
                                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                            }
                        });
            }
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Location permissions missing!", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper function to process and display location and update the map/UI
    private void processLocation(Location location) {
        currentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }
        getLocationNameFromCoordinates(location.getLatitude(), location.getLongitude());
    }

    private void getLocationNameFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null) locationName = address.getLocality();
                else if (address.getSubAdminArea() != null) locationName = address.getSubAdminArea();
                else if (address.getAdminArea() != null) locationName = address.getAdminArea();
            }
        } catch (IOException e) { locationName = ""; }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }
    }

    private void saveReportToFirebase() {
        final String description = etDescription.getText().toString().trim();
        final double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
        final double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;

        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Please add a description.", Toast.LENGTH_SHORT).show();
            return;
        }
        final String reportId = databaseRef.push().getKey();
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a simple report object (add more fields if needed)
        Report report = new Report(reportId,
                description, latitude, longitude, null, null, System.currentTimeMillis(), locationName, userId
        );
        // Your Report constructor should match this, or adjust as needed.

        if (reportId != null) {
            databaseRef.child(reportId).setValue(report)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Report submitted!", Toast.LENGTH_LONG).show();
                        etDescription.setText("");
                        imagePreview.setImageDrawable(null);
                        videoPreview.setVideoURI(null);
                        imagePreview.setVisibility(View.GONE);
                        videoPreview.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Save error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }

    }
}
