package com.project.smarthome.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.smarthome.R;
import com.project.smarthome.adapters.DeviceAdapter;
import com.project.smarthome.databinding.FragmentHomeBinding;
import com.project.smarthome.models.Device;

import com.project.smarthome.models.Room;
import com.project.smarthome.viewmodels.HomeViewModel;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements DeviceAdapter.OnDeviceClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private DeviceAdapter deviceAdapter;
    private List<Device> allDevices = new ArrayList<>();
    private List<Device> filteredDevices = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private String currentRoomFilter = "all";
    private boolean isGridMode = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        setupObservers();
        loadData();
    }

    private void initViews() {
        setupRecyclerView();
        setupQuickActions();
        setupViewModeToggle();
        setupFAB();
    }

    private void setupRecyclerView() {
        deviceAdapter = new DeviceAdapter(filteredDevices, this);
        binding.devicesRecyclerView.setAdapter(deviceAdapter);
        switchViewMode(true); // Начальный режим - сетка
    }
    private void setupQuickActions() {
        // Включить всё - исправленный ID
        View quickActionPowerOn = binding.getRoot().findViewById(R.id.quick_action_power_on);
        if (quickActionPowerOn != null) {
            quickActionPowerOn.setOnClickListener(v -> {
                homeViewModel.turnAllDevices(true);
            });
        }
        // Выключить всё - исправленный ID
        View quickActionPowerOff = binding.getRoot().findViewById(R.id.quick_action_power_off);
        if (quickActionPowerOff != null) {
            quickActionPowerOff.setOnClickListener(v -> {
                homeViewModel.turnAllDevices(false);
            });
        }
        // Режим охраны - исправленный ID
        View quickActionSecurity = binding.getRoot().findViewById(R.id.quick_action_security);
        if (quickActionSecurity != null) {
            quickActionSecurity.setOnClickListener(v -> {
                enableSecurityMode();
            });
        }
    }

    private void setupViewModeToggle() {
        MaterialButton viewModeToggle = binding.viewModeToggle;
        viewModeToggle.setOnClickListener(v -> {
            isGridMode = !isGridMode;
            switchViewMode(isGridMode);
            viewModeToggle.setText(isGridMode ? "Сетка" : "Список");
            viewModeToggle.setIconResource(isGridMode ?
                    R.drawable.ic_grid_view : R.drawable.ic_list_view);
        });
    }

    private void setupFAB() {
        FloatingActionButton fabAddDevice = binding.fabAddDevice;
        fabAddDevice.setOnClickListener(v -> {
            // Исправленный action ID
            Navigation.findNavController(v).navigate(R.id.action_home_to_addDevice);
        });
    }

    private void switchViewMode(boolean gridMode) {
        if (gridMode) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            binding.devicesRecyclerView.setLayoutManager(layoutManager);
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            binding.devicesRecyclerView.setLayoutManager(layoutManager);
        }

        if (deviceAdapter != null) {
            deviceAdapter.setGridMode(gridMode);
        }
    }

    private void setupRoomsFilter() {
        // Очищаем контейнер комнат
        binding.roomsContainer.removeAllViews();

        // Добавляем кнопку "Все"
        addRoomButton("all", "Все", true);

        // Добавляем кнопки для каждой комнаты
        for (Room room : rooms) {
            addRoomButton(String.valueOf(room.getId()), room.getName(), false);
        }
    }

    private void addRoomButton(String roomId, String roomName, boolean isSelected) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(roomName);
        button.setCheckable(true);
        button.setChecked(isSelected);

        // Теперь LinearLayout будет распознан
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 8, 0);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            currentRoomFilter = roomId;
            filterDevices(getCurrentSearchQuery(), roomId);
            updateRoomButtonsSelection(roomId);
        });

        binding.roomsContainer.addView(button);
    }

    private void updateRoomButtonsSelection(String selectedRoomId) {
        for (int i = 0; i < binding.roomsContainer.getChildCount(); i++) {
            View child = binding.roomsContainer.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                String buttonRoomId = getRoomIdFromButton(button);
                button.setChecked(buttonRoomId.equals(selectedRoomId));
            }
        }
    }

    private String getRoomIdFromButton(MaterialButton button) {
        // Для кнопки "Все" возвращаем "all", для остальных - ID комнаты
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

    private void setupObservers() {
        homeViewModel.getDevices().observe(getViewLifecycleOwner(), devices -> {
            allDevices.clear();
            allDevices.addAll(devices);
            filterDevices(getCurrentSearchQuery(), currentRoomFilter);
            binding.progressBar.setVisibility(View.GONE);
        });

        homeViewModel.getRooms().observe(getViewLifecycleOwner(), rooms -> {
            this.rooms.clear();
            this.rooms.addAll(rooms);
            setupRoomsFilter();
        });

        homeViewModel.getConnectionStatus().observe(getViewLifecycleOwner(), isConnected -> {
            updateConnectionStatus(isConnected);
        });
    }

    private void loadData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        homeViewModel.loadData();
    }

    private void filterDevices(String query, String roomId) {
        filteredDevices.clear();

        for (Device device : allDevices) {
            boolean matchesSearch = query.isEmpty() ||
                    device.getName().toLowerCase().contains(query.toLowerCase()) ||
                    device.getType().toLowerCase().contains(query.toLowerCase());

            boolean matchesRoom = roomId.equals("all") ||
                    String.valueOf(device.getRoomId()).equals(roomId);

            if (matchesSearch && matchesRoom) {
                filteredDevices.add(device);
            }
        }

        deviceAdapter.notifyDataSetChanged();

        // Показать/скрыть пустое состояние
        if (filteredDevices.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private String getCurrentSearchQuery() {
        // TODO: Реализовать получение текста из SearchView
        return "";
    }

    private void showEmptyState() {
        // TODO: Показать состояние "нет устройств"
        binding.devicesRecyclerView.setVisibility(View.GONE);
        // binding.emptyState.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        // TODO: Скрыть состояние "нет устройств"
        binding.devicesRecyclerView.setVisibility(View.VISIBLE);
        // binding.emptyState.setVisibility(View.GONE);
    }

    private void updateConnectionStatus(boolean isConnected) {
        // TODO: Обновить индикатор соединения в toolbar
        if (getActivity() != null) {
            androidx.appcompat.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(isConnected ? "Подключено" : "Не подключено");
            }
        }
    }

    private void enableSecurityMode() {
        // Активировать режим охраны - включить датчики движения и сирену
        homeViewModel.enableSecurityMode();

        // Показать диалог подтверждения
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Режим охраны")
                .setMessage("Режим охраны активирован. Все датчики движения и сирены включены.")
                .setPositiveButton("OK", null)
                .show();
    }

    // Реализация DeviceAdapter.OnDeviceClickListener
    @Override
    public void onDeviceClick(Device device) {
        // Переход к детальному экрану устройства
        Bundle bundle = new Bundle();
        bundle.putInt("device_id", device.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_deviceDetail, bundle);
    }

    @Override
    public void onDeviceToggle(Device device, boolean isOn) {
        homeViewModel.toggleDevice(device.getId(), isOn);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}