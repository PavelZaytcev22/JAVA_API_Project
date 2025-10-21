package com.project.smarthome.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.smarthome.R;
import com.project.smarthome.adapters.DeviceAdapter;
import com.project.smarthome.models.Device;
import com.project.smarthome.models.DeviceState;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment implements DeviceAdapter.OnDeviceClickListener {

    private RecyclerView devicesRecyclerView;
    private ProgressBar progressBar;
    private DeviceAdapter deviceAdapter;
    private List<Device> deviceList;
    private boolean isGridMode = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Включаем меню в toolbar
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupRecyclerView();
        loadDevices();
        return view;
    }

    private void initViews(View view) {
        devicesRecyclerView = view.findViewById(R.id.devices_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);

        // Настройка Toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        // Настройка FAB
        FloatingActionButton fabAddDevice = view.findViewById(R.id.fab_add_device);
        fabAddDevice.setOnClickListener(v -> openAddDeviceFragment());
    }

    private void setupRecyclerView() {
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(deviceList, this);
        devicesRecyclerView.setAdapter(deviceAdapter);

        // Начальный режим - сетка
        switchViewMode(true);
    }

    private void loadDevices() {
        showLoading(true);

        // TODO: Заменить на реальный API call
        // Временные демо-данные
        new android.os.Handler().postDelayed(() -> {
            List<Device> demoDevices = createDemoDevices();
            deviceList.clear();
            deviceList.addAll(demoDevices);
            deviceAdapter.notifyDataSetChanged();
            showLoading(false);
        }, 1000);
    }

    private List<Device> createDemoDevices() {
        List<Device> devices = new ArrayList<>();

        // Демо-устройства
        Device lamp1 = new Device(1, "Лампа гостиная", "lamp", true, 1);
        lamp1.getState().setOn(true);
        lamp1.getState().setBrightness(80);
        devices.add(lamp1);

        Device lamp2 = new Device(2, "Лампа спальня", "lamp", true, 2);
        lamp2.getState().setOn(false);
        lamp2.getState().setBrightness(100);
        devices.add(lamp2);

        Device motionSensor = new Device(3, "Датчик движения", "motion_sensor", true, 1);
        motionSensor.getState().setMotionDetected(true);
        devices.add(motionSensor);

        Device tempSensor = new Device(4, "Датчик температуры", "temp_sensor", true, 1);
        tempSensor.getState().setTemperature(22);
        devices.add(tempSensor);

        Device siren = new Device(5, "Сирена охраны", "siren", true, 1);
        siren.getState().setOn(false);
        devices.add(siren);

        return devices;
    }

    private void showLoading(boolean show) {
        if (progressBar != null && devicesRecyclerView != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            devicesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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

        // Обновляем иконку в меню
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private void openAddDeviceFragment() {
        // TODO: Реализовать переход к фрагменту добавления устройства
        // AddDeviceFragment addDeviceFragment = new AddDeviceFragment();
        // requireActivity().getSupportFragmentManager().beginTransaction()
        //     .replace(R.id.fragment_container, addDeviceFragment)
        //     .addToBackStack(null)
        //     .commit();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.dashboard_menu, menu);

        // Обновляем иконку в зависимости от текущего режима
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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_view_mode) {
            // Переключаем режим просмотра
            switchViewMode(!isGridMode);
            return true;
        } else if (id == R.id.menu_refresh) {
            // Обновляем список устройств
            loadDevices();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Реализация интерфейса DeviceAdapter.OnDeviceClickListener
    @Override
    public void onDeviceClick(Device device) {
        // TODO: Открыть детальную информацию об устройстве
        // DeviceDetailFragment detailFragment = DeviceDetailFragment.newInstance(device.getId());
        // requireActivity().getSupportFragmentManager().beginTransaction()...
    }

    @Override
    public void onDeviceToggle(Device device, boolean isOn) {
        // TODO: Отправить команду на сервер для изменения состояния
        if (device != null) {
            device.getState().setOn(isOn);

            // Временная демо-логика
            if (device.getType().equals("siren") && isOn) {
                // Показать предупреждение при включении сирены
                if (getContext() != null) {
                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("Включение сирены")
                            .setMessage("Вы уверены, что хотите включить сирену?")
                            .setPositiveButton("Да", (dialog, which) -> {
                                // Подтверждено - состояние уже обновлено
                                if (deviceAdapter != null) {
                                    int position = deviceList.indexOf(device);
                                    if (position != -1) {
                                        deviceAdapter.notifyItemChanged(position);
                                    }
                                }
                            })
                            .setNegativeButton("Отмена", (dialog, which) -> {
                                // Отменено - возвращаем переключатель
                                device.getState().setOn(false);
                                if (deviceAdapter != null) {
                                    int position = deviceList.indexOf(device);
                                    if (position != -1) {
                                        deviceAdapter.notifyItemChanged(position);
                                    }
                                }
                            })
                            .show();
                }
            } else {
                if (deviceAdapter != null) {
                    int position = deviceList.indexOf(device);
                    if (position != -1) {
                        deviceAdapter.notifyItemChanged(position);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        devicesRecyclerView = null;
        progressBar = null;
        deviceAdapter = null;
    }
}