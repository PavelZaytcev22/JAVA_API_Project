package com.project.smarthome.models.homes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.project.smarthome.R;
import com.project.smarthome.adapters.DeviceAdapter;
import com.project.smarthome.databinding.FragmentHomeBinding;
import com.project.smarthome.models.devices.Device;
import com.project.smarthome.viewmodels.HomeViewModel;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements DeviceAdapter.OnDeviceClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private DeviceAdapter deviceAdapter;
    private NavController navController;

    private List<Device> allDevices = new ArrayList<>();
    private List<Device> filteredDevices = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();

    private String currentRoomFilter = "all";
    private String currentSearchQuery = "";
    private boolean isGridMode = true;
    private boolean isRefreshing = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        initializeViews();
        setupObservers();
        setupListeners();
        loadInitialData();
    }

    private void initializeViews() {
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupProgressBar();
    }

    private void setupToolbar() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Умный дом");
            }
        }
    }

    private void setupRecyclerView() {
        deviceAdapter = new DeviceAdapter(filteredDevices, this);
        binding.devicesRecyclerView.setAdapter(deviceAdapter);
        switchViewMode(true);
    }

    private void setupSearch() {
        binding.searchBar.setOnClickListener(v -> binding.searchView.show());

        EditText searchEditText = binding.searchView.getEditText();
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    filterDevices();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        binding.searchView.addTransitionListener((searchView, previousState, newState) -> {
            if (newState == com.google.android.material.search.SearchView.TransitionState.HIDDEN) {
                if (binding.searchView.getEditText() != null) {
                    binding.searchView.getEditText().setText("");
                }
                currentSearchQuery = "";
                filterDevices();
            }
        });
    }

    private void setupProgressBar() {
        binding.progressBar.setIndeterminate(true);
        binding.progressBar.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // Быстрые действия
        binding.quickActionPowerOn.setOnClickListener(v -> controlAllDevices(true));
        binding.quickActionPowerOff.setOnClickListener(v -> controlAllDevices(false));
        binding.quickActionSecurity.setOnClickListener(v -> enableSecurityMode());

        // Переключение режима просмотра
        binding.viewModeToggle.setOnClickListener(v -> {
            isGridMode = !isGridMode;
            switchViewMode(isGridMode);
            updateViewModeButton();
        });
        updateViewModeButton();

        // FAB - удаляем слушатель здесь, так как он уже настроен в MainActivity
        // binding.fabAddDevice.setOnClickListener(v -> navigateToAddDevice());
    }

    private void setupObservers() {
        homeViewModel.getDevices().observe(getViewLifecycleOwner(), devices -> {
            allDevices.clear();
            if (devices != null) {
                allDevices.addAll(devices);
            }
            filterDevices();
            hideLoading();
        });

        homeViewModel.getRooms().observe(getViewLifecycleOwner(), roomList -> {
            rooms.clear();
            if (roomList != null) {
                rooms.addAll(roomList);
            }
            setupRoomsFilter();
        });

        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    showLoading();
                } else {
                    hideLoading();
                }
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });

        homeViewModel.getConnectionStatus().observe(getViewLifecycleOwner(), isConnected -> {
            if (isConnected != null) {
                updateConnectionStatus(isConnected);
            }
        });
    }

    private void loadInitialData() {
        if (homeViewModel.getCurrentHomeId() != -1) {
            refreshData();
        } else {
            homeViewModel.loadData();
        }
    }

    private void refreshData() {
        if (isRefreshing) return;
        isRefreshing = true;
        homeViewModel.refreshData();
        requireView().postDelayed(() -> isRefreshing = false, 2000);
    }

    private void switchViewMode(boolean gridMode) {
        if (gridMode) {
            binding.devicesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        } else {
            binding.devicesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
        if (deviceAdapter != null) {
            deviceAdapter.setGridMode(gridMode);
        }
    }

    private void updateViewModeButton() {
        binding.viewModeToggle.setText(isGridMode ? "Список" : "Сетка");
        binding.viewModeToggle.setIconResource(isGridMode ?
                R.drawable.ic_list_view : R.drawable.ic_grid_view);
    }

    private void setupRoomsFilter() {
        binding.roomsContainer.removeAllViews();
        addRoomButton("all", "Все", currentRoomFilter.equals("all"));
        for (Room room : rooms) {
            String roomId = String.valueOf(room.getId());
            addRoomButton(roomId, room.getName(), currentRoomFilter.equals(roomId));
        }
    }

    private void addRoomButton(String roomId, String roomName, boolean isSelected) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(roomName);
        button.setCheckable(true);
        button.setChecked(isSelected);
        button.setCornerRadius(20);

        // Используем цвет purple_500 вместо primary
        int selectedColor = getResources().getColor(R.color.purple_500);
        int unselectedColor = getResources().getColor(R.color.gray);
        int textSelectedColor = getResources().getColor(R.color.white);
        int textUnselectedColor = getResources().getColor(R.color.text_primary);

        button.setStrokeColorResource(isSelected ? R.color.purple_500 : R.color.gray);
        button.setTextColor(isSelected ? textSelectedColor : textUnselectedColor);
        if (isSelected) {
            button.setBackgroundColor(getResources().getColor(R.color.purple_500));
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 8, 0);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            currentRoomFilter = roomId;
            filterDevices();
            updateRoomButtonsSelection();
        });

        binding.roomsContainer.addView(button);
    }

    private void updateRoomButtonsSelection() {
        for (int i = 0; i < binding.roomsContainer.getChildCount(); i++) {
            View child = binding.roomsContainer.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                String buttonRoomId = getRoomIdFromButton(button);
                boolean isSelected = buttonRoomId.equals(currentRoomFilter);
                button.setChecked(isSelected);

                int selectedColor = getResources().getColor(R.color.purple_500);
                int unselectedColor = getResources().getColor(R.color.gray);
                int textSelectedColor = getResources().getColor(R.color.white);
                int textUnselectedColor = getResources().getColor(R.color.text_primary);

                button.setStrokeColorResource(isSelected ? R.color.purple_500 : R.color.gray);
                button.setTextColor(isSelected ? textSelectedColor : textUnselectedColor);
                if (isSelected) {
                    button.setBackgroundColor(getResources().getColor(R.color.purple_500));
                } else {
                    button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            }
        }
    }

    private String getRoomIdFromButton(MaterialButton button) {
        return button.getText().toString().equals("Все") ? "all" :
                getRoomIdFromName(button.getText().toString());
    }

    private String getRoomIdFromName(String roomName) {
        for (Room room : rooms) {
            if (room.getName().equals(roomName)) {
                return String.valueOf(room.getId());
            }
        }
        return "all";
    }

    private void filterDevices() {
        filteredDevices.clear();
        for (Device device : allDevices) {
            boolean matchesRoom = currentRoomFilter.equals("all") ||
                    (device.getRoomId() != null &&
                            String.valueOf(device.getRoomId()).equals(currentRoomFilter));
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    device.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());
            if (matchesRoom && matchesSearch) {
                filteredDevices.add(device);
            }
        }
        deviceAdapter.setDevices(filteredDevices);
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredDevices.isEmpty();
        boolean isSearching = !currentSearchQuery.isEmpty();
        if (isEmpty) {
            showEmptyState(isSearching);
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState(boolean isSearching) {
        binding.devicesRecyclerView.setVisibility(View.GONE);
        if (isSearching) {
            showSnackbar("По запросу \"" + currentSearchQuery + "\" ничего не найдено");
        } else if (currentRoomFilter.equals("all")) {
            showSnackbar("Устройств пока нет. Добавьте первое устройство!");
        } else {
            String roomName = getRoomNameById(currentRoomFilter);
            showSnackbar("В комнате \"" + roomName + "\" нет устройств");
        }
    }

    private void hideEmptyState() {
        binding.devicesRecyclerView.setVisibility(View.VISIBLE);
    }

    private String getRoomNameById(String roomId) {
        if (roomId.equals("all")) return "Все";
        for (Room room : rooms) {
            if (String.valueOf(room.getId()).equals(roomId)) {
                return room.getName();
            }
        }
        return "Неизвестная комната";
    }

    private void controlAllDevices(boolean turnOn) {
        if (allDevices.isEmpty()) {
            showSnackbar("Нет устройств для управления");
            return;
        }
        String action = turnOn ? "включены" : "выключены";
        int count = 0;
        for (Device device : allDevices) {
            if (device.getType().equals("lamp") || device.getType().equals("siren")) {
                homeViewModel.controlDevice(device.getId(), turnOn ? "ON" : "OFF");
                count++;
            }
        }
        if (count > 0) {
            showSnackbar(count + " устройств " + action);
        } else {
            showSnackbar("Нет управляемых устройств");
        }
    }

    private void enableSecurityMode() {
        if (allDevices.isEmpty()) {
            showSnackbar("Нет устройств для режима охраны");
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Режим охраны")
                .setMessage("Активировать режим охраны? Будут включены все датчики движения и сирены.")
                .setPositiveButton("Активировать", (dialog, which) -> {
                    int count = 0;
                    for (Device device : allDevices) {
                        if (device.getType().equals("siren") || device.getType().equals("motion_sensor")) {
                            homeViewModel.controlDevice(device.getId(), "ON");
                            count++;
                        }
                    }
                    showSnackbar("Режим охраны активирован: " + count + " устройств");
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.devicesRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.devicesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void updateConnectionStatus(boolean isConnected) {
        if (getActivity() != null) {
            String status = isConnected ? "Подключено" : "Нет соединения";
            binding.toolbar.setSubtitle(status);
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    private void showError(String error) {
        Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
                .setAction("Повторить", v -> homeViewModel.refreshData())
                .show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.dashboard_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            refreshData();
            showSnackbar("Обновление данных...");
            return true;
        } else if (id == R.id.menu_view_mode) {
            isGridMode = !isGridMode;
            switchViewMode(isGridMode);
            updateViewModeButton();
            return true;
        } else if (id == R.id.menu_settings) {
            // Нет настройки в навигационном графе, можно добавить позже
            showSnackbar("Настройки пока не доступны");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInitialData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDeviceClick(Device device) {
        Bundle args = new Bundle();
        args.putString("device_id", String.valueOf(device.getId())); // Исправлено на String
        navController.navigate(R.id.action_home_to_deviceDetail, args);
    }

    @Override
    public void onDeviceToggle(Device device, boolean isOn) {
        String newState = isOn ? "ON" : "OFF";
        if (device.getType().equals("siren") && isOn) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Включение сирены")
                    .setMessage("Вы уверены, что хотите включить сирену?")
                    .setPositiveButton("Включить", (dialog, which) -> {
                        homeViewModel.controlDevice(device.getId(), newState);
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> {
                        filterDevices(); // Обновляем UI
                    })
                    .show();
        } else {
            homeViewModel.controlDevice(device.getId(), newState);
        }
    }
}