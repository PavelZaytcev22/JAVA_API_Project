package com.project.smarthome.models.homes.room;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.project.smarthome.R;
import com.project.smarthome.api.ApiClient;


public class CreateEditRoomFragment extends Fragment {

    private RoomRepository roomRepository;

    private EditText roomNameEdit;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_create_edit_room,
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
                ApiClient.getApiService()
        );

        roomNameEdit = view.findViewById(R.id.editRoomName);
        Button saveButton = view.findViewById(R.id.buttonSaveRoom);
        Button cancelButton = view.findViewById(R.id.buttonCancel);

        saveButton.setOnClickListener(v -> onSaveClicked());
        cancelButton.setOnClickListener(v -> goBack());
    }

    private void onSaveClicked() {
        String name = roomNameEdit.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            roomNameEdit.setError("Введите название комнаты");
            return;
        }

        roomRepository.createRoom(name, new RoomRepository.CreateRoomCallback() {
            @Override
            public void onSuccess(Room room) {
                Toast.makeText(
                        requireContext(),
                        "Комната добавлена",
                        Toast.LENGTH_SHORT
                ).show();
                goBack();
            }

            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(
                        requireContext(),
                        "Ошибка создания комнаты",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void goBack() {
        NavHostFragment
                .findNavController(this)
                .popBackStack();
    }
}
