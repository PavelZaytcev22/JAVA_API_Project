package com.project.smarthome.models.homes.room;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.smarthome.R;
import com.project.smarthome.api.ApiClient;


import java.util.ArrayList;
import java.util.List;

public class RoomListFragment extends Fragment {

    private RoomRepository roomRepository;

    private RecyclerView recyclerView;
    private RoomListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_room_list,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        roomRepository = new RoomRepositoryImpl(
                requireContext(),
                ApiClient.getInstance().getApiService()
        );

        setupRecycler(view);
        setupButtons(view);

        loadRooms();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список при возврате с экрана создания комнаты
        loadRooms();
    }

    private void setupRecycler(@NonNull View view) {
        recyclerView = view.findViewById(R.id.recyclerRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new RoomListAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons(@NonNull View view) {
        Button addRoomButton = view.findViewById(R.id.buttonAddRoom);
        Button finishButton = view.findViewById(R.id.buttonFinish);

        NavController navController =
                NavHostFragment.findNavController(this);

        addRoomButton.setOnClickListener(v ->
                navController.navigate(
                        R.id.action_roomListFragment_to_createEditRoomFragment
                )
        );

        finishButton.setOnClickListener(v -> {
            startActivity(
                    new Intent(requireContext(), HomeActivity.class)
            );
            requireActivity().finish();
        });
    }

    private void loadRooms() {
        roomRepository.loadRooms(new RoomRepository.LoadRoomsCallback() {
            @Override
            public void onSuccess(List<Room> rooms) {
                adapter.updateData(rooms);
            }

            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки комнат",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
