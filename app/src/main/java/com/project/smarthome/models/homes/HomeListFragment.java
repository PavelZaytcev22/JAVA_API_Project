package com.project.smarthome.models.homes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.project.smarthome.R;

public class HomeListFragment extends Fragment {

    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_home_list,
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

        navController = Navigation.findNavController(view);

        Button createHomeButton = view.findViewById(R.id.buttonCreateHome);
        createHomeButton.setOnClickListener(v ->
                navController.navigate(
                        R.id.action_homeList_to_createHome
                )
        );
    }
}
