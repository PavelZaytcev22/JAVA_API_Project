package com.project.smarthome.models.homes.room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.smarthome.R;

import java.util.List;

public class RoomListAdapter
        extends RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {

    private final List<Room> rooms;

    public RoomListAdapter(List<Room> rooms) {
        this.rooms = rooms;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RoomViewHolder holder,
            int position
    ) {
        holder.bind(rooms.get(position));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public void updateData(List<Room> newRooms) {
        rooms.clear();
        rooms.addAll(newRooms);
        notifyDataSetChanged();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {

        private final TextView roomName;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.textRoomName);
        }

        void bind(Room room) {
            roomName.setText(room.getName());
        }
    }
}
