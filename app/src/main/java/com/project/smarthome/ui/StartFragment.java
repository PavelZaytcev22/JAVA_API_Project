// com.project.smarthome.ui.StartFragment
package com.project.smarthome.ui;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.project.smarthome.R;
import com.project.smarthome.viewmodels.StartViewModel;
import com.project.smarthome.viewmodels.StartViewModelFactory;

public class StartFragment extends Fragment {

    private StartViewModel viewModel;

    public StartFragment() {
        super(R.layout.fragment_start);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Используем ViewModel с контекстом
        viewModel = new ViewModelProvider(this,
                new StartViewModelFactory(requireContext()))
                .get(StartViewModel.class);

        observeState();
        viewModel.loadInitState();
    }

    private void observeState() {
        NavController navController = NavHostFragment.findNavController(this);

        viewModel.getInitState().observe(getViewLifecycleOwner(), state -> {
            if (!state.hasHome()) {
                navController.navigate(R.id.action_start_to_createHome);
                return;
            }

            if (!state.hasFamily()) {
                navController.navigate(R.id.action_start_to_familySetup);
                return;
            }

            if (!state.hasRooms()) {
                navController.navigate(R.id.action_start_to_roomSetup);
                return;
            }

            navController.navigate(R.id.action_start_to_home);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            // Пока минимально — позже можно добавить retry / диалог
        });
    }
}