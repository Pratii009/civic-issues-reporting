//package com.example.civic;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.os.Bundle;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.res.ResourcesCompat;
//import androidx.fragment.app.Fragment;
//
//public class NotificationFragment extends Fragment {
//
//    public NotificationFragment() {}
//
//    public static NotificationFragment newInstance() {
//        return new NotificationFragment();
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_notification, container, false);
//
//        LinearLayout notificationContainer = view.findViewById(R.id.notificationContainer);
//        loadNotifications(notificationContainer);
//
//        return view;
//    }
//
//    private void loadNotifications(LinearLayout container) {
//        container.removeAllViews();
//
//        SharedPreferences prefs = requireActivity().getSharedPreferences("Notifications", Context.MODE_PRIVATE);
//        String title = prefs.getString("last_notification_title", "No Reports Yet");
//        String body = prefs.getString("last_notification_body", "You have not submitted any issues.");
//        String issueStatus = prefs.getString("last_issue_status", "pending");
//        final String reportId = prefs.getString("last_report_id", null);
//
//        // Create notification card UI manually
//        LinearLayout card = new LinearLayout(getContext());
//        card.setOrientation(LinearLayout.VERTICAL);
//        card.setBackgroundResource(android.R.color.white);
//        card.setPadding(36, 36, 36, 36);
//        card.setElevation(8f);
//
//        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//        cardParams.setMargins(0, 0, 0, 24);
//        card.setLayoutParams(cardParams);
//        card.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.card_bg, null)); // Add a card_bg.xml for rounded corners if desired
//
//        // Status icon and text
//        LinearLayout statusRow = new LinearLayout(getContext());
//        statusRow.setOrientation(LinearLayout.HORIZONTAL);
//        statusRow.setGravity(Gravity.CENTER_VERTICAL);
//
//        ImageView statusIcon = new ImageView(getContext());
//        if ("solved".equals(issueStatus)) {
//            statusIcon.setImageResource(R.drawable.ic_check_circle); // Green check icon in drawable
//        } else {
//            statusIcon.setImageResource(R.drawable.ic_check_circle); // Clock/wait icon in drawable
//        }
//        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
//        iconParams.setMargins(0, 0, 16, 0);
//        statusIcon.setLayoutParams(iconParams);
//
//        TextView statusText = new TextView(getContext());
//        statusText.setText("solved".equals(issueStatus) ? "Solved" : "Pending");
//        statusText.setTextSize(18f);
//        statusText.setTypeface(Typeface.DEFAULT_BOLD);
//        statusText.setTextColor("solved".equals(issueStatus) ? Color.parseColor("#30C97D") : Color.parseColor("#FFA726"));
//
//        statusRow.addView(statusIcon);
//        statusRow.addView(statusText);
//
//        // Issue Title
//        TextView titleView = new TextView(getContext());
//        titleView.setText(title);
//        titleView.setTextSize(20f);
//        titleView.setTypeface(Typeface.DEFAULT_BOLD);
//        titleView.setTextColor(Color.parseColor("#22335A"));
//        titleView.setPadding(0, 24, 0, 12);
//
//        // Issue Body
//        TextView bodyView = new TextView(getContext());
//        bodyView.setText(body);
//        bodyView.setTextSize(16f);
//        bodyView.setTextColor(Color.parseColor("#22335A"));
//        bodyView.setPadding(0, 0, 0, 24);
//
//        // Status Message
//        TextView messageView = new TextView(getContext());
//        messageView.setText(
//                "solved".equals(issueStatus) ?
//                        "Your reported issue has been resolved by the admin."
//                        : "Your issue is still pending admin action."
//        );
//        messageView.setTextColor(Color.parseColor("#656D7B"));
//        messageView.setTextSize(15f);
//
//        // Clickable for details
//        card.setOnClickListener(v -> {
//            if (getActivity() instanceof MainActivity && reportId != null) {
//                ((MainActivity)getActivity()).navigateToHomeAndOpenReport(reportId);
//            }
//        });
//
//        // Add rows to card
//        card.addView(statusRow);
//        card.addView(titleView);
//        card.addView(bodyView);
//        card.addView(messageView);
//
//        container.addView(card);
//    }
//}

package com.example.civic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationFragment extends Fragment {

    public NotificationFragment() {}

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        LinearLayout notificationContainer = view.findViewById(R.id.notificationContainer);
        loadNotifications(notificationContainer);

        return view;
    }

    private void loadNotifications(LinearLayout container) {
        container.removeAllViews();

        SharedPreferences prefs = requireActivity().getSharedPreferences("Notifications", Context.MODE_PRIVATE);
        String notificationsJson = prefs.getString("notifications", "[]");
        try {
            JSONArray notifications = new JSONArray(notificationsJson);

            if (notifications.length() == 0) {
                addNoNotificationCard(container);
                return;
            }

            for (int i = 0; i < notifications.length(); i++) {
                JSONObject notif = notifications.getJSONObject(i);

                String title = notif.optString("title", "No Title");
                String body = notif.optString("body", "");
                String issueStatus = notif.optString("status", "pending");
                String reportId = notif.optString("id", null);

                addNotificationCard(container, title, body, issueStatus, reportId);
            }
        } catch (JSONException e) {
            addNoNotificationCard(container);
        }
    }

    private void addNoNotificationCard(LinearLayout container) {
        TextView noNotificationView = new TextView(getContext());
        noNotificationView.setText("No report notifications.");
        noNotificationView.setPadding(32, 32, 32, 32);
        container.addView(noNotificationView);
    }

    private void addNotificationCard(LinearLayout container, String title, String body, String issueStatus, String reportId) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.card_bg); // Use drawable with rounded corners
        card.setPadding(36, 36, 36, 36);
        card.setElevation(8f);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);

        // Status row
        LinearLayout statusRow = new LinearLayout(getContext());
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView statusIcon = new ImageView(getContext());
        if ("solved".equals(issueStatus)) {
            statusIcon.setImageResource(R.drawable.ic_check_circle);
        } else {
            statusIcon.setImageResource(R.drawable.ic_clock);
        }
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
        iconParams.setMargins(0, 0, 16, 0);
        statusIcon.setLayoutParams(iconParams);

        TextView statusText = new TextView(getContext());
        statusText.setText("solved".equals(issueStatus) ? "Solved" : "Pending");
        statusText.setTextSize(18f);
        statusText.setTypeface(Typeface.DEFAULT_BOLD);
        statusText.setTextColor("solved".equals(issueStatus) ? Color.parseColor("#30C97D") : Color.parseColor("#FFA726"));

        statusRow.addView(statusIcon);
        statusRow.addView(statusText);

        // Title
        TextView titleView = new TextView(getContext());
        titleView.setText(title);
        titleView.setTextSize(20f);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(Color.parseColor("#22335A"));
        titleView.setPadding(0, 24, 0, 12);

        // Body
        TextView bodyView = new TextView(getContext());
        bodyView.setText(body);
        bodyView.setTextSize(16f);
        bodyView.setTextColor(Color.parseColor("#22335A"));
        bodyView.setPadding(0, 0, 0, 24);

        // Status message
        TextView messageView = new TextView(getContext());
        messageView.setText(
                "solved".equals(issueStatus) ?
                        "Your reported issue has been resolved by the admin."
                        : "Your issue is still pending admin action."
        );
        messageView.setTextColor(Color.parseColor("#656D7B"));
        messageView.setTextSize(15f);

        card.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity && reportId != null) {
                ((MainActivity)getActivity()).navigateToHomeAndOpenReport(reportId);
            }
        });

        card.addView(statusRow);
        card.addView(titleView);
        card.addView(bodyView);
        card.addView(messageView);

        container.addView(card);
    }
}

