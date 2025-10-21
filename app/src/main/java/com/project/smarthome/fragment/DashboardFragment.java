package com.project.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project.smarthome.R;
import com.project.smarthome.adapters.DeviceAdapter;
import com.project.smarthome.models.Device;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);

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



    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        devicesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void switchViewMode(boolean gridMode) {
        isGridMode = gridMode;

        if (gridMode) {
            // Режим сетки - 2 колонки
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            devicesRecyclerView.setLayoutManager(layoutManager);
        } else {
            // Режим списка
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            devicesRecyclerView.setLayoutManager(layoutManager);
        }

        deviceAdapter.setGridMode(gridMode);

        // Обновляем иконку в меню (будет обновлено в onCreateOptionsMenu)
        requireActivity().invalidateOptionsMenu();
    }

    private void openAddDeviceFragment() {
        // TODO: Реализовать переход к фрагменту добавления устройства
        AddDeviceFragment addDeviceFragment = new AddDeviceFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addDeviceFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.dashboard_menu, menu);

        // Обновляем иконку в зависимости от текущего режима
        MenuItem viewModeItem = menu.findItem(R.id.menu_view_mode);
        if (isGridMode) {
            viewModeItem.setIcon(R.drawable.ic_list_view);
        } else {
            viewModeItem.setIcon(R.drawable.ic_grid_view);
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
        device.getState().setOn(isOn);

        // Временная демо-логика
        if (device.getType().equals("siren") && isOn) {
            // Показать предупреждение при включении сирены
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Включение сирены")
                    .setMessage("Вы уверены, что хотите включить сирену?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        // Подтверждено - состояние уже обновлено
                        deviceAdapter.notifyItemChanged(deviceList.indexOf(device));
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> {
                        // Отменено - возвращаем переключатель
                        device.getState().setOn(false);
                        deviceAdapter.notifyItemChanged(deviceList.indexOf(device));
                    })
                    .show();
        } else {
            deviceAdapter.notifyItemChanged(deviceList.indexOf(device));
        }
    }
}