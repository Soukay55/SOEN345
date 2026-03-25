package com.example.soen345;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    private final List<Reservation> items;
    private final OnActionListener listener;

    public interface OnActionListener {
        void onCancel(Reservation r);
    }

    public ReservationAdapter(List<Reservation> items, OnActionListener listener) { this.items = items; this.listener = listener; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation r = items.get(position);
        holder.title.setText("Tickets: " + r.getNumberOfTickets());
        holder.date.setText(r.getReservationDate() == null ? "" : r.getReservationDate().toString());
        holder.btnCancel.setOnClickListener(v -> { if (listener != null) listener.onCancel(r); });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        Button btnCancel;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reservationTitle);
            date = itemView.findViewById(R.id.reservationDate);
            btnCancel = itemView.findViewById(R.id.btnCancelReservation);
        }
    }
}
