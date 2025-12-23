package com.project.smarthome.models.families;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.smarthome.R;
import com.project.smarthome.databinding.FragmentFamilySetupBinding;

public class FamilySetupFragment extends Fragment {

    private FragmentFamilySetupBinding binding;
    private FamilySetupViewModel viewModel;
    private FamilyAdapter adapter;

    public FamilySetupFragment() {
        super(R.layout.fragment_family_setup);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentFamilySetupBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(FamilySetupViewModel.class);

        setupRecycler();
        observeViewModel();
        setupActions();

        viewModel.loadMembers();
    }

    private void setupRecycler() {
        adapter = new FamilyAdapter();
        binding.recyclerMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMembers.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), adapter::submitList);
    }

    private void setupActions() {
        binding.btnInvite.setOnClickListener(v -> showInviteDialog());

        binding.btnContinue.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_familySetup_to_roomSetup));
    }

    private void showInviteDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Username или Email");

        new AlertDialog.Builder(requireContext())
                .setTitle("Пригласить пользователя")
                .setView(input)
                .setPositiveButton("Пригласить", (d, w) -> {
                    String username = input.getText().toString().trim();
                    if (!username.isEmpty()) {
                        viewModel.inviteUser(username);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
