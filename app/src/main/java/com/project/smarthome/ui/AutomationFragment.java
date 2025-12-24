// com.project.smarthome.ui.AutomationFragment
package com.project.smarthome.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.smarthome.R;
import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.Automation;
import com.project.smarthome.adapters.AutomationAdapter;
import com.project.smarthome.models.EnableAutomationRequest;
import com.project.smarthome.models.EnableAutomationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class AutomationFragment extends Fragment {

    private RecyclerView recyclerView;
    private AutomationAdapter adapter;
    private List<Automation> automations;
    private FloatingActionButton fabAddAutomation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_automation, container, false);

        initViews(view);
        setupRecyclerView();
        loadAutomations();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_automations);
        fabAddAutomation = view.findViewById(R.id.fab_add_automation);

        fabAddAutomation.setOnClickListener(v -> {
            openCreateAutomationDialog();
        });
    }

    private void setupRecyclerView() {
        automations = new ArrayList<>();
        adapter = new AutomationAdapter(automations, this::toggleAutomation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadAutomations() {
        ApiClient.initialize(requireContext());
        ApiService apiService =
                ApiClient.getApiService(requireContext());

        Call<List<Automation>> call = apiService.getAutomations();
        call.enqueue(new Callback<List<Automation>>() {
            @Override
            public void onResponse(Call<List<Automation>> call, Response<List<Automation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    automations.clear();
                    automations.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Ошибка загрузки автоматизаций", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Automation>> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleAutomation(Automation automation) {
        ApiClient.initialize(requireContext());
        ApiService apiService =
                ApiClient.getApiService(requireContext());

        EnableAutomationRequest request =
                new EnableAutomationRequest(!automation.isEnabled());

        Call<EnableAutomationResponse> call =
                apiService.toggleAutomation(automation.getId(), request);

        call.enqueue(new Callback<EnableAutomationResponse>() {
            @Override
            public void onResponse(Call<EnableAutomationResponse> call,
                                   Response<EnableAutomationResponse> response) {
                if (response.isSuccessful()) {
                    automation.setEnabled(!automation.isEnabled());
                    adapter.notifyDataSetChanged();
                    String message = automation.isEnabled() ? "Автоматизация включена" : "Автоматизация отключена";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ошибка изменения состояния", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EnableAutomationResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCreateAutomationDialog() {
        Toast.makeText(getContext(), "Создание автоматизации", Toast.LENGTH_SHORT).show();
        // Здесь можно открыть диалог создания автоматизации
    }
}