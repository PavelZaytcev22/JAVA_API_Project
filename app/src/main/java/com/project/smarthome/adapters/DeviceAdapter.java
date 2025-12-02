package com.project.smarthome.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.project.smarthome.R;
import com.project.smarthome.models.Device;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Device> devices;
    private boolean isGridMode = true;
    private OnDeviceClickListener listener;

    // Типы ViewHolder
    private static final int TYPE_GRID = 0;
    private static final int TYPE_LIST = 1;

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
        void onDeviceToggle(Device device, boolean isOn);
    }

    public DeviceAdapter(List<Device> devices, OnDeviceClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    public void setGridMode(boolean gridMode) {
        this.isGridMode = gridMode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? TYPE_GRID : TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_GRID) {
            View view = inflater.inflate(R.layout.item_device_grid, parent, false);
            return new GridViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_device_list, parent, false);
            return new ListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Device device = devices.get(position);

        if (holder instanceof GridViewHolder) {
            ((GridViewHolder) holder).bind(device);
        } else if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(device);
        }
    }

    @Override
    public int getItemCount() {
        return devices != null ? devices.size() : 0;
    }

    // Вспомогательные методы для проверки состояния устройства
    private boolean isDeviceOn(Device device) {
        String state = device.getState();
        return "ON".equalsIgnoreCase(state) || "true".equalsIgnoreCase(state);
    }

    private boolean isMotionDetected(Device device) {
        String state = device.getState();
        return "MOTION".equalsIgnoreCase(state) || "true".equalsIgnoreCase(state);
    }

    private String getTemperature(Device device) {
        return device.getState(); // предполагаем, что температура хранится в state
    }

    private String getBrightness(Device device) {
        return device.getState(); // предполагаем, что яркость хранится в state
    }

    // Grid ViewHolder
    class GridViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView status;
        private View cardView;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.device_icon);
            name = itemView.findViewById(R.id.device_name);
            status = itemView.findViewById(R.id.device_status);
            cardView = itemView.findViewById(R.id.device_card);
        }

        public void bind(Device device) {
            name.setText(device.getName());

            // Установка иконки в зависимости от типа устройства
            switch (device.getType()) {
                case "lamp":
                    icon.setImageResource(R.drawable.ic_lamp);
                    boolean isLampOn = isDeviceOn(device);
                    status.setText(isLampOn ? "Вкл" : "Выкл");
                    status.setTextColor(ContextCompat.getColor(itemView.getContext(),
                            isLampOn ? R.color.green : R.color.gray));
                    break;
                case "motion_sensor":
                    icon.setImageResource(R.drawable.ic_motion_sensor);
                    boolean isMotion = isMotionDetected(device);
                    status.setText(isMotion ? "Обнаружено" : "Нет движения");
                    break;
                case "temp_sensor":
                    icon.setImageResource(R.drawable.ic_temp_sensor);
                    String temp = getTemperature(device);
                    status.setText(temp + "°C");
                    break;
                case "siren":
                    icon.setImageResource(R.drawable.ic_siren);
                    boolean isSirenOn = isDeviceOn(device);
                    status.setText(isSirenOn ? "Тревога" : "Выкл");
                    break;
                default:
                    icon.setImageResource(R.drawable.ic_siren);
                    status.setText(device.getState());
                    break;
            }

            // Обработка клика
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }
    }

    // List ViewHolder
    class ListViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView description;
        private Switch toggle;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.device_icon);
            name = itemView.findViewById(R.id.device_name);
            description = itemView.findViewById(R.id.device_description);
            toggle = itemView.findViewById(R.id.device_toggle);
        }

        public void bind(Device device) {
            name.setText(device.getName());

            switch (device.getType()) {
                case "lamp":
                    icon.setImageResource(R.drawable.ic_lamp);
                    boolean isLampOn = isDeviceOn(device);
                    description.setText("Яркость: " + getBrightness(device) + "%");
                    toggle.setChecked(isLampOn);
                    toggle.setVisibility(View.VISIBLE);
                    break;
                case "motion_sensor":
                    icon.setImageResource(R.drawable.ic_motion_sensor);
                    boolean isMotion = isMotionDetected(device);
                    description.setText(isMotion ? "Движение обнаружено" : "Нет движения");
                    toggle.setVisibility(View.GONE);
                    break;
                case "temp_sensor":
                    icon.setImageResource(R.drawable.ic_temp_sensor);
                    String temp = getTemperature(device);
                    description.setText("Температура: " + temp + "°C");
                    toggle.setVisibility(View.GONE);
                    break;
                case "siren":
                    icon.setImageResource(R.drawable.ic_siren);
                    boolean isSirenOn = isDeviceOn(device);
                    description.setText(isSirenOn ? "Тревога активна" : "Система охраны");
                    toggle.setChecked(isSirenOn);
                    toggle.setVisibility(View.VISIBLE);
                    break;
                default:
                    icon.setImageResource(R.drawable.ic_siren);
                    description.setText(device.getState());
                    toggle.setVisibility(View.GONE);
                    break;
            }

            // Обработка переключения
            toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onDeviceToggle(device, isChecked);
                }
            });

            // Обработка клика на всю карточку
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }
    }
}