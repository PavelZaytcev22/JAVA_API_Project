package com.project.smarthome.models.homes;

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

import com.project.smarthome.R;
import com.project.smarthome.api.ApiClient;
import com.project.smarthome.utils.SharedPrefManager;

public class CreateHomeFragment extends Fragment {

    private EditText homeNameEditText;
    private Button createButton;

    private HomeRepository homeRepository;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_create_home,
                container,
                false
        );

        homeNameEditText = view.findViewById(R.id.editTextHomeName);
        createButton = view.findViewById(R.id.buttonCreateHome);

        // Инициализация Repository
        homeRepository = new HomeRepository(
                ApiClient.getApiService(requireContext()),
                SharedPrefManager.getInstance(requireContext())
        );

        createButton.setOnClickListener(v -> createHome());

        return view;
    }

    private void createHome() {
        String homeName =
                homeNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(homeName)) {
            Toast.makeText(
                    getContext(),
                    "Введите название дома",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        createButton.setEnabled(false);

        homeRepository.createHome(
                homeName,
                new HomeRepository.RepositoryCallback<HomeResponse>() {

                    @Override
                    public void onSuccess(HomeResponse home) {
                        createButton.setEnabled(true);

                        Toast.makeText(
                                getContext(),
                                "Дом создан: " + home.getName(),
                                Toast.LENGTH_SHORT
                        ).show();

                        // TODO:
                        // - сохранить activeHomeId
                        // - перейти к списку домов или комнат
                    }

                    @Override
                    public void onError(String errorMessage) {
                        createButton.setEnabled(true);

                        Toast.makeText(
                                getContext(),
                                errorMessage,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
