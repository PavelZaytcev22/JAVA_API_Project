package com.project.smarthome.ui;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.project.smarthome.R;

public class DeviceDetailFragment extends Fragment {

    private String deviceId;

    public DeviceDetailFragment() {
        super(R.layout.fragment_device_detail);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString("device_id");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView deviceIdText = view.findViewById(R.id.device_id_text);
        if (deviceId != null) {
            deviceIdText.setText("ID устройства: " + deviceId);
        }
    }
}