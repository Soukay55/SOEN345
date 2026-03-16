package com.example.soen345;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    private final List<Event> events;
    private final boolean isAdmin;
    private final OnDeleteClickListener deleteClickListener;

    public EventAdapter(List<Event> events, boolean isAdmin, OnDeleteClickListener deleteClickListener) {
        this.events = events;
        this.isAdmin = isAdmin;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.title.setText(event.getTitle());
        holder.date.setText(event.getDate());
        holder.location.setText(event.getLocation());
        holder.category.setText(event.getCategory());

        if (isAdmin) {
            holder.btnDeleteEvent.setVisibility(View.VISIBLE);
            holder.btnDeleteEvent.setOnClickListener(v -> deleteClickListener.onDeleteClick(event));
        } else {
            holder.btnDeleteEvent.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, location, category;
        Button btnDeleteEvent;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            date = itemView.findViewById(R.id.eventDate);
            location = itemView.findViewById(R.id.eventLocation);
            category = itemView.findViewById(R.id.eventCategory);
            btnDeleteEvent = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}

