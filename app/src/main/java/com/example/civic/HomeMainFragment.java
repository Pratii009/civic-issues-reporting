package com.example.civic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeMainFragment extends Fragment {

    public HomeMainFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_homemain, container, false);

        LinearLayout btnReportIssue        = view.findViewById(R.id.btnReportIssue);
        LinearLayout municipalContactCard  = view.findViewById(R.id.municipalContactCard);
        LinearLayout emergencyHelplineCard = view.findViewById(R.id.emergencyHelplineCard);

        btnReportIssue.setOnClickListener(v -> {
            navigateToFragment(new ReportFragment());
        });

        municipalContactCard.setOnClickListener(v -> {
            Toast.makeText(
                    getActivity(),
                    "Municipal Office: 022-12345678\nSanitation: 022-98765432\nWater Supply: 022-66554433",
                    Toast.LENGTH_LONG
            ).show();
        });

        emergencyHelplineCard.setOnClickListener(v -> {
            Toast.makeText(
                    getActivity(),
                    "Police: 100\nFire: 101\nAmbulance: 102",
                    Toast.LENGTH_LONG
            ).show();
        });

        return view;
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentManager manager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.mainFrame, fragment);  // mainFrame must exist in activity layout
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
