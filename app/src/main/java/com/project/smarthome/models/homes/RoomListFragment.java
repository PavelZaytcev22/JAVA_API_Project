package com.project.smarthome.models.homes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.smarthome.R;
import com.project.smarthome.utils.SharedPrefManager;

public class RoomListFragment extends Fragment {

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

        Button addRoomButton = view.findViewById(R.id.buttonAddRoom);
        Button finishButton = view.findViewById(R.id.buttonFinish);

        addRoomButton.setOnClickListener(v -> {
            // следующий шаг — CreateEditRoomFragment
            // пока заглушка
        });

        finishButton.setOnClickListener(v -> {
            if (getContext() == null) return;

            // Контроль: активный дом должен существовать
            long homeId = SharedPrefManager
                    .getInstance(requireContext())
                    .getActiveHomeId();

            if (homeId == -1) {
                // сюда можно Snackbar / Toast
                return;
            }

            startActivity(
                    new Intent(requireContext(), HomeActivity.class)
            );
            requireActivity().finish();
        });
    }
}
