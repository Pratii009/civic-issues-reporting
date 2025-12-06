package com.example.civic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.IssueViewHolder> {

    private final List<Issue> issues;

    public IssuesAdapter(List<Issue> issues) {
        this.issues = issues;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        Issue issue = issues.get(position);
        holder.tvDescription.setText(issue.getDescription());
        holder.tvStatus.setText("Status: " + issue.getStatus());
    }

    @Override
    public int getItemCount() {
        return issues.size();
    }

    static class IssueViewHolder extends RecyclerView.ViewHolder {

        TextView tvDescription, tvStatus;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvIssueDescription);
            tvStatus = itemView.findViewById(R.id.tvIssueStatus);
        }
    }
}
