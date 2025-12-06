
package com.example.civic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
//import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;
    private Context context;

    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    public void setReportList(List<Report> reportList) {
        this.reportList = reportList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Assume you have a layout file named 'item_report_card.xml' for the CardView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_card, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.tvDescription.setText(report.getDescription());
        holder.tvLocation.setText(String.format("Location: %.4f, %.4f", report.getLatitude(), report.getLongitude()));

//         Load the photo into the ImageView using Glide (if a photo exists)
//        if (report.getPhotoUrl() != null && !report.getPhotoUrl().isEmpty()) {
//            Glide.with(context).load(report.getPhotoUrl()).into(holder.ivPhotoPreview);
//            holder.ivPhotoPreview.setVisibility(View.VISIBLE);
//        } else {
//            holder.ivPhotoPreview.setVisibility(View.GONE);
//        }

        // Display a message for the video (since playing video in a list is complex)
        if (report.getVideoUrl() != null && !report.getVideoUrl().isEmpty()) {
            holder.tvVideoInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvVideoInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription;
        TextView tvLocation;
        ImageView ivPhotoPreview;
        TextView tvVideoInfo; // To show if a video exists

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            // Assume you have these IDs in item_report_card.xml
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            ivPhotoPreview = itemView.findViewById(R.id.ivPhotoPreview);
            tvVideoInfo = itemView.findViewById(R.id.tvVideoInfo);
        }
    }
}
