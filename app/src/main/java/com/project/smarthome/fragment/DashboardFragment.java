package com.project.smarthome.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.project.smarthome.R;
import com.project.smarthome.adapters.DeviceAdapter;
import com.project.smarthome.models.Device;
import com.project.smarthome.viewmodels.HomeViewModel;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment implements DeviceAdapter.OnDeviceClickListener {

    private RecyclerView devicesRecyclerView;
    private ProgressBar progressBar;
    private DeviceAdapter deviceAdapter;
    private HomeViewModel homeViewModel;
    private boolean isGridMode = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Инициализируем ViewModel
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupToolbar();
        setupFAB();
    }

    private void initViews(View view) {
        devicesRecyclerView = view.findViewById(R.id.devices_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = getView().findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Мой дом");
            }
        }
    }

    private void setupFAB() {
        FloatingActionButton fabAddDevice = getView().findViewById(R.id.fab_add_device);
        fabAddDevice.setOnClickListener(v -> showAddDeviceDialog());
    }

    private void setupRecyclerView() {
        deviceAdapter = new DeviceAdapter(new ArrayList<>(), this);
        devicesRecyclerView.setAdapter(deviceAdapter);
        switchViewMode(true); // Начальный режим - сетка
    }

    private void setupObservers() {
        // Наблюдаем за списком устройств
        homeViewModel.getDevices().observe(getViewLifecycleOwner(), devices -> {
            if (devices != null && !devices.isEmpty()) {
                deviceAdapter.setDevices(devices);
                hideEmptyState();
            } else {
                deviceAdapter.setDevices(new ArrayList<>());
                showEmptyState();
            }
        });

        // Наблюдаем за состоянием загрузки
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                showLoading(isLoading);
            }
        });

        // Наблюдаем за ошибками
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                homeViewModel.getErrorMessage(); // Сбрасываем ошибку
            }
        });

        // Наблюдаем за статусом соединения
        homeViewModel.getConnectionStatus().observe(getViewLifecycleOwner(), connected -> {
            if (connected != null && !connected) {
                showNetworkError();
            }
        });
    }

    private void switchViewMode(boolean gridMode) {
        isGridMode = gridMode;

        if (devicesRecyclerView != null) {
            if (gridMode) {
                // Режим сетки - 2 колонки
                GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
                devicesRecyclerView.setLayoutManager(layoutManager);
            } else {
                // Режим списка
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                devicesRecyclerView.setLayoutManager(layoutManager);
            }

            if (deviceAdapter != null) {
                deviceAdapter.setGridMode(gridMode);
            }
        }

        // Обновляем меню
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null && devicesRecyclerView != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            devicesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState() {
        // TODO: Показать состояние "Нет устройств"
        // Можно добавить TextView с иконкой и текстом "Добавьте первое устройство"
    }

    private void hideEmptyState() {
        // TODO: Скрыть состояние "Нет устройств"
    }

    private void showError(String error) {
        Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG)
                .setAction("Повторить", v -> homeViewModel.refreshData())
                .show();
    }

    private void showNetworkError() {
        Snackbar.make(requireView(), "Нет соединения с сервером", Snackbar.LENGTH_INDEFINITE)
                .setAction("Повторить", v -> homeViewModel.refreshData())
                .show();
    }

    private void showAddDeviceDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Добавить устройство")
                .setMessage("Функция добавления устройства будет реализована в следующем обновлении")
                .setPositiveButton("ОК", null)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.dashboard_menu, menu);
        updateViewModeIcon(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void updateViewModeIcon(Menu menu) {
        MenuItem viewModeItem = menu.findItem(R.id.menu_view_mode);
        if (viewModeItem != null) {
            if (isGridMode) {
                viewModeItem.setIcon(R.drawable.ic_list_view);
                viewModeItem.setTitle("Список");
            } else {
                viewModeItem.setIcon(R.drawable.ic_grid_view);
                viewModeItem.setTitle("Сетка");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_view_mode) {
            switchViewMode(!isGridMode);
            return true;
        } else if (id == R.id.menu_refresh) {
            homeViewModel.refreshData();
            return true;
        } else if (id == R.id.menu_settings) {
            navigateToSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToSettings() {
        Toast.makeText(requireContext(), "Настройки будут реализованы позже", Toast.LENGTH_SHORT).show();
    }

    // Реализация интерфейса DeviceAdapter.OnDeviceClickListener
    @Override
    public void onDeviceClick(Device device) {
        // TODO: Открыть детальную информацию об устройстве
        Toast.makeText(requireContext(), "Открыть детали: " + device.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceToggle(Device device, boolean isOn) {
        String newState = isOn ? "ON" : "OFF";

        if (device.getType().equals("siren") && isOn) {
            // Показать подтверждение для сирены
            showSirenConfirmationDialog(device, newState);
        } else {
            // Немедленное управление для других устройств
            homeViewModel.controlDevice(device.getId(), newState);
        }
    }

    private void showSirenConfirmationDialog(Device device, String newState) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Включение сирены")
                .setMessage("Вы уверены, что хотите включить сирену? Это может вызвать тревогу.")
                .setPositiveButton("Включить", (dialog, which) -> {
                    homeViewModel.controlDevice(device.getId(), newState);
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    // Отменяем переключение в UI
                    deviceAdapter.setDevices(homeViewModel.getDevices().getValue());
                })
                .setOnCancelListener(dialog -> {
                    // Отменяем переключение в UI при отмене диалога
                    deviceAdapter.setDevices(homeViewModel.getDevices().getValue());
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на фрагмент
        if (homeViewModel.getCurrentHomeId() != -1) {
            homeViewModel.refreshData();
        } else {
            homeViewModel.loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очищаем ссылки
        devicesRecyclerView = null;
        progressBar = null;
        deviceAdapter = null;
    }
}